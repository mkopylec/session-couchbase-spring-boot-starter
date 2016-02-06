package com.github.mkopylec.sessioncouchbase.persistent;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.Field;

import static org.springframework.util.Base64Utils.decodeFromString;
import static org.springframework.util.Base64Utils.encodeToString;
import static org.springframework.util.SerializationUtils.deserialize;
import static org.springframework.util.SerializationUtils.serialize;

@Document
public class SessionEntity {

    @Id
    protected String sessionId;
    @Field
    protected String session;
    @Transient
    protected CouchbaseSession couchbaseSession;

    public SessionEntity(String sessionId, CouchbaseSession session) {
        this.sessionId = sessionId;
        this.session = encodeToString(serialize(session));
    }

    public String getSessionId() {
        return sessionId;
    }

    public CouchbaseSession getSession() {
        if (couchbaseSession == null) {
            couchbaseSession = (CouchbaseSession) deserialize(decodeFromString(session));
        }
        return couchbaseSession;
    }

    protected SessionEntity() {
    }
}
