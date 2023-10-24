package liruonian.orion.connector.rpc;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import liruonian.orion.Configs;
import liruonian.orion.codec.rpc.OrionCodec;
import liruonian.orion.codec.rpc.OrionProtocol;
import liruonian.orion.commons.NamedThreadFactory;
import liruonian.orion.config.ConnectorConfig;
import liruonian.orion.core.ConnectorBase;
import liruonian.orion.lifecycle.LifecycleException;
import liruonian.orion.remoting.ConnectionEventHandler;
import liruonian.orion.remoting.ServerIdleHandler;
import liruonian.orion.remoting.utils.EventLoopUtils;
import liruonian.orion.rpc.RpcServerHandler;

/**
 * 基于自定协议orion的连接器实现
 *
 * @author lihao
 * @date 2020年8月10日
 * @version 1.0
 */
public class OrionConnector extends ConnectorBase {

    private static Logger logger = LoggerFactory.getLogger(OrionConnector.class);

    private ServerBootstrap bootstrap;
    private EventLoopGroup acceptorGroup;
    private EventLoopGroup ioGroup;
    private ChannelFuture channelFuture;

    private OrionProtocol protocol = new OrionProtocol();
    private OrionCodec codec = new OrionCodec(protocol);

    public OrionConnector(ConnectorConfig connectorConfig) {
        super(connectorConfig);
    }

    /*
     * @see liruonian.orion.LifecycleSupport#initializeInternal()
     */
    @Override
    protected void initializeInternal() throws LifecycleException {
        setName(getConfig().getName());

        bootstrap = new ServerBootstrap();
        acceptorGroup = EventLoopUtils.newEventLoopGroup(1,
                new NamedThreadFactory("orion-server-acceptor", false));
        ioGroup = EventLoopUtils.newEventLoopGroup(
                Runtime.getRuntime().availableProcessors() * 2,
                new NamedThreadFactory("orion-server-io", true));

        bootstrap.group(acceptorGroup, ioGroup)
                .channel(EventLoopUtils.getServerChannelClass())
                .option(ChannelOption.SO_BACKLOG, 1024)
                // .option(ChannelOption.TCP_NODELAY, true)
                // .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_BACKLOG, 1024);

        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();

                pipeline.addLast(codec.newCoder());

                if (Configs.heartbeatEnabled()) {
                    pipeline.addLast(new IdleStateHandler(0, 0, Configs.serverIdleTime(),
                            TimeUnit.MILLISECONDS));
                    pipeline.addLast(new ServerIdleHandler());
                }

                pipeline.addLast("connectionEventHandler", new ConnectionEventHandler());
                pipeline.addLast(new RpcServerHandler(getEventBus()));
            }

        });
    }

    /*
     * @see liruonian.orion.LifecycleSupport#startInternal()
     */
    @Override
    protected void startInternal() throws LifecycleException {
        try {
            channelFuture = bootstrap.bind(new InetSocketAddress(getIp(), getPort()))
                    .sync();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            throw new LifecycleException(e);
        }
    }

    /*
     * @see liruonian.orion.LifecycleSupport#stopInternal()
     */
    @Override
    protected void stopInternal() throws LifecycleException {
        if (channelFuture != null) {
            channelFuture.channel().close();
        }
        acceptorGroup.shutdownGracefully();
        ioGroup.shutdownGracefully();
    }

}
