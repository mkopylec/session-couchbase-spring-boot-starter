package com.github.mkopylec.sessioncouchbase.data;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.System.currentTimeMillis;

public class InMemoryDao implements SessionDao, InitializingBean {

    protected final ThreadPoolTaskScheduler expirationScheduler = new ThreadPoolTaskScheduler();
    protected final Map<String, SessionDocument> sessions = new ConcurrentHashMap<>();
    protected final Map<String, PrincipalSessionsDocument> principalSessions = new ConcurrentHashMap<>();
    protected final Map<String, Long> expirationTimes = new ConcurrentHashMap<>();

    @Override
    public void insertNamespace(String namespace, String id) {
        sessions.get(id).getData().put(namespace, new HashMap<>());
    }

    @Override
    public void updateSession(Map<String, Object> attributesToUpdate, Set<String> attributesToRemove, String namespace, String id) {
        Map<String, Object> namespaceData = sessions.get(id).getData().get(namespace);
        if (MapUtils.isNotEmpty(attributesToUpdate)) {
            attributesToUpdate.forEach(namespaceData::put);
        }
        if (CollectionUtils.isNotEmpty(attributesToRemove)) {
            attributesToRemove.forEach(namespaceData::remove);
        }
    }

    @Override
    public void updatePutPrincipalSession(String principal, String sessionId) {
        principalSessions.get(principal).getSessionIds().add(sessionId);
    }

    @Override
    public void updateRemovePrincipalSession(String principal, String sessionId) {
        principalSessions.get(principal).getSessionIds().remove(sessionId);
    }

    @Override
    public Map<String, Object> findSessionAttributes(String id, String namespace) {
        SessionDocument document = sessions.get(id);
        if (document == null) {
            return null;
        }
        return document.getData().get(namespace);
    }

    @Override
    public SessionDocument findById(String id) {
        return sessions.get(id);
    }

    @Override
    public PrincipalSessionsDocument findByPrincipal(String principal) {
        return principalSessions.get(principal);
    }

    @Override
    public void updateExpirationTime(String id, int expiry) {
        long expirationTime = currentTimeMillis() + expiry * 1000;
        expirationTimes.put(id, expirationTime);
    }

    @Override
    public void save(SessionDocument document) {
        sessions.put(document.getId(), document);
    }

    @Override
    public void save(PrincipalSessionsDocument document) {
        principalSessions.put(document.getPrincipal(), document);
    }

    @Override
    public boolean exists(String documentId) {
        return sessions.containsKey(documentId) || principalSessions.containsKey(documentId);
    }

    @Override
    public void delete(String id) {
        sessions.remove(id);
        principalSessions.remove(id);
    }

    @Override
    public void deleteAll() {
        expirationTimes.clear();
        sessions.clear();
        principalSessions.clear();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        expirationScheduler.initialize();
        expirationScheduler.scheduleAtFixedRate(() -> expirationTimes.forEach((documentId, expirationTime) -> {
            if (expirationTime < currentTimeMillis()) {
                sessions.remove(documentId);
                principalSessions.remove(documentId);
            }
        }), 1000);
    }
}
