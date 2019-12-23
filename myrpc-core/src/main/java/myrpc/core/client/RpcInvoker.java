package myrpc.core.client;

import myrpc.core.common.RpcResult;
import myrpc.core.common.RpcInvocation;

public interface RpcInvoker {
    RpcResult invoke(RpcInvocation invocation);
}