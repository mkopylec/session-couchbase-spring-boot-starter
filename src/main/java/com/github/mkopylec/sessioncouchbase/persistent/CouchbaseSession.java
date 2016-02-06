package com.github.mkopylec.sessioncouchbase.persistent;

import org.springframework.session.ExpiringSession;
import org.springframework.session.MapSession;

import java.io.Serializable;
import java.util.Set;

public class CouchbaseSession implements ExpiringSession, Serializable {

    private static final long serialVersionUID = 1L;

    protected final MapSession session = new MapSession();

    public CouchbaseSession(int timeoutInSeconds) {
        session.setMaxInactiveIntervalInSeconds(timeoutInSeconds);
    }

    @Override
    public long getCreationTime() {
        return session.getCreationTime();
    }

    @Override
    public long getLastAccessedTime() {
        return session.getLastAccessedTime();
    }

    public void setLastAccessedTime(long lastAccessedTime) {
        session.setLastAccessedTime(lastAccessedTime);
    }

    @Override
    public void setMaxInactiveIntervalInSeconds(int interval) {
        session.setMaxInactiveIntervalInSeconds(interval);
    }

    @Override
    public int getMaxInactiveIntervalInSeconds() {
        return session.getMaxInactiveIntervalInSeconds();
    }

    @Override
    public boolean isExpired() {
        return session.isExpired();
    }

    @Override
    public String getId() {
        return session.getId();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttribute(String attributeName) {
        return (T) session.getAttribute(attributeName);
    }

    @Override
    public Set<String> getAttributeNames() {
        return session.getAttributeNames();
    }

    @Override
    public void setAttribute(String attributeName, Object attributeValue) {
        session.setAttribute(attributeName, attributeValue);
    }

    @Override
    public void removeAttribute(String attributeName) {
        session.removeAttribute(attributeName);
    }
}
