package com.github.mkopylec.sessioncouchbase;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Session couchbase configuration properties.
 */
@ConfigurationProperties("session-couchbase")
public class SessionCouchbaseProperties {

    /**
     * HTTP session timeout.
     */
    private int timeoutInSeconds = 30 * 60;
    /**
     * Properties responsible for persistent mode behaviour.
     */
    private Persistent persistent = new Persistent();
    /**
     * Properties responsible for in-memory mode behaviour.
     */
    private InMemory inMemory = new InMemory();

    public int getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    public void setTimeoutInSeconds(int timeoutInSeconds) {
        this.timeoutInSeconds = timeoutInSeconds;
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

    public static class Persistent {

        /**
         * HTTP session application namespace under which session data must be stored.
         */
        private String namespace;
        /**
         * Couchbase cluster hosts.
         */
        private List<String> hosts = singletonList("localhost");
        /**
         * Couchbase bucket name where session data must be stored.
         */
        private String bucketName = "default";
        /**
         * Couchbase bucket password.
         */
        private String password = EMPTY;

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public List<String> getHosts() {
            return hosts;
        }

        public void setHosts(List<String> hosts) {
            this.hosts = hosts;
        }

        public String getBucketName() {
            return bucketName;
        }

        public void setBucketName(String bucketName) {
            this.bucketName = bucketName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
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
