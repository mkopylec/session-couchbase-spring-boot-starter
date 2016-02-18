package com.github.mkopylec.sessioncouchbase.persistent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.session.SessionRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static com.couchbase.client.java.document.json.JsonObject.from;
import static java.lang.System.currentTimeMillis;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;
import static org.springframework.util.Base64Utils.decodeFromString;
import static org.springframework.util.Base64Utils.encodeToString;
import static org.springframework.util.ClassUtils.isPrimitiveOrWrapper;
import static org.springframework.util.SerializationUtils.deserialize;
import static org.springframework.util.SerializationUtils.serialize;

public class CouchbaseSessionRepository implements SessionRepository<CouchbaseSession> {

    protected static final String SERIALIZED_OBJECT_PREFIX = "_$object=";
    protected static final String GLOBAL_NAMESPACE = "global";

    protected final CouchbaseDao dao;
    protected final ObjectMapper mapper;
    protected final String namespace;
    protected final int sessionTimeout;

    public CouchbaseSessionRepository(CouchbaseDao dao, String namespace, ObjectMapper mapper, int sessionTimeout) {
        notNull(dao, "Missing couchbase data access object");
        notNull(mapper, "Missing JSON object mapper");
        hasText(namespace, "Empty HTTP session namespace");
        isTrue(!namespace.equals(GLOBAL_NAMESPACE), "Forbidden HTTP session namespace '" + namespace + "'");
        this.dao = dao;
        this.mapper = mapper;
        this.namespace = namespace.trim();
        this.sessionTimeout = sessionTimeout;
    }

    @Override
    public CouchbaseSession createSession() {
        CouchbaseSession session = new CouchbaseSession(sessionTimeout);
        Map<String, Map<String, Object>> sessionData = new HashMap<>(2);
        sessionData.put(GLOBAL_NAMESPACE, session.getGlobalAttributes());
        sessionData.put(namespace, session.getNamespaceAttributes());
        SessionEntity sessionEntity = new SessionEntity(session.getId(), sessionData);
        dao.save(sessionEntity);
        return session;
    }

    @Override
    public void save(CouchbaseSession session) {
        serializeSessionAttributes(session.getGlobalAttributes());
        serializeSessionAttributes(session.getNamespaceAttributes());
        dao.updateSession(from(session.getGlobalAttributes()), GLOBAL_NAMESPACE, session.getId());
        dao.updateSession(from(session.getNamespaceAttributes()), namespace, session.getId());
    }

    @Override
    public CouchbaseSession getSession(String id) {
        Map<String, Object> globalAttributes = dao.findSessionAttributes(id, GLOBAL_NAMESPACE);
        Map<String, Object> namespaceAttributes = dao.findSessionAttributes(id, namespace);

        if (globalAttributes == null && namespaceAttributes == null) {
            return null;
        }

        notNull(globalAttributes, "Invalid state of HTTP session persisted in couchbase. Missing global attributes.");

        deserializeSessionAttributes(globalAttributes);
        deserializeSessionAttributes(namespaceAttributes);
        CouchbaseSession session = new CouchbaseSession(id, globalAttributes, namespaceAttributes);
        if (session.isExpired()) {
            delete(id);
            return null;
        }
        session.setLastAccessedTime(currentTimeMillis());

        return session;
    }

    @Override
    public void delete(String id) {
        dao.delete(id);
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
