package com.github.mkopylec.sessioncouchbase.persistent;

import com.github.mkopylec.sessioncouchbase.SessionCouchbaseProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;

import java.util.List;

@Configuration("")
@EnableCouchbaseRepositories
@EnableConfigurationProperties(SessionCouchbaseProperties.class)
@ConditionalOnProperty(name = "session-couchbase.in-memory.enabled", havingValue = "false", matchIfMissing = true)
public class PersistentConfiguration extends AbstractCouchbaseConfiguration {

    @Override
    protected List<String> bootstrapHosts() {
        return null;
    }

    @Override
    protected String getBucketName() {
        return null;
    }

    @Override
    protected String getBucketPassword() {
        return null;
    }
}
