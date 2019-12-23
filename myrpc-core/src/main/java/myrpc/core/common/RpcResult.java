package myrpc.core.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class RpcResult implements Serializable {
    private Object result;
    private Throwable exception;
    private Class returnClazz;
}