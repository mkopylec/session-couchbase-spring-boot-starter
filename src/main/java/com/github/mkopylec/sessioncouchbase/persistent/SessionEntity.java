package com.github.mkopylec.sessioncouchbase.persistent;

import com.couchbase.client.java.repository.annotation.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

@Document
public class SessionEntity {

    @Id
    protected String key;
    @Field
    protected String sessionAttributes;

    public SessionEntity(String key, String sessionAttributes) {
        this.key = key;
        this.sessionAttributes = sessionAttributes;
    }

    public String getSessionAttributes() {
        return sessionAttributes;
    }
}
