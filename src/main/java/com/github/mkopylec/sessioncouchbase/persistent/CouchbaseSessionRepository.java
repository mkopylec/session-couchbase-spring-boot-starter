package com.github.mkopylec.sessioncouchbase.persistent;

import org.springframework.session.SessionRepository;

import static java.lang.System.currentTimeMillis;
import static org.springframework.util.Assert.notNull;

public class CouchbaseSessionRepository implements SessionRepository<CouchbaseSession> {

    protected final CouchbaseDao dao;
    protected final int sessionTimeout;

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
    }

    @Override
    public CouchbaseSession getSession(String id) {
        SessionEntity entity = dao.findOne(id);
        if (entity == null) {
            return null;
        }
        CouchbaseSession session = entity.getSession();
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
    }
}
