package com.github.mkopylec.sessioncouchbase.persistent;

import com.github.mkopylec.sessioncouchbase.SessionCouchbaseProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;
import org.springframework.session.SessionRepository;
import org.springframework.session.web.http.SessionRepositoryFilter;

import java.util.List;

@Configuration("sessionPersistentConfiguration")
@EnableCouchbaseRepositories
@EnableConfigurationProperties(SessionCouchbaseProperties.class)
@ConditionalOnProperty(name = "session-couchbase.in-memory.enabled", havingValue = "false", matchIfMissing = true)
public class PersistentConfiguration extends AbstractCouchbaseConfiguration {

    @Autowired
    private SessionCouchbaseProperties sessionCouchbase;

    @Bean
    @ConditionalOnMissingBean
    public SessionRepository mapSessionRepository(CouchbaseDao dao) {
        return new CouchbaseSessionRepository(dao, sessionCouchbase.getPersistent().getNamespace(), sessionCouchbase.getTimeoutInSeconds());
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionRepositoryFilter sessionRepositoryFilter(SessionRepository<CouchbaseSession> repository) {
        return new SessionRepositoryFilter<>(repository);
    }

    @Override
    protected List<String> bootstrapHosts() {
        return sessionCouchbase.getPersistent().getHosts();
    }

    @Override
    protected String getBucketName() {
        return sessionCouchbase.getPersistent().getBucketName();
    }

    @Override
    protected String getBucketPassword() {
        return sessionCouchbase.getPersistent().getPassword();
    }
}
