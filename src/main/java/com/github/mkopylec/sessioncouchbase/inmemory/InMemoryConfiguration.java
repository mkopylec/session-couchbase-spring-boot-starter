package com.github.mkopylec.sessioncouchbase.inmemory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.MapSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.session.web.http.SessionRepositoryFilter;

@Configuration
@ConditionalOnProperty(name = "session-couchbase.in-memory.enabled")
public class InMemoryConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SessionRepository mapSessionRepository() {
        return new MapSessionRepository();
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionRepositoryFilter sessionRepositoryFilter(SessionRepository<MapSession> repository) {
        return new SessionRepositoryFilter<>(repository);
    }
}
