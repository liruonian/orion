package liruonian.orion.client;

public interface Proxy {

    <T> T getProxy(Class<T> interfaceClass, ProxyInvoker proxyInvoker);

}
