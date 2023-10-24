package liruonian.orion.client.proxy.jdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import liruonian.orion.core.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import liruonian.orion.client.Constants;
import liruonian.orion.client.ProxyInvoker;
import liruonian.orion.client.ServiceInvocation;
import liruonian.orion.commons.StringManager;
import liruonian.orion.remoting.message.RequestBody;
import liruonian.orion.remoting.message.RpcResponse;

public class JdkInvocationHandler<T> implements InvocationHandler {

    private static final Logger logger = LoggerFactory
            .getLogger(JdkInvocationHandler.class);
    private static final StringManager sm = StringManager.getManager(Constants.PACAKGE);

    private Class<T> proxyClass;
    private ProxyInvoker proxyInvoker;

    public JdkInvocationHandler(Class<T> proxyClass, ProxyInvoker proxyInvoker) {
        this.proxyClass = proxyClass;
        this.proxyInvoker = proxyInvoker;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        ServiceInvocation service = method.getAnnotation(ServiceInvocation.class);
        if (service == null) {
            if (logger.isWarnEnabled()) {
                logger.warn(sm.getString("JdkInvocationHandler.invoke.annotationAbsent",
                        proxyClass.getName(), method.getName()));
            }
            return null;
        }

        RequestBody requestBody = new RequestBody();
        requestBody.setServiceName(service.name());
        requestBody.setParameters(args);

        RpcResponse rpcResponse = proxyInvoker.invoke(requestBody);
        if (rpcResponse.getResponseBody() instanceof  Throwable) {
            throw new ServerException((Throwable) rpcResponse.getResponseBody());
        }

        return rpcResponse.getResponseBody();
    }

}
