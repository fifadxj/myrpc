package myrpc.core.sample;

import myrpc.core.register.ZookeeperRegister;
import myrpc.core.server.RpcServer;

public class ServerSample {
    public static void main(String[] args) throws Exception {
        RpcServer<UserApi> rpcServer = new RpcServer<>();
        rpcServer.setInterface(UserApi.class);
        ZookeeperRegister register = new ZookeeperRegister("47.96.159.210", 2181);
        rpcServer.setRegister(register);
        rpcServer.setPort(9999);
        rpcServer.setTarget(new UserApiImpl());
        rpcServer.export();
        System.out.println("started");
    }
}
