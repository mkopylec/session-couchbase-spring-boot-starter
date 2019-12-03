package com.github.mkopylec.sessioncouchbase;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseConfiguration;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.couchbase.CouchbaseContainer;

import javax.annotation.PostConstruct;
import java.util.List;

import static com.couchbase.client.java.cluster.DefaultBucketSettings.builder;
import static java.util.Collections.singletonList;
import static org.testcontainers.couchbase.CouchbaseContainer.DOCKER_IMAGE_NAME;

@Configuration
@ConditionalOnProperty(name = "session-couchbase.in-memory.enabled", havingValue = "false", matchIfMissing = true)
public class Couchbase extends CouchbaseConfiguration {

    private static CouchbaseContainer couchbase;
    private static boolean started;
    private CouchbaseProperties properties;

    public Couchbase(CouchbaseProperties properties) {
        super(properties);
        this.properties = properties;
    }

    @Override
    protected List<String> determineBootstrapHosts() {
        return singletonList(couchbase.getContainerIpAddress());
    }

    @PostConstruct
    private void start() {
        if (started) {
            return;
        }
        couchbase = new CouchbaseContainer(DOCKER_IMAGE_NAME + "6.0.3")
                .withNewBucket(builder()
                        .name(properties.getBucket().getName())
                        .password(properties.getBucket().getPassword())
                        .build());
        couchbase.setPortBindings(singletonList("8091:8091"));
        couchbase.start();
        started = true;
    }
}
