package com.github.mkopylec.sessioncouchbase.persistent;

import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import com.couchbase.client.java.query.N1qlQueryRow;
import org.springframework.data.couchbase.core.CouchbaseTemplate;

import java.util.List;
import java.util.Map;

import static com.couchbase.client.java.document.json.JsonArray.from;
import static com.couchbase.client.java.query.N1qlQuery.parameterized;
import static org.springframework.util.Assert.isTrue;

public class CouchbaseDao {

    protected final CouchbaseTemplate couchbase;

    public CouchbaseDao(CouchbaseTemplate couchbase) {
        this.couchbase = couchbase;
    }

    public void updateSession(JsonObject attributes, String namespace, String id) {
        couchbase.queryN1QL(parameterized("UPDATE default USE KEYS $1 SET data.`" + namespace + "` = $2", from(id, attributes)));
    }

    public void updateAppendPrincipalSession(String principal, String sessionId) {
        couchbase.queryN1QL(parameterized("UPDATE default USE KEYS $1 SET sessionIds = ARRAY_APPEND(sessionIds, $2)", from(principal, sessionId)));
    }

    public void updateRemovePrincipalSession(String principal, String sessionId) {
        couchbase.queryN1QL(parameterized("UPDATE default USE KEYS $1 SET sessionIds = ARRAY_REMOVE(sessionIds, $2)", from(principal, sessionId)));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> findSessionAttributes(String id, String namespace) {
        List<N1qlQueryRow> attributes = couchbase.queryN1QL(parameterized("SELECT * FROM default.data.`" + namespace + "` USE KEYS $1", from(id))).allRows();
        isTrue(attributes.size() < 2, "Invalid HTTP session state. Multiple namespaces '" + namespace + "' for session ID '" + id + "'");
        if (attributes.isEmpty()) {
            return null;
        }
        return (Map<String, Object>) attributes.get(0).value().toMap().get(namespace);
    }

    public SessionDocument findById(String id) {
        return couchbase.findById(id, SessionDocument.class);
    }

    public PrincipalSessionsDocument findByPrincipal(String principal) {
        return couchbase.findById(principal, PrincipalSessionsDocument.class);
    }

    public void updateExpirationTime(String id, int expiry) {
        couchbase.getCouchbaseBucket().touch(id, expiry);
    }

    public void save(SessionDocument document) {
        couchbase.save(document);
    }

    public void save(PrincipalSessionsDocument document) {
        couchbase.save(document);
    }

    public boolean exists(String documentId) {
        return couchbase.exists(documentId);
    }

    public void delete(String id) {
        try {
            couchbase.remove(id);
        } catch (DocumentDoesNotExistException ex) {
            //Do nothing
        }
    }
}
