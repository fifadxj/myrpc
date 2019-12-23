package myrpc.core.serialize;

import myrpc.core.common.RpcException;

import java.io.*;

public class JdkSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        ByteArrayOutputStream bout = null;
        try {
            bout = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bout);
            out.writeObject(object);
        } catch (Exception e) {
            throw new RpcException(e);
        }

        return bout.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] binary, Class<T> clazz) {
        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(binary));
            return (T) in.readObject();
        } catch (Exception e) {
            throw new RpcException(e);
        }
    }
}
