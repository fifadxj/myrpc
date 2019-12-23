package myrpc.core.sample;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class User implements Serializable {
    private String username;
    private String uid;
    private String phone;
}
