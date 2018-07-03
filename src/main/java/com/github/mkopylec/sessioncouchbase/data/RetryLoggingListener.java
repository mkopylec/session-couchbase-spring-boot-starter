package com.github.mkopylec.sessioncouchbase.data;

import org.slf4j.Logger;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;

import static org.slf4j.LoggerFactory.getLogger;

public class RetryLoggingListener extends RetryListenerSupport {

    private static final Logger log = getLogger(RetryLoggingListener.class);

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        // TODO remove +1 ?
        int attempt = context.getRetryCount() + 1;
        log.debug("Attempt " + attempt + " to query Couchbase has failed", throwable);
    }

    @Override
    public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {

    }
}
