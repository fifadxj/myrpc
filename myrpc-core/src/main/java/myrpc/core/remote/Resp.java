package myrpc.core.remote;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Resp {
    private Head head;
    private byte[] body;
}
