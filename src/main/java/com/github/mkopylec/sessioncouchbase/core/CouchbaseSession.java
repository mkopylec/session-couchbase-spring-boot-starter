package com.github.mkopylec.sessioncouchbase.core;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.springframework.session.ExpiringSession;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.unmodifiableSet;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.isTrue;

public class CouchbaseSession implements ExpiringSession, Serializable {

    private static final long serialVersionUID = 1L;

    public static final String CREATION_TIME_ATTRIBUTE = "$creationTime";
    public static final String LAST_ACCESSED_TIME_ATTRIBUTE = "$lastAccessedTime";
    public static final String MAX_INACTIVE_INTERVAL_ATTRIBUTE = "$maxInactiveInterval";
    protected static final String GLOBAL_ATTRIBUTE_NAME_PREFIX = CouchbaseSession.class.getName() + ".global.";

    private static final Logger log = getLogger(CouchbaseSession.class);

    protected String id = randomUUID().toString();
    protected Map<String, Object> globalAttributesToUpdate = new HashMap<>();
    protected Set<String> globalAttributesToRemove = new HashSet<>();
    protected Map<String, Object> globalAttributes = new HashMap<>();
    protected Map<String, Object> namespaceAttributesToUpdate = new HashMap<>();
    protected Set<String> namespaceAttributesToRemove = new HashSet<>();
    protected Map<String, Object> namespaceAttributes = new HashMap<>();
    protected boolean principalSessionsUpdateRequired = false;

    public CouchbaseSession(int timeoutInSeconds) {
        long now = currentTimeMillis();
        setCreationTime(now);
        setLastAccessedTime(now);
        setMaxInactiveIntervalInSeconds(timeoutInSeconds);
    }

    public CouchbaseSession(String id, Map<String, Object> globalAttributes, Map<String, Object> namespaceAttributes) {
        this.id = id;
        this.globalAttributes = globalAttributes == null ? new HashMap<>() : globalAttributes;
        this.namespaceAttributes = namespaceAttributes == null ? new HashMap<>() : namespaceAttributes;
        if (containsPrincipalAttribute()) {
            principalSessionsUpdateRequired = true;
        }
    }

    public static String globalAttributeName(String attributeName) {
        return GLOBAL_ATTRIBUTE_NAME_PREFIX + attributeName;
    }

    @Override
    public long getCreationTime() {
        return ((Number) globalAttributes.get(CREATION_TIME_ATTRIBUTE)).longValue();
    }

    @Override
    public long getLastAccessedTime() {
        return ((Number) globalAttributes.get(LAST_ACCESSED_TIME_ATTRIBUTE)).longValue();
    }

    public void setLastAccessedTime(long lastAccessedTime) {
        globalAttributes.put(LAST_ACCESSED_TIME_ATTRIBUTE, lastAccessedTime);
        globalAttributesToUpdate.put(LAST_ACCESSED_TIME_ATTRIBUTE, lastAccessedTime);
    }

    @Override
    public void setMaxInactiveIntervalInSeconds(int interval) {
        globalAttributes.put(MAX_INACTIVE_INTERVAL_ATTRIBUTE, interval);
        globalAttributesToUpdate.put(MAX_INACTIVE_INTERVAL_ATTRIBUTE, interval);
    }

    @Override
    public int getMaxInactiveIntervalInSeconds() {
        return (int) globalAttributes.get(MAX_INACTIVE_INTERVAL_ATTRIBUTE);
    }

    @Override
    public boolean isExpired() {
        return getMaxInactiveIntervalInSeconds() >= 0 && currentTimeMillis() - SECONDS.toMillis(getMaxInactiveIntervalInSeconds()) >= getLastAccessedTime();
    }

    @Override
    public String getId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttribute(String attributeName) {
        checkAttributeName(attributeName);
        T attribute;
        if (isGlobal(attributeName)) {
            String name = getNameFromGlobalName(attributeName);
            attribute = (T) globalAttributes.get(name);
            log.trace("Global HTTP session attribute: [name='{}', value={}] has been read", name, attribute);
        } else {
            attribute = (T) namespaceAttributes.get(attributeName);
            log.trace("Application namespace HTTP session attribute: [name='{}', value={}] has been read", attributeName, attribute);
        }
        return attribute;
    }

    @Override
    public Set<String> getAttributeNames() {
        Set<String> attributesNames = globalAttributes.keySet().stream()
                .map(CouchbaseSession::globalAttributeName)
                .collect(toSet());
        attributesNames.addAll(namespaceAttributes.keySet());
        return unmodifiableSet(attributesNames);
    }

