package liruonian.orion.remoting;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.netty.util.Timeout;
import liruonian.orion.remoting.message.MessageFactory;
import liruonian.orion.remoting.message.RpcResponse;

/**
 * Default invoke future.
 *
 * @author lihao
 * @date 2020年8月11日
 * @version 1.0
 */
public class DefaultInvokeFuture implements InvokeFuture {

    private volatile RpcResponse rpcResponse;
    private long invokeId;
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    private MessageFactory messageFactory;
    private Timeout timeout;
    private InvokeCallback callback;

    public DefaultInvokeFuture(long invokeId, MessageFactory messageFactory) {
        this.invokeId = invokeId;
        this.messageFactory = messageFactory;
    }

    public DefaultInvokeFuture(long invokeId, MessageFactory messageFactory,
            InvokeCallback callback) {
        this.invokeId = invokeId;
        this.messageFactory = messageFactory;
        this.callback = callback;
    }

    /*
     * @see liruonian.orion.InvokeFuture#invokeId()
     */
    @Override
    public long invokeId() {
        return invokeId;
    }

    /*
     * @see liruonian.orion.InvokeFuture#waitResponse()
     */
    @Override
    public RpcResponse waitResponse() throws InterruptedException {
        countDownLatch.await();
        return rpcResponse;
    }

    /*
     * @see liruonian.orion.InvokeFuture#waitResponse(long)
     */
    @Override
    public RpcResponse waitResponse(long timeoutMillis) throws InterruptedException {
        countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        return rpcResponse;
    }

    /*
     * @see liruonian.orion.InvokeFuture#putResponse(liruonian.orion.remoting)
     */
    @Override
    public void putResponse(RpcResponse response) {
        this.rpcResponse = response;
        countDownLatch.countDown();
    }

    /*
     * @see liruonian.orion.InvokeFuture#isDone()
     */
    @Override
    public boolean isDone() {
        return countDownLatch.getCount() <= 0;
    }

    /*
     * @see liruonian.orion.InvokeFuture#createConnectionClosedResponse(java.net.
     * InetSocketAddress)
     */
    @Override
    public RpcResponse createConnectionClosedResponse(InetSocketAddress remoteAddress) {
        return messageFactory.createConnectionClosedResponse(remoteAddress, null);
    }

    /*
     * @see liruonian.orion.InvokeFuture#addTimeout(io.netty.util.Timeout)
     */
    @Override
    public void addTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

    /*
     * @see liruonian.orion.InvokeFuture#cancelTimeout()
     */
    @Override
    public void cancelTimeout() {
        if (timeout != null) {
            timeout.cancel();
        }
    }

    /*
     * @see liruonian.orion.remoting.InvokeFuture#executeInvokeCallback()
     */
    @Override
    public void executeInvokeCallback() {
        if (callback != null) {
            callback.performCallback(this);
        }
    }

}
