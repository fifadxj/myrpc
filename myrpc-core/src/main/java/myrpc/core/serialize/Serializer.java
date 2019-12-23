package myrpc.core.serialize;

public interface Serializer {
    byte[] serialize(Object object);
    <T> T deserialize(byte[] binary, Class<T> clazz);
}
