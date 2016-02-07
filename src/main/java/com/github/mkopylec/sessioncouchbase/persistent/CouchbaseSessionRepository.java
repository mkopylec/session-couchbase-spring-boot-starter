package com.github.mkopylec.sessioncouchbase.persistent;

import org.springframework.session.MapSession;
import org.springframework.session.SessionRepository;

import static java.lang.System.currentTimeMillis;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

public class CouchbaseSessionRepository implements SessionRepository<CouchbaseSession> {

    protected static final String SESSION_KEY_SEPARATOR = "###";

    protected final CouchbaseDao dao;
    protected final String namespace;
    protected final int sessionTimeout;

    public CouchbaseSessionRepository(CouchbaseDao dao, String namespace, int sessionTimeout) {
        notNull(dao, "Missing couchbase data access object");
        hasText(namespace, "Empty HTTP session namespace");
        this.dao = dao;
        this.namespace = namespace.trim();
        this.sessionTimeout = sessionTimeout;
    }

    @Override
    public CouchbaseSession createSession() {
        return new CouchbaseSession(sessionTimeout);
    }

    @Override
    public void save(CouchbaseSession session) {
        SessionEntity namespaceEntity = new SessionEntity(getNamespaceSessionKey(session.getId()), session.getNamespaceSession());
        SessionEntity globalEntity = new SessionEntity(session.getId(), session.getGlobalSession());
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

        MapSession globalSession = globalEntity.getSession();
        MapSession namespaceSession = namespaceEntity.getSession();
        CouchbaseSession session = new CouchbaseSession(globalSession, namespaceSession);

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
}
