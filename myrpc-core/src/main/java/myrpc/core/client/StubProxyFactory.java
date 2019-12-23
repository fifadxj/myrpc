package myrpc.core.client;

import java.lang.reflect.Proxy;

public class StubProxyFactory {
    public <T> T getProxy(RpcInvoker invoker, Class<T> _interface) {
        RpcInvokerInvocationHandler rpcInvokerInvocationHandler = new RpcInvokerInvocationHandler();
        rpcInvokerInvocationHandler.setRpcInvoker(invoker);
        rpcInvokerInvocationHandler.setServiceClazz(_interface);

        T proxy = (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] {_interface}, rpcInvokerInvocationHandler);

        return proxy;
    }
}
