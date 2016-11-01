package com.github.mkopylec.sessioncouchbase.persistent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mkopylec.sessioncouchbase.SessionCouchbaseProperties;
import com.github.mkopylec.sessioncouchbase.persistent.data.CouchbaseDao;
import com.github.mkopylec.sessioncouchbase.persistent.data.RetryLoggingListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.session.SessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.web.http.CookieHttpSessionStrategy;
import org.springframework.session.web.http.MultiHttpSessionStrategy;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCouchbaseRepositories
@EnableSpringHttpSession
@EnableConfigurationProperties({SessionCouchbaseProperties.class, CouchbaseProperties.class})
@ConditionalOnProperty(name = "session-couchbase.in-memory.enabled", havingValue = "false", matchIfMissing = true)
public class PersistentAutoConfiguration {

    @Autowired
    protected CouchbaseProperties couchbase;
    @Autowired
    protected SessionCouchbaseProperties sessionCouchbase;

    @Bean
    @ConditionalOnMissingBean
    public RetryLoggingListener retryLoggingListener() {
        return new RetryLoggingListener();
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryTemplate sessionCouchbaseRetryTemplate(RetryLoggingListener listener) {
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>(1);
        retryableExceptions.put(Exception.class, true);
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(sessionCouchbase.getPersistent().getRetry().getMaxAttempts(), retryableExceptions);
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.registerListener(listener);
        return retryTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public CouchbaseDao couchbaseDao(CouchbaseTemplate couchbaseTemplate, @Qualifier("sessionCouchbaseRetryTemplate") RetryTemplate retryTemplate) {
        return new CouchbaseDao(couchbase, couchbaseTemplate, retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public MultiHttpSessionStrategy multiHttpSessionStrategy(CouchbaseDao dao, Serializer serializer) {
        return new DelegatingSessionStrategy(new CookieHttpSessionStrategy(), dao, sessionCouchbase.getPersistent().getNamespace(), serializer);
    }

    @Bean
    @ConditionalOnMissingBean
    public Serializer serializer() {
        return new Serializer();
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionRepository sessionRepository(CouchbaseDao dao, ObjectMapper mapper, Serializer serializer) {
        return new CouchbaseSessionRepository(
                dao, sessionCouchbase.getPersistent().getNamespace(), mapper, sessionCouchbase.getTimeoutInSeconds(), serializer, sessionCouchbase.getPersistent().getPrincipalSessions().isEnabled()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
