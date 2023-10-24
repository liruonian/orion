package liruonian.orion.client.proxy.jdk;

import liruonian.orion.client.Client;
import liruonian.orion.client.ProxyInvoker;
import liruonian.orion.remoting.RemotingException;
import liruonian.orion.remoting.message.RequestBody;
import liruonian.orion.remoting.message.RpcResponse;

public class JdkProxyInvoker implements ProxyInvoker {

    private String address;
    private Client client;

    public JdkProxyInvoker(String addresss, Client client) {
        this.address = addresss;
        this.client = client;
    }

    @Override
    public RpcResponse invoke(RequestBody requestBody) throws InterruptedException, RemotingException {
        try {
            return client.invokeAsync(address, requestBody, 5000).waitResponse();
        } catch (InterruptedException | RemotingException e) {
            throw e;
        }
    }

}
