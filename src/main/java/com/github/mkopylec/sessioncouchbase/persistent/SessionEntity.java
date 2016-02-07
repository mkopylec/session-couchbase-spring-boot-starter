package com.github.mkopylec.sessioncouchbase.persistent;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.Field;
import org.springframework.session.MapSession;

import static org.springframework.util.Base64Utils.decodeFromString;
import static org.springframework.util.Base64Utils.encodeToString;
import static org.springframework.util.SerializationUtils.deserialize;
import static org.springframework.util.SerializationUtils.serialize;

@Document
public class SessionEntity {

    @Id
    protected String key;
    @Field
    protected String session;
    @Transient
    protected MapSession couchbaseSession;

    public SessionEntity(String key, MapSession session) {
        this.key = key;
        this.session = encodeToString(serialize(session));
    }

    public String getKey() {
        return key;
    }

    public MapSession getSession() {
        if (couchbaseSession == null) {
            couchbaseSession = (MapSession) deserialize(decodeFromString(session));
        }
        return couchbaseSession;
    }

    protected SessionEntity() {
    }
}
