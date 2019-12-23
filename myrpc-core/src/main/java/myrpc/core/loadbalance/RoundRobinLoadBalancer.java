package myrpc.core.loadbalance;

import myrpc.core.register.Node;
import myrpc.core.client.SingleNodeRpcInvoker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer implements LoadBalancer {
    private AtomicInteger index = new AtomicInteger(0);

    public SingleNodeRpcInvoker choose(List<SingleNodeRpcInvoker> candidates) {
        for (; ; ) {
            int current = index.get();
            int next = (current == candidates.size() - 1 ? 0 : current + 1);
            if (index.compareAndSet(current, next)) {
                return candidates.get(current);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        RoundRobinLoadBalancer loadBalancer = new RoundRobinLoadBalancer();
        List<SingleNodeRpcInvoker> nodes = new ArrayList<SingleNodeRpcInvoker>();
        nodes.add(new SingleNodeRpcInvoker(new Node("1.1.1.1", 1)));
        nodes.add(new SingleNodeRpcInvoker(new Node("2.2.2.2", 2)));
        nodes.add(new SingleNodeRpcInvoker(new Node("3.3.3.3", 3)));
        nodes.add(new SingleNodeRpcInvoker(new Node("4.4.4.4", 4)));
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        AtomicInteger i1 = new AtomicInteger(0);
        AtomicInteger i2 = new AtomicInteger(0);
        AtomicInteger i3 = new AtomicInteger(0);
        AtomicInteger i4 = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(10000);
        for (int i = 0; i < 10000; i++) {
            executorService.execute(() -> {
                SingleNodeRpcInvoker node = loadBalancer.choose(nodes);
//                if (node.getIp().contains("1")) {
//                    i1.incrementAndGet();
//                }
//                if (node.getIp().contains("2")) {
//                    i2.incrementAndGet();
//                }
//                if (node.getIp().contains("3")) {
//                    i3.incrementAndGet();
//                }
//                if (node.getIp().contains("4")) {
//                    i4.incrementAndGet();
//                }
                latch.countDown();
            });
        }

        latch.await();
        System.out.println(i1.get() + " " + i2.get() + " " + i3.get() + " " + i4.get());
        executorService.shutdown();
    }
}
