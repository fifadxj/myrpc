package myrpc.core.server;

import myrpc.core.register.Node;
import myrpc.core.remote.NettyServer;
import myrpc.core.register.Register;

import java.net.InetAddress;

public class RpcServer<T> {
    private Class<T> clazz;
    private Register register;
    private int port;
    private T target;
    private static volatile boolean running = false;
    private NettyServer server;

    public void export() throws Exception {
        String ip = InetAddress.getLocalHost().getHostAddress();
        Node service = new Node(ip, port);
        register.register(clazz, service);

        RpcExecutor.exportRpcExecutor(clazz, target);

        if (! running) {
            server = new NettyServer(port);
            server.start();
        }
    }

    public void setInterface(Class<T> clazz) {
        this.clazz = clazz;
    }

    public void setRegister(Register register) {
        this.register = register;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setTarget(T target) {
        this.target = target;
    }

}
