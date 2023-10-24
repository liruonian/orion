package liruonian.orion.client.hrpc;

import io.netty.channel.ChannelHandler;
import liruonian.orion.client.Client;
import liruonian.orion.codec.hrpc.HrpcClientCodec;
import liruonian.orion.codec.hrpc.HrpcProtocol;
import liruonian.orion.lifecycle.LifecycleException;
import liruonian.orion.lifecycle.LifecycleSupport;
import liruonian.orion.remoting.Connection;
import liruonian.orion.remoting.ConnectionEventHandler;
import liruonian.orion.remoting.ConnectionManager;
import liruonian.orion.remoting.DefaultConnectionFactory;
import liruonian.orion.remoting.DefaultConnectionManager;
import liruonian.orion.remoting.InvokeCallback;
import liruonian.orion.remoting.InvokeFuture;
import liruonian.orion.remoting.RandomSelectStrategy;
import liruonian.orion.remoting.RemotingException;
import liruonian.orion.remoting.SelectStrategy;
import liruonian.orion.remoting.Url;
import liruonian.orion.remoting.UrlParser;
import liruonian.orion.remoting.message.RequestBody;
import liruonian.orion.remoting.message.RpcResponse;
import liruonian.orion.rpc.RpcClientHandler;
import liruonian.orion.rpc.RpcRemoting;
import liruonian.orion.rpc.RpcUrlParser;

/**
 * Rpc客户端实现，用于与服务端通信的核心接口实现，同时也实现连接管理、连接事件监听等功能
 *
 * @author lihao
 * @date 2020年8月20日
 * @version 1.0
 */
public class HrpcClient extends LifecycleSupport implements Client {

    private HrpcProtocol protocol;
    private HrpcClientCodec codec;
    private RpcRemoting remoting;
    private ConnectionManager connectionManager;

    /*
     * @see liruonian.orion.LifecycleSupport#initializeInternal()
     */
    @Override
    protected void initializeInternal() throws LifecycleException {
        this.protocol = new HrpcProtocol();
        this.codec = new HrpcClientCodec(protocol);

        ChannelHandler connectionEventHandler = new ConnectionEventHandler();
        ChannelHandler rpclientHandler = new RpcClientHandler();
        UrlParser addressParser = new RpcUrlParser(protocol);
        SelectStrategy<Connection> selectStrategy = new RandomSelectStrategy<Connection>();

        // 初始化连接管理器，用于生成和管理连接状态
        connectionManager = new DefaultConnectionManager(
                new DefaultConnectionFactory(protocol, codec, connectionEventHandler,
                        rpclientHandler),
                selectStrategy);

        // 客户端远程调用实现类
        remoting = new RpcRemoting(protocol.getMessageFactory(), addressParser,
                connectionManager);
    }

    /*
     * @see liruonian.orion.LifecycleSupport#startInternal()
     */
    @Override
    protected void startInternal() throws LifecycleException {
        connectionManager.start();
    }

    /*
     * @see liruonian.orion.LifecycleSupport#stopInternal()
     */
    @Override
    protected void stopInternal() throws LifecycleException {
        connectionManager.stop();
    }

    /*
     * @see liruonian.orion.Client#oneway(java.lang.String,
     * liruonian.orion.remoting.RequestBody)
     */
    @Override
    public void oneway(String address, RequestBody requestBody)
            throws RemotingException, InterruptedException {
        remoting.oneway(address, requestBody);
    }

    /*
     * @see liruonian.orion.Client#oneway(liruonian.orion.remoting.Url,
     * liruonian.orion.remoting.RequestBody)
     */
    @Override
    public void oneway(Url url, RequestBody requestBody)
            throws RemotingException, InterruptedException {
        remoting.oneway(url, requestBody);
    }

    /*
     * @see liruonian.orion.Client#oneway(liruonian.orion.remoting.Connection,
     * liruonian.orion.remoting.RequestBody)
     */
    @Override
    public void oneway(Connection conn, RequestBody requestBody)
            throws RemotingException {
        remoting.oneway(conn, requestBody);
    }

