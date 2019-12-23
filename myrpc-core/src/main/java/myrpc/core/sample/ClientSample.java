package myrpc.core.sample;

import myrpc.core.register.ZookeeperRegister;
import myrpc.core.serialize.JsonUtils;
import myrpc.core.client.RpcClient;

public class ClientSample {
    public static void main(String[] args) {
        RpcClient<UserApi> rpcClient = new RpcClient<>();
        rpcClient.setInterface(UserApi.class);
        ZookeeperRegister register = new ZookeeperRegister("47.96.159.210", 2181);
        rpcClient.setRegister(register);
        UserApi userApi = rpcClient.getStubProxy();

        for (int i = 0; i < 10; i++) {
            User user = userApi.login("hello", "world");
            System.out.println(JsonUtils.toJson(user));
        }
    }
}
