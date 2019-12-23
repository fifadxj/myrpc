package myrpc.core.common;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class RpcException extends RuntimeException {
    private String code;

    public RpcException(String code) {
        super();
        this.code = code;
    }

    public RpcException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public RpcException(String code, String message) {
        super(message);
        this.code = code;
    }

    public RpcException(String code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public RpcException(Throwable cause) {
        super(cause);
    }
}