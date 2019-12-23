package myrpc.core.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class RpcInvocation implements Serializable {
    private String methodName;
    private Class[] parameterTypes;
    private Object[] arguments;
    private Class serviceClazz;
    private int timeout = 1000;
}