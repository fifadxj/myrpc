package myrpc.core.register;

import com.google.common.primitives.Ints;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ZookeeperRegister extends AbstractRegister {
    private CuratorFramework curatorClient;

    public ZookeeperRegister(String ip, int port) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        curatorClient = CuratorFrameworkFactory.builder()
                .sessionTimeoutMs(10 * 1000)
                .retryPolicy(retryPolicy)
                .connectString(ip + ":" + port)
                .build();
        curatorClient.start();
    }

    protected List<Node> doDiscover(Class clazz) throws Exception {
        List<Node> nodes;
        String providerPath = "/myrpc/" + clazz.getName() + "/providers";
        Stat stat = curatorClient.checkExists().forPath(providerPath);

        if (stat == null) {
            return new ArrayList<>();
        }

        List<String> nodesString = curatorClient.getChildren().forPath(providerPath);
        nodes = nodesString.stream()
            .map((t) -> new Node(t.split(":")[0], Ints.tryParse(t.split(":")[1])))
            .collect(Collectors.toList());
        return nodes;
    }

    protected void doRegister(Class clazz, Node node) throws Exception {
        String servicePath = "/myrpc/" + clazz.getName();
        Stat stat = curatorClient.checkExists().forPath(servicePath);
        if (stat == null) {
            curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(servicePath);
        }

        String providerPath = servicePath + "/providers/" + node.getIp() + ":" + node.getPort();
        Stat stat2 = curatorClient.checkExists().forPath(providerPath);
        if (stat2 == null) {
            curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(providerPath);
        }
    }

    public static void main(String[] args) throws Exception {
        ZookeeperRegister register = new ZookeeperRegister("47.96.159.210", 2181);
        register.register(Integer.class, new Node("127.0.0.1", 9999));
        register.discover(Integer.class);
    }
}
