package com.github.mkopylec.sessioncouchbase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mkopylec.sessioncouchbase.configuration.SessionCouchbaseProperties;
import com.github.mkopylec.sessioncouchbase.core.CouchbaseSessionRepository;
import com.github.mkopylec.sessioncouchbase.core.Serializer;
import com.github.mkopylec.sessioncouchbase.data.SessionDao;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import static java.lang.Math.toIntExact;

@Repository
@Profile("quick-expiration")
public class QuickSessionExpirationRepository extends CouchbaseSessionRepository {

    public QuickSessionExpirationRepository(SessionCouchbaseProperties properties, SessionDao dao, ObjectMapper mapper, Serializer serializer, ApplicationEventPublisher eventPublisher) {
        super(properties, dao, mapper, serializer, eventPublisher);
    }

    @Override
    protected int getSessionDocumentExpiration() {
        return toIntExact(properties.getTimeout().plusSeconds(1).getSeconds());
    }
}
