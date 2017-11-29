package ipd.fontys.rtt;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

public class RttReader implements Closeable {

    private final static int BUFFER_SIZE = 512;
    /**
     * The segger RTT server will send a 3 line message when a connection is established, so initialize
     * the Semaphore at -3 because they shouldn't count as response.
     */
    private final static int SEM_INIT_VAL = -3;

    private final Process gdbServer;

    private final EventLoopGroup workGroup = new NioEventLoopGroup();
    private final Semaphore responseSem;
    private final AtomicReference<String> rttResponse;

    private final static Bootstrap bootstrap = new Bootstrap()
            .group(new NioEventLoopGroup())
            .channel(NioSocketChannel.class)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.TCP_NODELAY, true);

    /**
     * Create a new {@link RttReader} object.
     *
     * @throws IOException When the socket client couldn't connect to the RTT server.
     */
    public RttReader() throws IOException {
        responseSem = new Semaphore(SEM_INIT_VAL, true);
        rttResponse = new AtomicReference<>();
        gdbServer = new ProcessBuilder()
                .command("JLinkGDBServer",  "-device", "STM32F412ZG", "-speed", "4000", "-if", "SWD", "-nohalt")
                .inheritIO()
                .start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Connect to the TCP port
     * @param ipAddress The IP address where the RTT server is listening.
     * @param port      The port where the RTT server is.
     * @throws IOException
     */
    public void connect(final String ipAddress, final int port) throws IOException {
        Channel socketChannel = bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws IOException {
                ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8),
                        new LineBasedFrameDecoder(BUFFER_SIZE),
                        new StringDecoder(CharsetUtil.UTF_8),
                        new RttHandler(RttReader.this, responseSem));
            }
        }).connect(new InetSocketAddress(ipAddress, port))
                .syncUninterruptibly()
                .channel();
        socketChannel.read();
        socketChannel.flush();
    }

    public String getMessage() {
        responseSem.acquireUninterruptibly();
        return rttResponse.get();
    }

    /**
     * Close the TCP connection
     * @throws IOException When the TCP connection couldn't be properly closed.
     */
    @Override
    public void close() throws IOException {
        workGroup.shutdownGracefully();
        gdbServer.destroy();
    }

    /**
     * Handle TCP connection events.
     */
    private final static class RttHandler extends SimpleChannelInboundHandler<String> {

        private final Semaphore responseSem;
        private final RttReader RttReader;

        RttHandler(RttReader RttReader, Semaphore responseSem) {
            this.responseSem = responseSem;
            this.RttReader = RttReader;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            System.out.println(msg);
            RttReader.rttResponse.set(msg);
            responseSem.release();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace(System.err);
            ctx.close();
        }
    }
}
