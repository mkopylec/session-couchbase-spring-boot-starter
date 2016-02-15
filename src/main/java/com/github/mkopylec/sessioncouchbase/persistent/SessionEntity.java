package com.github.mkopylec.sessioncouchbase.persistent;

import com.couchbase.client.java.repository.annotation.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

import java.util.Map;

@Document
public class SessionEntity {

    @Id
    protected String key;
    @Field
    protected Map<String, Object> sessionAttributes;

    public SessionEntity(String key, Map<String, Object> sessionAttributes) {
        this.key = key;
        this.sessionAttributes = sessionAttributes;
    }

    public Map<String, Object> getSessionAttributes() {
        return sessionAttributes;
    }
}
