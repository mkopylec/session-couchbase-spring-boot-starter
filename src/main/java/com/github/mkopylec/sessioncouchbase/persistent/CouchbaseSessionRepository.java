package com.github.mkopylec.sessioncouchbase.persistent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.session.FindByIndexNameSessionRepository;

import java.util.HashMap;
import java.util.Map;

import static com.couchbase.client.java.document.json.JsonObject.from;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

public class CouchbaseSessionRepository implements FindByIndexNameSessionRepository<CouchbaseSession> {

    protected static final String GLOBAL_NAMESPACE = "global";
    protected static final int SESSION_DOCUMENT_EXPIRATION_DELAY_IN_SECONDS = 60;

    private static final Logger log = getLogger(CouchbaseSessionRepository.class);

    protected final CouchbaseDao dao;
    protected final ObjectMapper mapper;
    protected final String namespace;
    protected final int sessionTimeout;
    protected final Serializer serializer;

    public CouchbaseSessionRepository(CouchbaseDao dao, String namespace, ObjectMapper mapper, int sessionTimeout, Serializer serializer) {
        notNull(dao, "Missing couchbase data access object");
        notNull(mapper, "Missing JSON object mapper");
        hasText(namespace, "Empty HTTP session namespace");
        isTrue(!namespace.equals(GLOBAL_NAMESPACE), "Forbidden HTTP session namespace '" + namespace + "'");
        notNull(serializer, "Missing object serializer");
        this.dao = dao;
        this.mapper = mapper;
        this.namespace = namespace.trim();
        this.sessionTimeout = sessionTimeout;
        this.serializer = serializer;
    }

    @Override
    public CouchbaseSession createSession() {
        CouchbaseSession session = new CouchbaseSession(sessionTimeout);
        Map<String, Map<String, Object>> sessionData = new HashMap<>(2);
        sessionData.put(GLOBAL_NAMESPACE, session.getGlobalAttributes());
        sessionData.put(namespace, session.getNamespaceAttributes());
        SessionDocument sessionDocument = new SessionDocument(session.getId(), sessionData);
        dao.save(sessionDocument);
        dao.updateExpirationTime(session.getId(), getSessionDocumentExpiration());

        log.debug("HTTP session created with ID {}", session.getId());

        return session;
    }

    @Override
    public void save(CouchbaseSession session) {
        log.debug("Saving HTTP session with ID {}", session.getId());

        Map<String, Object> serializedGlobal = serializer.serializeSessionAttributes(session.getGlobalAttributes());
        dao.updateSession(from(serializedGlobal), GLOBAL_NAMESPACE, session.getId());

        if (session.isNamespacePersistenceRequired()) {
            Map<String, Object> serializedNamespace = serializer.serializeSessionAttributes(session.getNamespaceAttributes());
            dao.updateSession(from(serializedNamespace), namespace, session.getId());
        }

        if (session.isPrincipalSession()) {
            String principal = session.getPrincipalAttribute();
            log.debug("Adding principals {} session with ID {}", principal, session.getId());
            if (dao.exists(principal)) {
                dao.updateAppendPrincipalSession(principal, session.getId());
            } else {
                PrincipalSessionsDocument sessionsDocument = new PrincipalSessionsDocument(principal, singletonList(session.getId()));
                dao.save(sessionsDocument);
            }
            try {
                dao.updateExpirationTime(principal, getSessionDocumentExpiration());
            } catch (Exception e) {
                log.error("hahahaha", e);
            }
        }

//        try {
        dao.updateExpirationTime(session.getId(), getSessionDocumentExpiration());
//        }catch (Exception e) {
//            log.error("hahahaha", e);
//        }
    }

    @Override
    public CouchbaseSession getSession(String id) {
        log.debug("Getting HTTP session with ID {}", id);

        Map<String, Object> globalAttributes = dao.findSessionAttributes(id, GLOBAL_NAMESPACE);
        Map<String, Object> namespaceAttributes = dao.findSessionAttributes(id, namespace);

        if (globalAttributes == null && namespaceAttributes == null) {
            log.debug("HTTP session with ID {} not found", id);
            return null;
        }

        notNull(globalAttributes, "Invalid state of HTTP session persisted in couchbase. Missing global attributes.");

        Map<String, Object> deserializedGlobal = serializer.deserializeSessionAttributes(globalAttributes);
        Map<String, Object> deserializedNamespace = serializer.deserializeSessionAttributes(namespaceAttributes);
        CouchbaseSession session = new CouchbaseSession(id, deserializedGlobal, deserializedNamespace);
        if (session.isExpired()) {
            log.debug("HTTP session with ID {} has expired", id);
            deleteSession(session);
            return null;
        }
        session.setLastAccessedTime(currentTimeMillis());

        return session;
    }

    @Override
    public void delete(String id) {
        CouchbaseSession session = getSession(id);
        if (session == null) {
            return;
        }
        deleteSession(session);
    }

    @Override
    public Map<String, CouchbaseSession> findByIndexNameAndIndexValue(String indexName, String indexValue) {
        log.debug("Getting principals {} sessions", indexValue);

        if (!PRINCIPAL_NAME_INDEX_NAME.equals(indexName)) {
            return emptyMap();
        }
        PrincipalSessionsDocument sessionsDocument = dao.findByPrincipal(indexValue);
        if (sessionsDocument == null) {
            return emptyMap();
        }
        Map<String, CouchbaseSession> sessionsById = new HashMap<>(sessionsDocument.getSessionIds().size());
        for (String sessionId : sessionsDocument.getSessionIds()) {
            CouchbaseSession session = getSession(sessionId);
            sessionsById.put(sessionId, session);
        }
        return sessionsById;
    }

    protected int getSessionDocumentExpiration() {
        return sessionTimeout + SESSION_DOCUMENT_EXPIRATION_DELAY_IN_SECONDS;
    }

    protected void deleteSession(CouchbaseSession session) {
        if (session.isPrincipalSession()) {
            log.debug("Removing principals {} session with ID {}", session.getPrincipalAttribute(), session.getId());
            dao.updateRemovePrincipalSession(session.getPrincipalAttribute(), session.getId());
        }
        log.debug("Deleting HTTP session with ID {}", session.getId());
        dao.delete(session.getId());
    }
}
