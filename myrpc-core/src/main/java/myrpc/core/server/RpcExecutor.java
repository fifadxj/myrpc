package myrpc.core.server;

import lombok.Getter;
import lombok.Setter;
import myrpc.core.common.RpcException;
import myrpc.core.common.RpcInvocation;
import myrpc.core.common.RpcResult;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class RpcExecutor<T> {
    private static Map<Class<?>, RpcExecutor<?>> rpcExecutorMap = new ConcurrentHashMap<>();
    private T target;
    public RpcResult execute(RpcInvocation invocation) throws Exception {
        Method method = target.getClass().getMethod(invocation.getMethodName(), invocation.getParameterTypes());

        RpcResult result = new RpcResult();
        try {
            Object returnObject = method.invoke(target, invocation.getArguments());
            result.setResult(returnObject);
        } catch (InvocationTargetException e) {
            result.setException(e.getTargetException());
        } catch (Exception e) {
            throw new RpcException(e);
        }

        return result;
    }

    public static RpcExecutor<?> findRpcExecutor(Class clazz) {
        return rpcExecutorMap.get(clazz);
    }

    public static RpcExecutor<?> exportRpcExecutor(Class clazz, Object target) {
        RpcExecutor rpcExecutor = new RpcExecutor();
        rpcExecutor.setTarget(target);
        rpcExecutorMap.put(clazz, rpcExecutor);

        return rpcExecutor;
    }
}
