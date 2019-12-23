package myrpc.core.serialize;

import com.google.common.base.Charsets;

public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object object) {
        String json = JsonUtils.toJson(object);
        return json.getBytes(Charsets.UTF_8);
    }

    @Override
    public <T> T deserialize(byte[] binary, Class<T> clazz) {
        String json = new String(binary, Charsets.UTF_8);
        T object = JsonUtils.fromJson(json, clazz);
        return object;
    }
}