    @Override
    public void setAttribute(String attributeName, Object attributeValue) {
        checkAttributeName(attributeName);
        if (isGlobal(attributeName)) {
            String name = getNameFromGlobalName(attributeName);
            if (PRINCIPAL_NAME_INDEX_NAME.equals(name)) {
                principalSessionsUpdateRequired = true;
            }
            globalAttributes.put(name, attributeValue);
            globalAttributesToUpdate.put(name, attributeValue);
            log.trace("Global HTTP session attribute: [name='{}', value={}] has been set", name, attributeValue);
        } else {
            if (PRINCIPAL_NAME_INDEX_NAME.equals(attributeName)) {
                principalSessionsUpdateRequired = true;
            }
            namespaceAttributes.put(attributeName, attributeValue);
            namespaceAttributesToUpdate.put(attributeName, attributeValue);
            log.trace("Application namespace HTTP session attribute: [name='{}', value={}] has been set", attributeName, attributeValue);
        }
    }

    @Override
    public void removeAttribute(String attributeName) {
        checkAttributeName(attributeName);
        if (isGlobal(attributeName)) {
            String name = getNameFromGlobalName(attributeName);
            globalAttributes.remove(name);
            globalAttributesToRemove.add(name);
            log.trace("Global HTTP session attribute: [name='{}'] has been removed", name);
        } else {
            namespaceAttributes.remove(attributeName);
            namespaceAttributesToRemove.add(attributeName);
            log.trace("Application namespace HTTP session attribute: [name='{}'] has been removed", attributeName);
        }
    }

    public Map<String, Object> getGlobalAttributesToUpdate() {
        return globalAttributesToUpdate;
    }

    public Set<String> getGlobalAttributesToRemove() {
        return globalAttributesToRemove;
    }

    public void clearChangedGlobalAttributes() {
        globalAttributesToUpdate.clear();
        globalAttributesToRemove.clear();
    }

    public Map<String, Object> getGlobalAttributes() {
        return globalAttributes;
    }

    public Map<String, Object> getNamespaceAttributesToUpdate() {
        return namespaceAttributesToUpdate;
    }

    public Set<String> getNamespaceAttributesToRemove() {
        return namespaceAttributesToRemove;
    }

    public void clearChangedNamespaceAttributes() {
        namespaceAttributesToUpdate.clear();
        namespaceAttributesToRemove.clear();
    }

    public Map<String, Object> getNamespaceAttributes() {
        return namespaceAttributes;
    }

    public boolean isGlobalPersistenceRequired() {
        return MapUtils.isNotEmpty(globalAttributesToUpdate) || CollectionUtils.isNotEmpty(globalAttributesToRemove);
    }

    public boolean isNamespacePersistenceRequired() {
        return MapUtils.isNotEmpty(namespaceAttributesToUpdate) || CollectionUtils.isNotEmpty(namespaceAttributesToRemove);
    }

    public boolean isPrincipalSessionsUpdateRequired() {
        return principalSessionsUpdateRequired;
    }

    public String getPrincipalAttribute() {
        Object principal = globalAttributes.get(PRINCIPAL_NAME_INDEX_NAME);
        if (principal == null) {
            principal = namespaceAttributes.get(PRINCIPAL_NAME_INDEX_NAME);
        }
        return (String) principal;
    }

    public void unsetPrincipalSessionsUpdateRequired() {
        principalSessionsUpdateRequired = false;
    }

    protected void setCreationTime(long creationTime) {
        globalAttributes.put(CREATION_TIME_ATTRIBUTE, creationTime);
        globalAttributesToUpdate.put(CREATION_TIME_ATTRIBUTE, creationTime);
    }

    protected void checkAttributeName(String attributeName) {
        hasText(attributeName, "Empty HTTP session attribute name");
        isTrue(!attributeName.trim().equals(GLOBAL_ATTRIBUTE_NAME_PREFIX), "Empty HTTP session global attribute name");
    }

    protected boolean isGlobal(String attributeName) {
        return attributeName.startsWith(GLOBAL_ATTRIBUTE_NAME_PREFIX);
    }

    protected String getNameFromGlobalName(String globalAttributeName) {
        return removeStart(globalAttributeName, GLOBAL_ATTRIBUTE_NAME_PREFIX);
    }

    protected boolean containsPrincipalAttribute() {
        return globalAttributes.containsKey(PRINCIPAL_NAME_INDEX_NAME) || namespaceAttributes.containsKey(PRINCIPAL_NAME_INDEX_NAME);
    }
}
