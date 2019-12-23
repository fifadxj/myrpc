package myrpc.core.remote;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Head {
    public static final int RPC_CALL = 1;
    public static final int HEART_BEAT = 0;
    private long reqId;
    private int type;
}
