package myrpc.core.register;

import myrpc.core.common.RpcException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class AbstractRegister implements Register {
    //private Map<Class, Object> localImplRefs = new HashMap<>();
    protected Map<Class, List<Node>> registerCache = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);

    @Override
    public List<Node> discover(Class clazz) {
        List<Node> nodes = registerCache.get(clazz);
        if (nodes == null) {
            try {
                nodes = doDiscover(clazz);
            } catch (Exception e) {
                throw new RpcException(e);
            }
            registerCache.put(clazz, nodes);
        }

        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                List<Node> latestNodes = AbstractRegister.this.doDiscover(clazz);
                registerCache.put(clazz, latestNodes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 10, TimeUnit.SECONDS);

        return nodes;
    }

    protected abstract List<Node> doDiscover(Class clazz) throws Exception;

    @Override
    public void register(Class clazz, Node node) {
        try {
            doRegister(clazz, node);
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }

    protected abstract void doRegister(Class clazz, Node node) throws Exception;
}
