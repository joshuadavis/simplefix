package eg;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import static org.slf4j.LoggerFactory.*;

/**
 * Netty client/server example.
 * <br>
 * User: josh
 * Date: 9/7/12
 * Time: 10:18 AM
 */
public class SimpleClientServerExample {
    private static final Logger log = getLogger(SimpleClientServerExample.class);
    private static final int PORT = 18080;

    class ServerHandler extends SimpleChannelHandler {
        private final Server server;

        ServerHandler(Server server) {
            this.server = server;
        }

        @Override
        public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            log.info("channelOpen() : " + e);
            server.channelOpen(e.getChannel());
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

            ChannelBuffer buf = (ChannelBuffer) e.getMessage();
            int readableBytes = buf.readableBytes();
            if (readableBytes > 0)
            {
                log.info("readableBytes=" + readableBytes);
                byte[] bytes = new byte[readableBytes];
                buf.getBytes(0,bytes);
                String s = new String(bytes);
                log.info("received: >" + s + "<");
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            logExceptionEventAndClose(e);
        }
    }

    private void logExceptionEventAndClose(ExceptionEvent e) {
        final Throwable throwable = e.getCause();
        log.error("Unexpected: " + throwable, throwable);
        Channel ch = e.getChannel();
        ch.close();
    }

    class Server {

        private final ChannelGroup allChannels = new DefaultChannelGroup("example-server");
        private ChannelFactory factory;

        void start() {
            log.info("Server.start()");
            final Server me = this; // Remember the 'outer this'.
            factory = new NioServerSocketChannelFactory(
                    Executors.newCachedThreadPool(),
                    Executors.newCachedThreadPool());
            ServerBootstrap bootstrap = new ServerBootstrap(factory);
            bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
                public ChannelPipeline getPipeline() throws Exception {
                    return Channels.pipeline(
                            new ServerHandler(me)
                    );
                }
            });
            bootstrap.setOption("child.tcpNoDelay", true);
            bootstrap.setOption("child.keepAlive", true);
            Channel channel = bootstrap.bind(new InetSocketAddress(PORT));
            allChannels.add(channel);
            log.info("Server listening on " + PORT);
        }

        public void stop() {
            log.info("Closing channels...");
            ChannelGroupFuture future = allChannels.close();
            future.awaitUninterruptibly();
            log.info("Channels closed.");
            factory.releaseExternalResources();
            log.info("Server stopped.");
        }

        public void channelOpen(Channel channel) {
            allChannels.add(channel);
        }


    }

    class ClientHandler extends SimpleChannelHandler {
        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            logExceptionEventAndClose(e);
        }
    }

    class Client {

        private ChannelFactory factory;
        private Channel channel;

        public void start() {
            log.info("Client.start()");
            factory = new NioClientSocketChannelFactory(
                    Executors.newCachedThreadPool(),
                    Executors.newCachedThreadPool());
            ClientBootstrap bootstrap = new ClientBootstrap(factory);
            bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
                public ChannelPipeline getPipeline() throws Exception {
                    return Channels.pipeline(
                            new ClientHandler());
                }
            });
            bootstrap.setOption("child.tcpNoDelay", true);
            bootstrap.setOption("child.keepAlive", true);
            log.info("Client connecting to " + PORT + " ...");
            ChannelFuture future = bootstrap.connect(new InetSocketAddress(PORT));
            future.awaitUninterruptibly();
            if (!future.isSuccess()) {
                log.error("Unable to connect due to: " + future.getCause(), future.getCause());
            } else {
                log.info("Client connected.");
            }
            channel = future.getChannel();
        }

        public void stop() {
            log.info("Client disconnecting...");
            channel.getCloseFuture().awaitUninterruptibly();
            factory.releaseExternalResources();
        }

        public void write(String s) {
            if (channel.isWritable())
            {
                log.info("Client: writing '" + s + "'...");
                ChannelBuffer buf = ChannelBuffers.dynamicBuffer(s.length());
                buf.writeBytes(s.getBytes());
                channel.write(buf);
            }
            else
                log.info("Client: channel not writable.");
        }
    }

    private void go() {

        Server server = new Server();
        server.start();

        // Now connect and send some data.

        Client client = new Client();
        client.start();


        log.info("go() : processing goes here...");

        client.write("hello world!");

        client.stop();

        server.stop();
    }

    public static void main(String[] args) {
        SimpleClientServerExample ex = new SimpleClientServerExample();
        ex.go();
    }
}
