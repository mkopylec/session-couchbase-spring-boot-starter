package com.github.mkopylec.sessioncouchbase.configuration;

import com.couchbase.client.java.query.consistency.ScanConsistency;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;

import static com.couchbase.client.java.query.consistency.ScanConsistency.REQUEST_PLUS;
import static java.time.Duration.ofSeconds;
import static org.apache.commons.lang3.StringUtils.trimToNull;

/**
 * Session couchbase configuration properties.
 */
@ConfigurationProperties("session-couchbase")
public class SessionCouchbaseProperties {

    /**
     * HTTP session timeout.
     */
    private Duration timeout = ofSeconds(1800);
    /**
     * HTTP session application namespace under which session data must be stored.
     */
    private String applicationNamespace = "default";
    /**
     * Properties responsible for managing principal HTTP sessions.
     */
    @NestedConfigurationProperty
    private PrincipalSessions principalSessions = new PrincipalSessions();
    /**
     * Properties responsible for persistent mode behaviour.
     */
    @NestedConfigurationProperty
    private Persistent persistent = new Persistent();
    /**
     * Properties responsible for in-memory mode behaviour.
     */
    @NestedConfigurationProperty
    private InMemory inMemory = new InMemory();

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public String getApplicationNamespace() {
        return trimToNull(applicationNamespace);
    }

    public void setApplicationNamespace(String applicationNamespace) {
        this.applicationNamespace = applicationNamespace;
    }

    public PrincipalSessions getPrincipalSessions() {
        return principalSessions;
    }

    public void setPrincipalSessions(PrincipalSessions principalSessions) {
        this.principalSessions = principalSessions;
    }

    public Persistent getPersistent() {
        return persistent;
    }

    public void setPersistent(Persistent persistent) {
        this.persistent = persistent;
    }

    public InMemory getInMemory() {
        return inMemory;
    }

    public void setInMemory(InMemory inMemory) {
        this.inMemory = inMemory;
    }

    public static class PrincipalSessions {

        /**
         * Flag for enabling and disabling finding HTTP sessions by principal. Can significantly decrease application performance when enabled.
         */
        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Persistent {

        /**
         * N1QL query scan consistency.
         */
        private ScanConsistency queryConsistency = REQUEST_PLUS;
        /**
         * Properties responsible for retrying Couchbase query when an error occurs.
         */
        @NestedConfigurationProperty
        private Retry retry = new Retry();

        public ScanConsistency getQueryConsistency() {
            return queryConsistency;
        }

        public void setQueryConsistency(ScanConsistency queryConsistency) {
            this.queryConsistency = queryConsistency;
        }

        public Retry getRetry() {
            return retry;
        }

        public void setRetry(Retry retry) {
            this.retry = retry;
        }

        public static class Retry {

            /**
             * Maximum number of attempts to repeat a query to Couchbase when error occurs.
             */
            private int maxAttempts = 1;

            public int getMaxAttempts() {
                return maxAttempts;
            }

            public void setMaxAttempts(int maxAttempts) {
                this.maxAttempts = maxAttempts;
            }
        }
    }

    public static class InMemory {

        /**
         * Flag for enabling and disabling in-memory mode.
         */
        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
