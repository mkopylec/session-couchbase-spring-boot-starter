package com.github.mkopylec.sessioncouchbase.core;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.session.FindByIndexNameSessionRepository;

import java.util.Map;

import static com.github.mkopylec.sessioncouchbase.core.OperationMetricName.CREATE_SESSION;
import static com.github.mkopylec.sessioncouchbase.core.OperationMetricName.DELETE_SESSION;
import static com.github.mkopylec.sessioncouchbase.core.OperationMetricName.FIND_SESSION_BY_ID;
import static com.github.mkopylec.sessioncouchbase.core.OperationMetricName.FIND_SESSION_BY_INDEX_NAME_AND_INDEX_VALUE;
import static com.github.mkopylec.sessioncouchbase.core.OperationMetricName.SAVE_SESSION;
import static com.github.mkopylec.sessioncouchbase.core.ResultMetricName.CREATED;
import static com.github.mkopylec.sessioncouchbase.core.ResultMetricName.DELETED;
import static com.github.mkopylec.sessioncouchbase.core.ResultMetricName.ERROR;
import static com.github.mkopylec.sessioncouchbase.core.ResultMetricName.FOUND;
import static com.github.mkopylec.sessioncouchbase.core.ResultMetricName.NOT_FOUND;
import static com.github.mkopylec.sessioncouchbase.core.ResultMetricName.SAVED;

public class MeteredSessionRepository implements FindByIndexNameSessionRepository<CouchbaseSession> {

    protected final MetricNameFactory metricNameFactory;
    protected final MeterRegistry registry;
    protected final FindByIndexNameSessionRepository<CouchbaseSession> delegate;

    public MeteredSessionRepository(MetricNameFactory metricNameFactory, MeterRegistry registry, FindByIndexNameSessionRepository<CouchbaseSession> delegate) {
        this.metricNameFactory = metricNameFactory;
        this.registry = registry;
        this.delegate = delegate;
    }

    @Override
    public CouchbaseSession createSession() {
        try {
            CouchbaseSession session = registry.timer(metricNameFactory.create(CREATE_SESSION)).record(delegate::createSession);
            registry.counter(metricNameFactory.create(CREATE_SESSION, CREATED)).increment();
            return session;
        } catch (RuntimeException e) {
            registry.counter(metricNameFactory.create(CREATE_SESSION, ERROR)).increment();
            throw e;
        }
    }

    @Override
    public void save(CouchbaseSession session) {
        try {
            registry.timer(metricNameFactory.create(SAVE_SESSION)).record(() -> delegate.save(session));
            registry.counter(metricNameFactory.create(SAVE_SESSION, SAVED)).increment();
        } catch (Exception e) {
            registry.counter(metricNameFactory.create(SAVE_SESSION, ERROR)).increment();
            throw e;
        }
    }

    @Override
    public CouchbaseSession findById(String id) {
        try {
            CouchbaseSession session = registry.timer(metricNameFactory.create(FIND_SESSION_BY_ID)).record(() -> delegate.findById(id));
            if (session != null) {
                registry.counter(metricNameFactory.create(FIND_SESSION_BY_ID, FOUND)).increment();
            } else {
                registry.counter(metricNameFactory.create(FIND_SESSION_BY_ID, NOT_FOUND)).increment();
            }
            return session;
        } catch (RuntimeException e) {
            registry.counter(metricNameFactory.create(FIND_SESSION_BY_ID, ERROR)).increment();
            throw e;
        }
    }

    @Override
    public void deleteById(String id) {
        try {
            registry.timer(metricNameFactory.create(DELETE_SESSION)).record(() -> delegate.deleteById(id));
            registry.counter(metricNameFactory.create(DELETE_SESSION, DELETED)).increment();
        } catch (Exception e) {
            registry.counter(metricNameFactory.create(DELETE_SESSION, ERROR)).increment();
            throw e;
        }
    }

    @Override
    public Map<String, CouchbaseSession> findByIndexNameAndIndexValue(String indexName, String indexValue) {
        try {
            Map<String, CouchbaseSession> sessions = registry.timer(metricNameFactory.create(FIND_SESSION_BY_INDEX_NAME_AND_INDEX_VALUE)).record(() -> delegate.findByIndexNameAndIndexValue(indexName, indexValue));
            if (sessions != null) {
                registry.counter(metricNameFactory.create(FIND_SESSION_BY_INDEX_NAME_AND_INDEX_VALUE, FOUND)).increment();
            } else {
                registry.counter(metricNameFactory.create(FIND_SESSION_BY_INDEX_NAME_AND_INDEX_VALUE, NOT_FOUND)).increment();
            }
            return sessions;
        } catch (RuntimeException e) {
            registry.counter(metricNameFactory.create(FIND_SESSION_BY_INDEX_NAME_AND_INDEX_VALUE, ERROR)).increment();
            throw e;
        }
    }
}
