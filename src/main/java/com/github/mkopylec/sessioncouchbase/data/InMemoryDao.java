package com.github.mkopylec.sessioncouchbase.data;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.System.currentTimeMillis;
import static java.util.Optional.ofNullable;

public class InMemoryDao implements SessionDao, InitializingBean {

    protected final ThreadPoolTaskScheduler expirationScheduler = new ThreadPoolTaskScheduler();
    protected final Map<String, SessionDocument> sessions = new ConcurrentHashMap<>();
    protected final Map<String, PrincipalSessionsDocument> principalSessions = new ConcurrentHashMap<>();
    protected final Map<String, Long> expirationTimes = new ConcurrentHashMap<>();

    @Override
    public void insertNamespace(String namespace, String id) {
        ofNullable(sessions.get(id)).ifPresent(
                document -> document.getData().put(namespace, new HashMap<>())
        );
    }

    @Override
    public void updateSession(Map<String, Object> attributesToUpdate, Set<String> attributesToRemove, String namespace, String id) {
        ofNullable(sessions.get(id)).ifPresent(document -> {
            Map<String, Object> namespaceData = document.getData().get(namespace);
            if (MapUtils.isNotEmpty(attributesToUpdate)) {
                attributesToUpdate.forEach(namespaceData::put);
            }
            if (CollectionUtils.isNotEmpty(attributesToRemove)) {
                attributesToRemove.forEach(namespaceData::remove);
            }
        });
    }

    @Override
    public void updatePutPrincipalSession(String principal, String sessionId) {
        ofNullable(principalSessions.get(principal)).ifPresent(
                document -> document.getSessionIds().add(sessionId)
        );
    }

    @Override
    public void updateRemovePrincipalSession(String principal, String sessionId) {
        ofNullable(principalSessions.get(principal)).ifPresent(
                document -> document.getSessionIds().remove(sessionId)
        );
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
        expirationTimes.remove(id);
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
    public void afterPropertiesSet() {
        expirationScheduler.initialize();
        expirationScheduler.scheduleAtFixedRate(() -> {
            Iterator<Entry<String, Long>> iterator = expirationTimes.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, Long> entry = iterator.next();
                if (entry.getValue() < currentTimeMillis()) {
                    sessions.remove(entry.getKey());
                    principalSessions.remove(entry.getKey());
                    iterator.remove();
                }
            }
        }, 1000);
    }
}
