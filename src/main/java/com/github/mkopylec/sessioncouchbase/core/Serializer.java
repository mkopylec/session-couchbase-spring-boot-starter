package com.github.mkopylec.sessioncouchbase.core;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.springframework.util.Base64Utils.decodeFromString;
import static org.springframework.util.Base64Utils.encodeToString;
import static org.springframework.util.ClassUtils.isPrimitiveOrWrapper;
import static org.springframework.util.SerializationUtils.deserialize;
import static org.springframework.util.SerializationUtils.serialize;

public class Serializer {

    protected static final String SERIALIZED_OBJECT_PREFIX = "_$object=";

    public Map<String, Object> serializeSessionAttributes(Map<String, Object> attributes) {
        if (attributes == null) {
            return null;
        }
        Map<String, Object> serialized = new HashMap<>(attributes.size());
        attributes.forEach((name, value) -> {
            if (isDeserializedObject(value)) {
                Object attributeValue = encodeToString(serialize(value));
                serialized.put(name, SERIALIZED_OBJECT_PREFIX + attributeValue);
            } else {
                serialized.put(name, value);
            }
        });
        return serialized;
    }

    public Map<String, Object> deserializeSessionAttributes(Map<String, Object> attributes) {
        if (attributes == null) {
            return null;
        }
        Map<String, Object> deserialized = new HashMap<>(attributes.size());
        attributes.forEach((name, value) -> {
            Object attributeValue = value;
            if (isSerializedObject(value)) {
                String content = removeStart(value.toString(), SERIALIZED_OBJECT_PREFIX);
                attributeValue = deserialize(decodeFromString(content));
            }
            deserialized.put(name, attributeValue);
        });
        return deserialized;
    }

    protected boolean isDeserializedObject(Object attributeValue) {
        return attributeValue != null && !isPrimitiveOrWrapper(attributeValue.getClass()) && !(attributeValue instanceof String);
    }

    protected boolean isSerializedObject(Object attributeValue) {
        return attributeValue != null && attributeValue instanceof String && startsWith(attributeValue.toString(), SERIALIZED_OBJECT_PREFIX);
    }
}
