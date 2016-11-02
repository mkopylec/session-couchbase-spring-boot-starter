package com.github.mkopylec.sessioncouchbase.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Session couchbase configuration properties.
 */
@ConfigurationProperties("session-couchbase")
public class SessionCouchbaseProperties {

    /**
     * HTTP session timeout.
     */
    private int timeoutInSeconds = 1800;
    /**
     * HTTP session application namespace under which session data must be stored.
     */
    private String applicationNamespace;
    /**
     * Properties responsible for managing principal HTTP sessions.
     */
    @NestedConfigurationProperty
    private PrincipalSessions principalSessions = new PrincipalSessions();
    /**
     * Properties responsible for retrying Couchbase query when an error occurs.
     */
    @NestedConfigurationProperty
    private Retry retry = new Retry();
    /**
     * Properties responsible for in-memory mode behaviour.
     */
    @NestedConfigurationProperty
    private InMemory inMemory = new InMemory();

    public int getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    public void setTimeoutInSeconds(int timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
    }

    public String getApplicationNamespace() {
        return applicationNamespace;
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

    public Retry getRetry() {
        return retry;
    }

    public void setRetry(Retry retry) {
        this.retry = retry;
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