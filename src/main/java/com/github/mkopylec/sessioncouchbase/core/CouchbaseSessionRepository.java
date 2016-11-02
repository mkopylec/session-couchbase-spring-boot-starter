package com.github.mkopylec.sessioncouchbase.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mkopylec.sessioncouchbase.configuration.SessionCouchbaseProperties;
import com.github.mkopylec.sessioncouchbase.data.PrincipalSessionsDocument;
import com.github.mkopylec.sessioncouchbase.data.SessionDao;
import com.github.mkopylec.sessioncouchbase.data.SessionDocument;
import org.slf4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.events.SessionCreatedEvent;
import org.springframework.session.events.SessionDeletedEvent;
import org.springframework.session.events.SessionExpiredEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyMap;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

public class CouchbaseSessionRepository implements FindByIndexNameSessionRepository<CouchbaseSession> {

    protected static final String GLOBAL_NAMESPACE = "global";
    protected static final int SESSION_DOCUMENT_EXPIRATION_DELAY_IN_SECONDS = 60;

    private static final Logger log = getLogger(CouchbaseSessionRepository.class);

    protected final SessionDao dao;
    protected final ObjectMapper mapper;
    protected final String namespace;
    protected final int sessionTimeout;
    protected final Serializer serializer;
    protected final boolean principalSessionsEnabled;
    protected final ApplicationEventPublisher eventPublisher;

    public CouchbaseSessionRepository(
            SessionCouchbaseProperties sessionCouchbase,
            SessionDao dao,
            ObjectMapper mapper,
            Serializer serializer,
            ApplicationEventPublisher eventPublisher
    ) {
        notNull(sessionCouchbase, "Missing session couchbase properties");
        notNull(dao, "Missing couchbase data access object");
        notNull(mapper, "Missing JSON object mapper");
        String namespace = sessionCouchbase.getApplicationNamespace();
        hasText(namespace, "Empty HTTP session namespace");
        isTrue(!namespace.equals(GLOBAL_NAMESPACE), "Forbidden HTTP session namespace '" + namespace + "'");
        notNull(serializer, "Missing object serializer");
        notNull(eventPublisher, "Missing application event publisher");
        this.dao = dao;
        this.mapper = mapper;
        this.namespace = namespace.trim();
        this.sessionTimeout = sessionCouchbase.getTimeoutInSeconds();
        this.serializer = serializer;
        this.principalSessionsEnabled = sessionCouchbase.getPrincipalSessions().isEnabled();
        this.eventPublisher = eventPublisher;
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
        eventPublisher.publishEvent(new SessionCreatedEvent(this, session));

        log.debug("HTTP session with ID {} has been created", session.getId());

        return session;
    }

    @Override
    public void save(CouchbaseSession session) {
        Map<String, Object> serializedGlobal = serializer.serializeSessionAttributes(session.getGlobalAttributesToUpdate());
        dao.updateSession(serializedGlobal, session.getGlobalAttributesToRemove(), GLOBAL_NAMESPACE, session.getId());

        if (session.isNamespacePersistenceRequired()) {
            Map<String, Object> serializedNamespace = serializer.serializeSessionAttributes(session.getNamespaceAttributesToUpdate());
            dao.updateSession(serializedNamespace, session.getNamespaceAttributesToRemove(), namespace, session.getId());
        }

        if (isOperationOnPrincipalSessionsRequired(session)) {
            savePrincipalSession(session);
        }
        dao.updateExpirationTime(session.getId(), getSessionDocumentExpiration());
        log.debug("HTTP session with ID {} has been saved", session.getId());
    }

    @Override
    public CouchbaseSession getSession(String id) {
        Map<String, Object> globalAttributes = dao.findSessionAttributes(id, GLOBAL_NAMESPACE);
        Map<String, Object> namespaceAttributes = dao.findSessionAttributes(id, namespace);

        if (globalAttributes == null && namespaceAttributes == null) {
            log.debug("HTTP session with ID {} not found", id);
            return null;
        }

        notNull(globalAttributes, "Invalid state of HTTP session persisted in couchbase. Missing global attributes.");

        if (namespaceAttributes == null) {
            dao.insertNamespace(namespace, id);
        }

        Map<String, Object> deserializedGlobal = serializer.deserializeSessionAttributes(globalAttributes);
        Map<String, Object> deserializedNamespace = serializer.deserializeSessionAttributes(namespaceAttributes);
        CouchbaseSession session = new CouchbaseSession(id, deserializedGlobal, deserializedNamespace);
        if (session.isExpired()) {
            log.debug("HTTP session with ID {} has expired", id);
            deleteSession(session);
            eventPublisher.publishEvent(new SessionExpiredEvent(this, session));
            return null;
        }
        session.setLastAccessedTime(currentTimeMillis());

        log.debug("HTTP session with ID {} has been found", id);

        return session;
    }

    @Override
    public void delete(String id) {
        CouchbaseSession session = getSession(id);
        if (session == null) {
            return;
        }
        deleteSession(session);
        eventPublisher.publishEvent(new SessionDeletedEvent(this, session));
    }

    @Override
    public Map<String, CouchbaseSession> findByIndexNameAndIndexValue(String indexName, String indexValue) {
        if (!principalSessionsEnabled) {
            throw new IllegalStateException("Cannot get principal HTTP sessions. Enable getting principal HTTP sessions using 'session-couchbase.principal-sessions.enabled' configuration property.");
        }
        if (!PRINCIPAL_NAME_INDEX_NAME.equals(indexName)) {
            return emptyMap();
        }
        PrincipalSessionsDocument sessionsDocument = dao.findByPrincipal(indexValue);
        if (sessionsDocument == null) {
            log.debug("Principals {} sessions not found", indexValue);
            return emptyMap();
        }
        Map<String, CouchbaseSession> sessionsById = new HashMap<>(sessionsDocument.getSessionIds().size());
        sessionsDocument.getSessionIds().forEach(sessionId -> {
            CouchbaseSession session = getSession(sessionId);
            if (session != null) {
                sessionsById.put(sessionId, session);
            }
        });
        if (sessionsById.isEmpty()) {
            dao.delete(indexValue);
        }

        log.debug("Principals {} sessions with IDs {} have been found", indexValue, sessionsById.keySet());

        return sessionsById;
    }

    protected int getSessionDocumentExpiration() {
        return sessionTimeout + SESSION_DOCUMENT_EXPIRATION_DELAY_IN_SECONDS;
    }

    protected void savePrincipalSession(CouchbaseSession session) {
        String principal = session.getPrincipalAttribute();
        if (dao.exists(principal)) {
            dao.updatePutPrincipalSession(principal, session.getId());
        } else {
            Set<String> sessionIds = new HashSet<>(1);
            sessionIds.add(session.getId());
            PrincipalSessionsDocument sessionsDocument = new PrincipalSessionsDocument(principal, sessionIds);
            dao.save(sessionsDocument);
        }
        log.debug("Principals {} session with ID {} has been added", principal, session.getId());
    }

    protected void deleteSession(CouchbaseSession session) {
        if (isOperationOnPrincipalSessionsRequired(session)) {
            dao.updateRemovePrincipalSession(session.getPrincipalAttribute(), session.getId());
            log.debug("Principals {} session with ID {} has been removed", session.getPrincipalAttribute(), session.getId());
        }
        dao.delete(session.getId());
        log.debug("HTTP session with ID {} has been deleted", session.getId());
    }

    protected boolean isOperationOnPrincipalSessionsRequired(CouchbaseSession session) {
        return principalSessionsEnabled && session.isPrincipalSession();
    }
}
