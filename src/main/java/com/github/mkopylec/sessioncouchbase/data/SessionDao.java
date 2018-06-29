package com.github.mkopylec.sessioncouchbase.data;

import java.util.Map;
import java.util.Set;

public interface SessionDao {

    void insertNamespace(String namespace, String id);

    void updateSession(Map<String, Object> attributesToUpdate, Set<String> attributesToRemove, String namespace, String id);

    void updatePutPrincipalSession(String principal, String sessionId);

    void updateRemovePrincipalSession(String principal, String sessionId);

    Map<String, Object> findSessionAttributes(String id, String namespace);

    PrincipalSessionsDocument findByPrincipal(String principal);

    void updateExpirationTime(String id, int expiry);

    void save(SessionDocument document);

    void save(PrincipalSessionsDocument document);

    boolean exists(String documentId);

    void delete(String id);

    void deleteAll();
}
