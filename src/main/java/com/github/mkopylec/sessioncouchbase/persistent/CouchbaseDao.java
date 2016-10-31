package com.github.mkopylec.sessioncouchbase.persistent;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;
import org.springframework.data.couchbase.core.CouchbaseQueryExecutionException;
import org.springframework.data.couchbase.core.CouchbaseTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.couchbase.client.java.document.json.JsonArray.from;
import static com.couchbase.client.java.query.N1qlQuery.parameterized;
import static org.springframework.util.Assert.isTrue;

public class CouchbaseDao {

    protected final CouchbaseProperties couchbase;
    protected final CouchbaseTemplate template;

    public CouchbaseDao(CouchbaseProperties couchbase, CouchbaseTemplate template) {
        this.couchbase = couchbase;
        this.template = template;
    }

    public void insertNamespace(String namespace, String id) {
        String statement = "UPDATE " + getBucketName() + " USE KEYS $1 SET data.`" + namespace + "` = {}";
        executeQuery(statement, from(id, namespace));
    }

    public void updateSession(Map<String, Object> attributesToUpdate, Set<String> attributesToRemove, String namespace, String id) {
        StringBuilder statement = new StringBuilder("UPDATE ").append(getBucketName()).append(" USE KEYS $1");
        List<Object> parameters = new ArrayList<>(attributesToUpdate.size() + attributesToRemove.size() + 1);
        parameters.add(id);
        int parameterIndex = 2;
        if (MapUtils.isNotEmpty(attributesToUpdate)) {
            statement.append(" SET ");
            for (Entry<String, Object> attribute : attributesToUpdate.entrySet()) {
                parameters.add(attribute.getValue());
                statement.append("data.`").append(namespace).append("`.`").append(attribute.getKey()).append("` = $").append(parameterIndex++).append(",");
            }
            deleteLastCharacter(statement);
        }
        if (CollectionUtils.isNotEmpty(attributesToRemove)) {
            statement.append(" UNSET ");
            attributesToRemove.forEach(name -> statement.append("data.`").append(namespace).append("`.`").append(name).append("`,"));
            deleteLastCharacter(statement);
        }
        executeQuery(statement.toString(), from(parameters));
    }

    public void updatePutPrincipalSession(String principal, String sessionId) {
        String statement = "UPDATE " + getBucketName() + " USE KEYS $1 SET sessionIds = ARRAY_PUT(sessionIds, $2)";
        executeQuery(statement, from(principal, sessionId));
    }

    public void updateRemovePrincipalSession(String principal, String sessionId) {
        String statement = "UPDATE " + getBucketName() + " USE KEYS $1 SET sessionIds = ARRAY_REMOVE(sessionIds, $2)";
        executeQuery(statement, from(principal, sessionId));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> findSessionAttributes(String id, String namespace) {
        String statement = "SELECT * FROM " + getBucketName() + ".data.`" + namespace + "` USE KEYS $1";
        N1qlQueryResult result = executeQuery(statement, from(id));
        List<N1qlQueryRow> attributes = result.allRows();
        isTrue(attributes.size() < 2, "Invalid HTTP session state. Multiple namespaces '" + namespace + "' for session ID '" + id + "'");
        if (attributes.isEmpty()) {
            return null;
        }
        return (Map<String, Object>) attributes.get(0).value().toMap().get(namespace);
    }

    public SessionDocument findById(String id) {
        return template.findById(id, SessionDocument.class);
    }

    public PrincipalSessionsDocument findByPrincipal(String principal) {
        return template.findById(principal, PrincipalSessionsDocument.class);
    }

    public void updateExpirationTime(String id, int expiry) {
        template.getCouchbaseBucket().touch(id, expiry);
    }

    public void save(SessionDocument document) {
        template.save(document);
    }

    public void save(PrincipalSessionsDocument document) {
        template.save(document);
    }

    public boolean exists(String documentId) {
        return template.exists(documentId);
    }

    public void delete(String id) {
        try {
            template.remove(id);
        } catch (DocumentDoesNotExistException ex) {
            //Do nothing
        }
    }

    protected N1qlQueryResult executeQuery(String statement, JsonArray parameters) {
        N1qlQueryResult result = template.queryN1QL(parameterized(statement, parameters));
        if (isQueryFailed(result)) {
            throw new CouchbaseQueryExecutionException("Error executing N1QL statement '" + statement + "'. " + result.errors());
        }
        return result;
    }

    protected String getBucketName() {
        return couchbase.getBucket().getName();
    }

    protected boolean isQueryFailed(N1qlQueryResult result) {
        return !result.finalSuccess() || CollectionUtils.isNotEmpty(result.errors());
    }

    protected void deleteLastCharacter(StringBuilder statement) {
        statement.deleteCharAt(statement.length() - 1);
    }
}
