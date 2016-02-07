package com.github.mkopylec.sessioncouchbase.persistent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.session.SessionRepository;

import java.io.IOException;
import java.util.Map;

import static java.lang.System.currentTimeMillis;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;
import static org.springframework.util.Base64Utils.decodeFromString;
import static org.springframework.util.Base64Utils.encodeToString;
import static org.springframework.util.SerializationUtils.deserialize;
import static org.springframework.util.SerializationUtils.serialize;

public class CouchbaseSessionRepository implements SessionRepository<CouchbaseSession> {

    protected static final String SESSION_KEY_SEPARATOR = "###";

    protected final CouchbaseDao dao;
    protected final ObjectMapper mapper;
    protected final boolean jsonSerialization;
    protected final String namespace;
    protected final int sessionTimeout;

    public CouchbaseSessionRepository(CouchbaseDao dao, String namespace, ObjectMapper mapper, boolean jsonSerialization, int sessionTimeout) {
        notNull(dao, "Missing couchbase data access object");
        notNull(mapper, "Missing JSON object mapper");
        hasText(namespace, "Empty HTTP session namespace");
        this.dao = dao;
        this.mapper = mapper;
        this.jsonSerialization = jsonSerialization;
        this.namespace = namespace.trim();
        this.sessionTimeout = sessionTimeout;
    }

    @Override
    public CouchbaseSession createSession() {
        return new CouchbaseSession(sessionTimeout);
    }

    @Override
    public void save(CouchbaseSession session) {
        String namespaceSessionKey = getNamespaceSessionKey(session.getId());
        String namespaceAttributes = serializeSessionAttributes(session.getNamespaceAttributes());
        SessionEntity namespaceEntity = new SessionEntity(namespaceSessionKey, namespaceAttributes);

        String globalAttributes = serializeSessionAttributes(session.getGlobalAttributes());
        SessionEntity globalEntity = new SessionEntity(session.getId(), globalAttributes);

        dao.save(namespaceEntity);
        dao.save(globalEntity);
    }

    @Override
    public CouchbaseSession getSession(String id) {
        SessionEntity globalEntity = dao.findOne(id);
        SessionEntity namespaceEntity = dao.findOne(getNamespaceSessionKey(id));

        if (globalEntity == null && namespaceEntity == null) {
            return null;
        }

        notNull(globalEntity, "Invalid state of HTTP session persisted in couchbase. Missing global data.");
        notNull(namespaceEntity, "Invalid state of HTTP session persisted in couchbase. Missing local namespace data.");

        Map<String, Object> globalAttributes = deserializeSessionAttributes(globalEntity.getSessionAttributes());
        Map<String, Object> namespaceAttributes = deserializeSessionAttributes(namespaceEntity.getSessionAttributes());
        CouchbaseSession session = new CouchbaseSession(globalAttributes, namespaceAttributes);
        if (session.isExpired()) {
            delete(id);
            return null;
        }
        session.setLastAccessedTime(currentTimeMillis());

        return session;
    }

    @Override
    public void delete(String id) {
        dao.delete(getNamespaceSessionKey(id));
        dao.delete(id);
    }

    protected String getNamespaceSessionKey(String id) {
        return id + SESSION_KEY_SEPARATOR + namespace;
    }

    protected String serializeSessionAttributes(Map<String, Object> attributes) {
        if (jsonSerialization) {
            try {
                return mapper.writeValueAsString(attributes);
            } catch (JsonProcessingException ex) {
                throw new IllegalStateException("Error serializing HTTP session attributes to JSON", ex);
            }
        }
        return encodeToString(serialize(attributes));
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> deserializeSessionAttributes(String attributes) {
        if (jsonSerialization) {
            try {
                return mapper.readValue(attributes, Map.class);
            } catch (IOException ex) {
                throw new IllegalStateException("Error deserializing HTTP session attributes from JSON", ex);
            }
        }
        return (Map<String, Object>) deserialize(decodeFromString(attributes));
    }
}
