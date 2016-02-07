package com.github.mkopylec.sessioncouchbase.persistent;

import org.springframework.session.ExpiringSession;
import org.springframework.session.MapSession;

import java.io.Serializable;
import java.util.Set;

import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

public class CouchbaseSession implements ExpiringSession, Serializable {

    private static final long serialVersionUID = 1L;

    protected static final String GLOBAL_ATTRIBUTE_NAME_PREFIX = "_#global#_";

    protected MapSession globalSession = new MapSession();
    protected MapSession namespaceSession = new MapSession();

    public CouchbaseSession(int timeoutInSeconds) {
        setMaxInactiveIntervalInSeconds(timeoutInSeconds);
    }

    public CouchbaseSession(MapSession globalSession, MapSession namespaceSession) {
        notNull(globalSession, "Missing HTTP session global attributes");
        notNull(namespaceSession, "Missing HTTP session local namespace attributes");
        this.globalSession = globalSession;
        this.namespaceSession = namespaceSession;
    }

    public static String globalAttributeName(String attributeName) {
        return GLOBAL_ATTRIBUTE_NAME_PREFIX + attributeName;
    }

    @Override
    public long getCreationTime() {
        return namespaceSession.getCreationTime();
    }

    @Override
    public long getLastAccessedTime() {
        return namespaceSession.getLastAccessedTime();
    }

    public void setLastAccessedTime(long lastAccessedTime) {
        globalSession.setLastAccessedTime(lastAccessedTime);
        namespaceSession.setLastAccessedTime(lastAccessedTime);
    }

    @Override
    public void setMaxInactiveIntervalInSeconds(int interval) {
        globalSession.setMaxInactiveIntervalInSeconds(interval);
        namespaceSession.setMaxInactiveIntervalInSeconds(interval);
    }

    @Override
    public int getMaxInactiveIntervalInSeconds() {
        return namespaceSession.getMaxInactiveIntervalInSeconds();
    }

    @Override
    public boolean isExpired() {
        return namespaceSession.isExpired();
    }

    @Override
    public String getId() {
        return namespaceSession.getId();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttribute(String attributeName) {
        checkAttributeName(attributeName);
        if (isGlobal(attributeName)) {
            return (T) globalSession.getAttribute(getAttributeName(attributeName));
        } else {
            return (T) namespaceSession.getAttribute(attributeName);
        }
    }

    @Override
    public Set<String> getAttributeNames() {
        return namespaceSession.getAttributeNames();
    }

    @Override
    public void setAttribute(String attributeName, Object attributeValue) {
        checkAttributeName(attributeName);
        if (isGlobal(attributeName) && globalSession != null) {
            globalSession.setAttribute(getAttributeName(attributeName), attributeValue);
        } else {
            namespaceSession.setAttribute(attributeName, attributeValue);
        }
    }

    @Override
    public void removeAttribute(String attributeName) {
        checkAttributeName(attributeName);
        if (isGlobal(attributeName)) {
            globalSession.removeAttribute(getAttributeName(attributeName));
        } else {
            namespaceSession.removeAttribute(attributeName);
        }
    }

    protected MapSession getGlobalSession() {
        return globalSession;
    }

    protected MapSession getNamespaceSession() {
        return namespaceSession;
    }

    private void checkAttributeName(String attributeName) {
        hasText(attributeName, "Empty HTTP session attribute name");
        isTrue(!attributeName.equals(GLOBAL_ATTRIBUTE_NAME_PREFIX), "Empty HTTP session global attribute name");
    }

    private boolean isGlobal(String attributeName) {
        return attributeName.startsWith(GLOBAL_ATTRIBUTE_NAME_PREFIX);
    }

    private String getAttributeName(String globalAttributeName) {
        return globalAttributeName.replaceFirst(GLOBAL_ATTRIBUTE_NAME_PREFIX, "");
    }
}
