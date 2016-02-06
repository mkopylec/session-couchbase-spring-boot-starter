package com.github.mkopylec.sessioncouchbase.persistent;

import org.springframework.session.SessionRepository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.System.currentTimeMillis;
import static org.springframework.util.Assert.notNull;

public class CouchbaseSessionRepository implements SessionRepository<CouchbaseSession> {

    protected final CouchbaseDao dao;
    protected final int sessionTimeout;
    protected final ConcurrentMap<String, CouchbaseSession> sessionsCache = new ConcurrentHashMap<>();

    public CouchbaseSessionRepository(CouchbaseDao dao, int sessionTimeout) {
        notNull(dao, "Missing couchbase data access object");
        this.dao = dao;
        this.sessionTimeout = sessionTimeout;
    }

    @Override
    public CouchbaseSession createSession() {
        return new CouchbaseSession(sessionTimeout);
    }

    @Override
    public void save(CouchbaseSession session) {
        SessionEntity entity = new SessionEntity(session.getId(), session);
        dao.save(entity);
        sessionsCache.put(session.getId(), session);
    }

    @Override
    public CouchbaseSession getSession(String id) {
        CouchbaseSession session = sessionsCache.get(id);
        if (session == null) {
            SessionEntity entity = dao.findOne(id);
            session = entity == null ? null : entity.getSession();
        }
        if (session == null) {
            return null;
        }
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
        sessionsCache.remove(id);
    }
}
