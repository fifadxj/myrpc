package myrpc.core.client;

import myrpc.core.register.Node;
import myrpc.core.serialize.JdkSerializer;
import myrpc.core.serialize.Serializer;
import myrpc.core.common.RpcInvocation;
import myrpc.core.common.RpcResult;
import myrpc.core.remote.NettyClient;

public class SingleNodeRpcInvoker implements RpcInvoker {
    private Node node;
    private NettyClient client;
    private Serializer serializer = new JdkSerializer();
    public SingleNodeRpcInvoker(Node node) {
        this.node = node;
        this.client = new NettyClient(node.getIp(), node.getPort());
        this.client.connect();
    }
    @Override
    public RpcResult invoke(RpcInvocation invocation) {
        byte[] reqBody = serializer.serialize(invocation);
        byte[] respBody = client.send(reqBody, invocation.getTimeout());
        RpcResult rpcResult = (RpcResult) serializer.deserialize(respBody, RpcResult.class);

        return rpcResult;
    }
}
