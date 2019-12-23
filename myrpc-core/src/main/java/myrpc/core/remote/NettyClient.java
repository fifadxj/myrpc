package myrpc.core.remote;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class NettyClient {
    private Map<Long, RpcFuture> waitingMap = new ConcurrentHashMap<Long, RpcFuture>();
    private String ip;
    private int port;
    private int readTimeout = 5;
    private static AtomicLong reqIdGenerator = new AtomicLong(1);
    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private Channel channel;
    private Bootstrap bootstrap;

    public NettyClient(String ip, int port) {
        this.ip = ip;
        this.port = port;

        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ReqEncoder());
                ch.pipeline().addLast(new RespDecoder());
                ch.pipeline().addLast(new RespProcessor(waitingMap));
            }
        });
    }

    public boolean connect() {
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(ip, port));
        channel = future.awaitUninterruptibly().channel();

        if (!future.isSuccess()) {
            log.error("Connect to " + ip + ":" + port + " failed.",
                    future.cause());
            return false;
        } else {
            log.info("Connect to " + ip + ":" + port + " successfully.");
            return true;
        }
    }

    public void setReadTimeout(int timeout) {
        this.readTimeout = timeout;
    }

    public byte[] send(byte[] msg, int timeout) {
        Req req = new Req();
        Head head = new Head();
        head.setType(Head.RPC_CALL);
        head.setReqId(reqIdGenerator.getAndIncrement());
        req.setHead(head);
        req.setBody(msg);
        ChannelFuture writeFuture = channel.writeAndFlush(req);
        RpcFuture future = new RpcFuture(timeout);
        waitingMap.put(head.getReqId(), future);
        byte[] respBody = future.get();

        return respBody;
    }

    public void heartBeat() {

    }
}

@Slf4j
class RespProcessor extends ChannelInboundHandlerAdapter {
    private Map<Long, RpcFuture> waitingMap;
    public RespProcessor(Map<Long, RpcFuture> waitingMap) {
        this.waitingMap = waitingMap;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Resp resp = (Resp) msg;
        long reqId = resp.getHead().getReqId();
        int reqType = resp.getHead().getType();
        RpcFuture future = waitingMap.get(reqId);
        if (future != null) {
            future.set(resp.getBody());
        } else {
            log.warn("Late arrived response, reqType:{}, reqId:{}", reqType, reqId);
        }

    }
}

class RespDecoder extends ByteToMessageDecoder {
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

        Resp resp = new Resp();
        Head head = new Head();
        head.setReqId(reqId);
        head.setType(type);
        resp.setHead(head);
        resp.setBody(b);

        out.add(resp);
    }
}

class ReqEncoder extends MessageToByteEncoder<Req> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Req msg, ByteBuf out) throws Exception {
        out.writeInt(8 + 4 + msg.getBody().length);
        out.writeLong(msg.getHead().getReqId());
        out.writeInt(msg.getHead().getType());
        out.writeBytes(msg.getBody());
    }
}

