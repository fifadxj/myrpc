package myrpc.core.client;

import lombok.Getter;
import lombok.Setter;
import myrpc.core.loadbalance.LoadBalancer;
import myrpc.core.common.RpcInvocation;
import myrpc.core.common.RpcResult;

import java.util.List;

@Getter
@Setter
public class ClusterRpcInvoker implements RpcInvoker {
    private LoadBalancer loadBalancer;
    private List<SingleNodeRpcInvoker> invokers;

    public ClusterRpcInvoker(List<SingleNodeRpcInvoker> invokers) {
        this.invokers = invokers;
    }
    @Override
    public RpcResult invoke(RpcInvocation invocation) {
        SingleNodeRpcInvoker invoker = loadBalancer.choose(invokers);
        RpcResult result = invoker.invoke(invocation);

        return result;
    }
}
