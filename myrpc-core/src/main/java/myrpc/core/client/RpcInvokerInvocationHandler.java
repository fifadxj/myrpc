package myrpc.core.client;

import lombok.Getter;
import lombok.Setter;
import myrpc.core.common.RpcException;
import myrpc.core.common.RpcResult;
import myrpc.core.common.RpcInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@Getter
@Setter
public class RpcInvokerInvocationHandler implements InvocationHandler {
    private RpcInvoker rpcInvoker;
    private Class serviceClazz;

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(rpcInvoker, args);
        }
        RpcInvocation rpcInvocation = new RpcInvocation();
        rpcInvocation.setArguments(args);
        rpcInvocation.setMethodName(methodName);
        rpcInvocation.setParameterTypes(parameterTypes);
        rpcInvocation.setServiceClazz(serviceClazz);

        RpcResult rpcResult = null;
        try {
            rpcResult = rpcInvoker.invoke(rpcInvocation);
        } catch (Exception e) {
            throw new RpcException(e.getMessage(), e);
        }

        if (rpcResult.getException() != null) {
            throw rpcResult.getException();
        } else {
            return rpcResult.getResult();
        }
    }

}