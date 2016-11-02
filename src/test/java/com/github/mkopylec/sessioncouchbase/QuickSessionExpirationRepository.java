package com.github.mkopylec.sessioncouchbase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mkopylec.sessioncouchbase.configuration.SessionCouchbaseProperties;
import com.github.mkopylec.sessioncouchbase.core.CouchbaseSessionRepository;
import com.github.mkopylec.sessioncouchbase.core.Serializer;
import com.github.mkopylec.sessioncouchbase.data.SessionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

@Repository
public class QuickSessionExpirationRepository extends CouchbaseSessionRepository {

    @Autowired
    public QuickSessionExpirationRepository(SessionCouchbaseProperties sessionCouchbase, SessionDao dao, ObjectMapper mapper, Serializer serializer, ApplicationEventPublisher eventPublisher) {
        super(sessionCouchbase, dao, mapper, serializer, eventPublisher);
    }

    @Override
    protected int getSessionDocumentExpiration() {
        return sessionTimeout + 1;
    }
}
