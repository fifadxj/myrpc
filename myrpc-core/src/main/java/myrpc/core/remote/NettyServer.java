package myrpc.core.remote;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.concurrent.ImmediateEventExecutor;
import lombok.extern.slf4j.Slf4j;
import myrpc.core.serialize.JdkSerializer;
import myrpc.core.serialize.Serializer;
import myrpc.core.common.RpcException;
import myrpc.core.common.RpcInvocation;
import myrpc.core.common.RpcResult;
import myrpc.core.server.RpcExecutor;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Slf4j
public class NettyServer {
    private int port;
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    public NettyServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new RespEncoder());
                        ch.pipeline().addLast(new ReqDecoder());
                        ch.pipeline().addLast(new ReqProcessor());
                    }
                })
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture f = b.bind(port).sync();

        log.info("Start nio server successfully, listen to " + port);
    }


    public void stop() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        log.info("Stop nio server successfully");
    }
}

@Slf4j
class ReqProcessor extends ChannelInboundHandlerAdapter {
    private final ExecutorService serviceExecutor = ImmediateEventExecutor.INSTANCE;
    private Serializer serializer = new JdkSerializer();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Req req = (Req) msg;
        int type = req.getHead().getType();
        long reqId = req.getHead().getReqId();
        byte[] reqBody = req.getBody();

        if (type == Head.RPC_CALL) {
            RpcInvocation invocation = serializer.deserialize(reqBody, RpcInvocation.class);
            RpcExecutor rpcExecutor = RpcExecutor.findRpcExecutor(invocation.getServiceClazz());
            RpcResult rpcResult = rpcExecutor.execute(invocation);
            byte[] respBody = serializer.serialize(rpcResult);

            Resp resp = new Resp();
            Head head = new Head();
            head.setType(type);
            head.setReqId(reqId);
            resp.setHead(head);
            resp.setBody(respBody);
            ChannelFuture writeFuture = ctx.channel().writeAndFlush(resp);

            writeFuture.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future)
                        throws Exception {
                    if (future.isSuccess()) {
                        return;
                    }
                    log.error("Write response failed, reqId:{}, cause:{}", head.getReqId(), future.cause());
                }
            });
        } else if (type == Head.HEART_BEAT) {

        } else {
            throw new RpcException("invalid request type");
        }
    }
}

class ReqDecoder extends ByteToMessageDecoder {
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        if (buf.readableBytes() < 4) {
            return;
        }

        buf.markReaderIndex();

        int length = buf.readInt();

        if (buf.readableBytes() < length) {
            buf.resetReaderIndex();
            return;
        }

        long reqId = buf.readLong();
        int type = buf.readInt();
        byte[] b = new byte[length - 8 - 4];
        buf.readBytes(b);

        Req req = new Req();
        Head head = new Head();
        head.setReqId(reqId);
        head.setType(type);
        req.setHead(head);
        req.setBody(b);

        out.add(req);
    }
}

class RespEncoder extends MessageToByteEncoder<Resp> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Resp msg, ByteBuf out) throws Exception {
        out.writeInt(8 + 4 + msg.getBody().length);
        out.writeLong(msg.getHead().getReqId());
        out.writeInt(msg.getHead().getType());
        out.writeBytes(msg.getBody());
    }
}