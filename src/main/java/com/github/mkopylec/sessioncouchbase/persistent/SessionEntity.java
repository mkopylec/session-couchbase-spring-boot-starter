package com.github.mkopylec.sessioncouchbase.persistent;

import com.couchbase.client.java.repository.annotation.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

import java.util.Map;

@Document
public class SessionEntity {

    @Id
    protected String id;
    @Field
    protected Map<String, Map<String, Object>> data;

    public SessionEntity(String id, Map<String, Map<String, Object>> data) {
        this.id = id;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public Map<String, Map<String, Object>> getData() {
        return data;
    }
}
