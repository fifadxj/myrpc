package myrpc.core.remote;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Req {
    private Head head;
    private byte[] body;
}