    /*
     * @see liruonian.orion.Client#invokeSync(java.lang.String,
     * liruonian.orion.remoting.RequestBody, int)
     */
    @Override
    public RpcResponse invoke(String address, RequestBody requestBody, int timeoutMillis)
            throws RemotingException, InterruptedException {
        return remoting.invoke(address, requestBody, timeoutMillis);
    }

    /*
     * @see liruonian.orion.Client#invokeSync(liruonian.orion.remoting.Url,
     * liruonian.orion.remoting.RequestBody, int)
     */
    @Override
    public RpcResponse invoke(Url url, RequestBody requestBody, int timeoutMillis)
            throws RemotingException, InterruptedException {
        return remoting.invoke(url, requestBody, timeoutMillis);
    }

    /*
     * @see liruonian.orion.Client#invokeSync(liruonian.orion.remoting.Connection,
     * liruonian.orion.remoting.RequestBody, int)
     */
    @Override
    public RpcResponse invoke(Connection conn, RequestBody requestBody, int timeoutMillis)
            throws RemotingException, InterruptedException {
        return remoting.invoke(conn, requestBody, timeoutMillis);
    }

    /*
     * @see liruonian.orion.Client#invokeWithFuture(java.lang.String,
     * liruonian.orion.remoting.RequestBody, int)
     */
    @Override
    public InvokeFuture invokeAsync(String address, RequestBody requestBody,
            int timeoutMillis) throws RemotingException, InterruptedException {
        return remoting.invokeAsync(address, requestBody, timeoutMillis);
    }

    /*
     * @see liruonian.orion.Client#invokeWithFuture(liruonian.orion.remoting.Url,
     * liruonian.orion.remoting.RequestBody, int)
     */
    @Override
    public InvokeFuture invokeAsync(Url url, RequestBody requestBody, int timeoutMillis)
            throws RemotingException, InterruptedException {
        return remoting.invokeAsync(url, requestBody, timeoutMillis);
    }

    /*
     * @see liruonian.orion.Client#invokeWithFuture(liruonian.orion.remoting.
     * Connection, liruonian.orion.remoting.RequestBody, int)
     */
    @Override
    public InvokeFuture invokeAsync(Connection conn, RequestBody requestBody,
            int timeoutMillis) throws RemotingException, InterruptedException {
        return remoting.invokeAsync(conn, requestBody, timeoutMillis);
    }

    /*
     * @see liruonian.orion.Client#invokeAsync(java.lang.String,
     * liruonian.orion.remoting.RequestBody,
     * liruonian.orion.remoting.InvokeCallback, int)
     */
    @Override
    public void invokeAsync(String address, RequestBody requestBody,
            InvokeCallback callback, int timeoutMillis)
            throws RemotingException, InterruptedException {
        remoting.invokeAsync(address, requestBody, callback, timeoutMillis);
    }

    /*
     * @see liruonian.orion.Client#invokeAsync(liruonian.orion.remoting.Url,
     * liruonian.orion.remoting.RequestBody,
     * liruonian.orion.remoting.InvokeCallback, int)
     */
    @Override
    public void invokeAsync(Url url, RequestBody requestBody, InvokeCallback callback,
            int timeoutMillis) throws RemotingException, InterruptedException {
        remoting.invokeAsync(url, requestBody, callback, timeoutMillis);
    }

    /*
     * @see
     * liruonian.orion.Client#invokeAsync(liruonian.orion.remoting.Connection,
     * liruonian.orion.remoting.RequestBody,
     * liruonian.orion.remoting.InvokeCallback, int)
     */
    @Override
    public void invokeAsync(Connection conn, RequestBody requestBody,
            InvokeCallback callback, int timeoutMillis)
            throws RemotingException, InterruptedException {
        remoting.invokeAsync(conn, requestBody, callback, timeoutMillis);
    }

}
