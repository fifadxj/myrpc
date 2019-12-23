package myrpc.core.remote;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class RpcFuture {
    private byte[] response;
    private boolean done = false;
    private int timeout;
    private List<Thread> waiters = new ArrayList<>();

    public RpcFuture(int timeout) {
        this.timeout = timeout;
    }

    public byte[] get() {
        return get(timeout);
    }

    public byte[] get(int timeout) {
        if (done) {
            return response;
        }

        waiters.add(Thread.currentThread());
        while (! done) {
            long nano = TimeUnit.MILLISECONDS.toNanos(timeout);
            LockSupport.parkNanos(this, nano);
        }

        return response;
    }

    public void set(byte[] response) {
        this.response = response;
        this.done = true;

        for (Thread thread : waiters) {
            LockSupport.unpark(thread);
        }
    }
}
