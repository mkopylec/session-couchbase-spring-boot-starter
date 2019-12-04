package com.github.mkopylec.sessioncouchbase.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mkopylec.sessioncouchbase.core.CouchbaseSessionRepository;
import com.github.mkopylec.sessioncouchbase.core.MeteredSessionRepository;
import com.github.mkopylec.sessioncouchbase.core.MetricNameFactory;
import com.github.mkopylec.sessioncouchbase.core.Serializer;
import com.github.mkopylec.sessioncouchbase.data.SessionDao;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.SessionRepository;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;

@Configuration
@EnableSpringHttpSession
@EnableConfigurationProperties(SessionCouchbaseProperties.class)
public class SessionCouchbaseAutoConfiguration {

    protected SessionCouchbaseProperties properties;
    protected ObjectProvider<MeterRegistry> meterRegistry;

    public SessionCouchbaseAutoConfiguration(SessionCouchbaseProperties properties, ObjectProvider<MeterRegistry> meterRegistry) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    @Bean
    @ConditionalOnMissingBean
    public Serializer serializer() {
        return new Serializer();
    }

    @Bean
    @ConditionalOnMissingBean
    public MetricNameFactory metricNameFactory() {
        return new MetricNameFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionRepository sessionRepository(SessionDao dao, ObjectMapper mapper, Serializer serializer, ApplicationEventPublisher eventPublisher, MetricNameFactory metricNameFactory) {
        CouchbaseSessionRepository repository = new CouchbaseSessionRepository(properties, dao, mapper, serializer, eventPublisher);
        if (properties.getMetrics().isEnabled()) {
            MeterRegistry registry = meterRegistry.getIfAvailable(() -> {
                throw new IllegalStateException("No " + MeterRegistry.class.getName() + " Spring bean provided");
            });
            return new MeteredSessionRepository(metricNameFactory, registry, repository);
        }
        return repository;
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
