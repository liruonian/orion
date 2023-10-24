package liruonian.orion.client.proxy;

import liruonian.orion.client.Client;
import liruonian.orion.client.Proxy;
import liruonian.orion.client.ProxyInvoker;
import liruonian.orion.client.proxy.jdk.JdkProxy;
import liruonian.orion.client.proxy.jdk.JdkProxyInvoker;

public class ClientProxy {

    public static <T> T  getProxy(String address, Class<T> interfaceClass, Client client) {
        Proxy proxy = new JdkProxy();
        ProxyInvoker proxyInvoker = new JdkProxyInvoker(address, client);

        return proxy.getProxy(interfaceClass, proxyInvoker);
    }
}
