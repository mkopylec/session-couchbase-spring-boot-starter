package com.github.mkopylec.sessioncouchbase.configuration;

import com.github.mkopylec.sessioncouchbase.data.InMemoryDao;
import com.github.mkopylec.sessioncouchbase.data.SessionDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SessionCouchbaseProperties.class)
@ConditionalOnProperty(name = "session-couchbase.in-memory.enabled")
public class InMemoryConfiguration {

    @Autowired
    protected SessionCouchbaseProperties sessionCouchbase;

    @Bean
    @ConditionalOnMissingBean
    public SessionDao sessionDao() {
        return new InMemoryDao();
    }
}
