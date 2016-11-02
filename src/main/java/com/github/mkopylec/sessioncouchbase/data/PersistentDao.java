package com.github.mkopylec.sessioncouchbase.data;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.data.couchbase.core.CouchbaseQueryExecutionException;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.retry.support.RetryTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.couchbase.client.java.document.json.JsonArray.from;
import static com.couchbase.client.java.query.N1qlQuery.parameterized;
import static org.springframework.util.Assert.isTrue;

public class PersistentDao implements SessionDao {

    protected final String bucket;
    protected final CouchbaseTemplate couchbaseTemplate;
    protected final RetryTemplate retryTemplate;

    public PersistentDao(CouchbaseProperties couchbase, CouchbaseTemplate couchbaseTemplate, RetryTemplate retryTemplate) {
        bucket = couchbase.getBucket().getName();
        this.couchbaseTemplate = couchbaseTemplate;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public void insertNamespace(String namespace, String id) {
        String statement = "UPDATE " + bucket + " USE KEYS $1 SET data.`" + namespace + "` = {}";
        executeQuery(statement, from(id, namespace));
    }

    @Override
    public void updateSession(Map<String, Object> attributesToUpdate, Set<String> attributesToRemove, String namespace, String id) {
        StringBuilder statement = new StringBuilder("UPDATE ").append(bucket).append(" USE KEYS $1");
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

    @Override
    public void updatePutPrincipalSession(String principal, String sessionId) {
        String statement = "UPDATE " + bucket + " USE KEYS $1 SET sessionIds = ARRAY_PUT(sessionIds, $2)";
        executeQuery(statement, from(principal, sessionId));
    }

    @Override
    public void updateRemovePrincipalSession(String principal, String sessionId) {
        String statement = "UPDATE " + bucket + " USE KEYS $1 SET sessionIds = ARRAY_REMOVE(sessionIds, $2)";
        executeQuery(statement, from(principal, sessionId));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> findSessionAttributes(String id, String namespace) {
        String statement = "SELECT * FROM " + bucket + ".data.`" + namespace + "` USE KEYS $1";
        N1qlQueryResult result = executeQuery(statement, from(id));
        List<N1qlQueryRow> attributes = result.allRows();
        isTrue(attributes.size() < 2, "Invalid HTTP session state. Multiple namespaces '" + namespace + "' for session ID '" + id + "'");
        if (attributes.isEmpty()) {
            return null;
        }
        return (Map<String, Object>) attributes.get(0).value().toMap().get(namespace);
    }

    @Override
    public SessionDocument findById(String id) {
        return couchbaseTemplate.findById(id, SessionDocument.class);
    }

    @Override
    public PrincipalSessionsDocument findByPrincipal(String principal) {
        return couchbaseTemplate.findById(principal, PrincipalSessionsDocument.class);
    }

    @Override
    public void updateExpirationTime(String id, int expiry) {
        couchbaseTemplate.getCouchbaseBucket().touch(id, expiry);
    }

    @Override
    public void save(SessionDocument document) {
        couchbaseTemplate.save(document);
    }

    @Override
    public void save(PrincipalSessionsDocument document) {
        couchbaseTemplate.save(document);
    }

    @Override
    public boolean exists(String documentId) {
        return couchbaseTemplate.exists(documentId);
    }

    @Override
    public void delete(String id) {
        try {
            couchbaseTemplate.remove(id);
        } catch (DataRetrievalFailureException ex) {
            if (!(ex.getCause() instanceof DocumentDoesNotExistException)) {
                throw ex;
            }
            //Do nothing
        }
    }

    @Override
    public void deleteAll() {
        String statement = "DELETE FROM " + bucket;
        executeQuery(statement, from());
    }

    protected N1qlQueryResult executeQuery(String statement, JsonArray parameters) {
        return retryTemplate.execute(context -> {
            N1qlQueryResult result = couchbaseTemplate.queryN1QL(parameterized(statement, parameters));
            if (isQueryFailed(result)) {
                throw new CouchbaseQueryExecutionException("Error executing N1QL statement '" + statement + "'. " + result.errors());
            }
            return result;
        });
    }

    protected boolean isQueryFailed(N1qlQueryResult result) {
        return !result.finalSuccess() || CollectionUtils.isNotEmpty(result.errors());
    }

    protected void deleteLastCharacter(StringBuilder statement) {
        statement.deleteCharAt(statement.length() - 1);
    }
}
