package liruonian.orion.client;

import liruonian.orion.remoting.RemotingException;
import liruonian.orion.remoting.message.RequestBody;
import liruonian.orion.remoting.message.RpcResponse;

public interface ProxyInvoker {

    RpcResponse invoke(RequestBody request) throws InterruptedException, RemotingException;

}
