package myrpc.core.client;

import myrpc.core.loadbalance.LoadBalancer;
import myrpc.core.loadbalance.RoundRobinLoadBalancer;
import myrpc.core.register.Node;
import myrpc.core.register.Register;
import myrpc.core.common.RpcException;

import java.util.List;
import java.util.stream.Collectors;

public class RpcClient<T> {
    private StubProxyFactory stubProxyFactory = new StubProxyFactory();
    private Class<T> clazz;
    private Register register;
    private LoadBalancer loadBalancer;
    private T stub;

    public T getStubProxy() {
        if (stub == null) {
            init();
        }

        return stub;
    }

    private void init() {
        List<Node> nodes = register.discover(clazz);
        if (nodes.size() == 0) {
            throw new RpcException("no avaliable server");
        }
        List<SingleNodeRpcInvoker> invokers = nodes.stream().map((node) -> new SingleNodeRpcInvoker(node)).collect(Collectors.toList());
        ClusterRpcInvoker clusterRpcInvoker = new ClusterRpcInvoker(invokers);

        if (loadBalancer == null) {
            loadBalancer = new RoundRobinLoadBalancer();
        }
        clusterRpcInvoker.setLoadBalancer(loadBalancer);
        stub = stubProxyFactory.getProxy(clusterRpcInvoker, clazz);
    }

    public void setInterface(Class<T> clazz) {
        this.clazz = clazz;
    }

    public void setRegister(Register register) {
        this.register = register;
    }

    public void setLoadBalancer(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }
}
