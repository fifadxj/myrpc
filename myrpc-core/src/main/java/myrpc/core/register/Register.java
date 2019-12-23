package myrpc.core.register;

import java.util.List;

public interface Register {
    List<Node> discover(Class clazz);

    void register(Class clazz, Node node);
}
