package com.github.mkopylec.sessioncouchbase.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mkopylec.sessioncouchbase.core.CouchbaseSessionRepository;
import com.github.mkopylec.sessioncouchbase.core.DelegatingSessionStrategy;
import com.github.mkopylec.sessioncouchbase.core.Serializer;
import com.github.mkopylec.sessioncouchbase.data.CouchbaseDao;
import com.github.mkopylec.sessioncouchbase.data.InMemoryDao;
import com.github.mkopylec.sessioncouchbase.data.RetryLoggingListener;
import com.github.mkopylec.sessioncouchbase.data.SessionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
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
public class SessionCouchbaseAutoConfiguration {

    @Autowired
    protected CouchbaseProperties couchbase;
    @Autowired
    protected SessionCouchbaseProperties sessionCouchbase;

    @Bean
    @ConditionalOnMissingBean
    @OnInMemoryDisabled
    public RetryLoggingListener retryLoggingListener() {
        return new RetryLoggingListener();
    }

    @Bean
    @ConditionalOnMissingBean
    @OnInMemoryDisabled
    public RetryTemplate sessionCouchbaseRetryTemplate(RetryLoggingListener listener) {
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>(1);
        retryableExceptions.put(Exception.class, true);
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(sessionCouchbase.getRetry().getMaxAttempts(), retryableExceptions);
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.registerListener(listener);
        return retryTemplate;
    }

    @Bean(name = "sessionDao")
    @ConditionalOnMissingBean
    @OnInMemoryDisabled
    public SessionDao couchbaseDao(CouchbaseTemplate couchbaseTemplate, @Qualifier("sessionCouchbaseRetryTemplate") RetryTemplate retryTemplate) {
        return new CouchbaseDao(couchbase, couchbaseTemplate, retryTemplate);
    }

    @Bean(name = "sessionDao")
    @ConditionalOnMissingBean
    @OnInMemoryEnabled
    public SessionDao inMemoryDao() {
        return new InMemoryDao();
    }

    @Bean
    @ConditionalOnMissingBean
    public MultiHttpSessionStrategy multiHttpSessionStrategy(SessionDao dao, Serializer serializer) {
        return new DelegatingSessionStrategy(new CookieHttpSessionStrategy(), dao, sessionCouchbase, serializer);
    }

    @Bean
    @ConditionalOnMissingBean
    public Serializer serializer() {
        return new Serializer();
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionRepository sessionRepository(SessionDao dao, ObjectMapper mapper, Serializer serializer, ApplicationEventPublisher eventPublisher) {
        return new CouchbaseSessionRepository(sessionCouchbase, dao, mapper, serializer, eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
