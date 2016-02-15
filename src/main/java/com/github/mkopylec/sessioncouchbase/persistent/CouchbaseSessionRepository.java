package com.github.mkopylec.sessioncouchbase.persistent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.session.SessionRepository;

import java.util.Map;
import java.util.Map.Entry;

import static java.lang.System.currentTimeMillis;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;
import static org.springframework.util.Base64Utils.decodeFromString;
import static org.springframework.util.Base64Utils.encodeToString;
import static org.springframework.util.ClassUtils.isPrimitiveOrWrapper;
import static org.springframework.util.SerializationUtils.deserialize;
import static org.springframework.util.SerializationUtils.serialize;

public class CouchbaseSessionRepository implements SessionRepository<CouchbaseSession> {

    protected static final String SESSION_KEY_SEPARATOR = "###";
    protected static final String SERIALIZED_OBJECT_PREFIX = "_$object=";

    protected final CouchbaseDao dao;
    protected final ObjectMapper mapper;
    protected final String namespace;
    protected final int sessionTimeout;

    public CouchbaseSessionRepository(CouchbaseDao dao, String namespace, ObjectMapper mapper, int sessionTimeout) {
        notNull(dao, "Missing couchbase data access object");
        notNull(mapper, "Missing JSON object mapper");
        hasText(namespace, "Empty HTTP session namespace");
        this.dao = dao;
        this.mapper = mapper;
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
        serializeSessionAttributes(session.getNamespaceAttributes());
        SessionEntity namespaceEntity = new SessionEntity(namespaceSessionKey, session.getNamespaceAttributes());

        serializeSessionAttributes(session.getGlobalAttributes());
        SessionEntity globalEntity = new SessionEntity(session.getId(), session.getGlobalAttributes());

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

        deserializeSessionAttributes(globalEntity.getSessionAttributes());
        Map<String, Object> namespaceAttributes = namespaceEntity == null ? null : namespaceEntity.getSessionAttributes();
        deserializeSessionAttributes(namespaceAttributes);
        CouchbaseSession session = new CouchbaseSession(id, globalEntity.getSessionAttributes(), namespaceAttributes);
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

    protected void serializeSessionAttributes(Map<String, Object> attributes) {
        for (Entry<String, Object> attribute : attributes.entrySet()) {
            if (isObject(attribute)) {
                String serializedAttribute = encodeToString(serialize(attribute.getValue()));
                attribute.setValue(SERIALIZED_OBJECT_PREFIX + serializedAttribute);
            }
        }
    }

    protected boolean isObject(Entry<String, Object> attribute) {
        return !isPrimitiveOrWrapper(attribute.getValue().getClass());
    }

    protected void deserializeSessionAttributes(Map<String, Object> attributes) {
        if (attributes != null) {
            for (Entry<String, Object> attribute : attributes.entrySet()) {
                if (isSerializedObject(attribute)) {
                    String content = removeStart(attribute.getValue().toString(), SERIALIZED_OBJECT_PREFIX);
                    Object deserializedAttribute = deserialize(decodeFromString(content));
                    attribute.setValue(deserializedAttribute);
                }
            }
        }
    }

    protected boolean isSerializedObject(Entry<String, Object> attribute) {
        return attribute.getValue() != null && attribute.getValue() instanceof String && startsWith(attribute.getValue().toString(), SERIALIZED_OBJECT_PREFIX);
    }
}
