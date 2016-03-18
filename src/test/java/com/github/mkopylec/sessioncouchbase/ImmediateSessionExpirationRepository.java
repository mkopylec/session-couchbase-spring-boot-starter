package com.github.mkopylec.sessioncouchbase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mkopylec.sessioncouchbase.persistent.CouchbaseDao;
import com.github.mkopylec.sessioncouchbase.persistent.CouchbaseSessionRepository;
import com.github.mkopylec.sessioncouchbase.persistent.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "session-couchbase.in-memory.enabled", havingValue = "false", matchIfMissing = true)
public class ImmediateSessionExpirationRepository extends CouchbaseSessionRepository {

    @Autowired
    public ImmediateSessionExpirationRepository(CouchbaseDao dao, ObjectMapper mapper, Serializer serializer, SessionCouchbaseProperties sessionCouchbase) {
        super(dao, sessionCouchbase.getPersistent().getNamespace(), mapper, sessionCouchbase.getTimeoutInSeconds(), serializer);
    }

    @Override
    protected int getSessionEntityExpiration() {
        return sessionTimeout;
    }
}
