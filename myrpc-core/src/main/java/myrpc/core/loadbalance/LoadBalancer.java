package myrpc.core.loadbalance;

import myrpc.core.client.SingleNodeRpcInvoker;

import java.util.List;

public interface LoadBalancer {
    SingleNodeRpcInvoker choose(List<SingleNodeRpcInvoker> candidates);
}
