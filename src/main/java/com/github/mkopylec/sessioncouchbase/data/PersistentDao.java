package com.github.mkopylec.sessioncouchbase.data;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.couchbase.client.java.query.consistency.ScanConsistency;
import com.github.mkopylec.sessioncouchbase.configuration.SessionCouchbaseProperties;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.data.couchbase.core.CouchbaseQueryExecutionException;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.retry.support.RetryTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.couchbase.client.java.document.json.JsonArray.from;
import static com.couchbase.client.java.document.json.JsonObject.create;
import static com.couchbase.client.java.query.N1qlParams.build;
import static com.couchbase.client.java.query.N1qlQuery.parameterized;
import static java.util.stream.Collectors.toList;

// TODO document per namespace
public class PersistentDao implements SessionDao {

    protected final String bucket;
    protected final ScanConsistency queryConsistency;
    protected final CouchbaseTemplate couchbaseTemplate;
    protected final RetryTemplate retryTemplate;

    public PersistentDao(SessionCouchbaseProperties sessionCouchbase, CouchbaseTemplate couchbaseTemplate, RetryTemplate retryTemplate) {
        bucket = couchbaseTemplate.getCouchbaseBucket().name();
        queryConsistency = sessionCouchbase.getPersistent().getQueryConsistency();
        this.couchbaseTemplate = couchbaseTemplate;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public void insertNamespace(String namespace, String id) {
        String statement = "UPDATE `" + bucket + "` USE KEYS $1 SET data.`" + namespace + "` = {}";
        executeQuery(statement, from(id, namespace));
    }

    @Override
    public void updateSession(Map<String, Object> attributesToUpdate, Set<String> attributesToRemove, String namespace, String id) {
        StringBuilder statement = new StringBuilder("UPDATE `").append(bucket).append("` USE KEYS $1");
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
        String statement = "UPDATE `" + bucket + "` USE KEYS $1 SET sessionIds = ARRAY_PUT(sessionIds, $2)";
        executeQuery(statement, from(principal, sessionId));
    }

    @Override
    public void updateRemovePrincipalSession(String principal, String sessionId) {
        String statement = "UPDATE `" + bucket + "` USE KEYS $1 SET sessionIds = ARRAY_REMOVE(sessionIds, $2)";
        executeQuery(statement, from(principal, sessionId));
    }

    @Override
    public Map<String, Object> findSessionAttributes(String id, String namespace) {
        String statement = "SELECT data.`" + namespace + "` FROM `" + bucket + "` USE KEYS $1";
        N1qlQueryResult result = executeQuery(statement, from(id));
        JsonObject document = getDocument(namespace, result);
        if (document == null) {
            return null;
        }
        return document.toMap();
    }

    @Override
    public PrincipalSessionsDocument findByPrincipal(String principal) {
        JsonObject document = findByDocumentKey(principal);
        if (document == null) {
            return null;
        }
        List<String> sessionIds = document.getArray("sessionIds").toList().stream()
                .map(sessionId -> (String) sessionId)
                .collect(toList());
        return new PrincipalSessionsDocument(principal, sessionIds);
    }

    @Override
    public void updateExpirationTime(String id, int expiry) {
        couchbaseTemplate.getCouchbaseBucket().touch(id, expiry);
    }

    @Override
    public void save(SessionDocument document) {
        String statement = "UPSERT INTO `" + bucket + "` (KEY, VALUE) VALUES ($1, $2)";
        JsonObject json = create().put("data", document.getData());
        executeQuery(statement, from(document.getId(), json));
    }

    @Override
    public void save(PrincipalSessionsDocument document) {
        String statement = "UPSERT INTO `" + bucket + "` (KEY, VALUE) VALUES ($1, $2)";
        JsonObject json = create().put("sessionIds", document.getSessionIds());
        executeQuery(statement, from(document.getPrincipal(), json));
    }

    @Override
    public boolean exists(String documentId) {
        String statement = "SELECT * FROM `" + bucket + "` USE KEYS $1";
        N1qlQueryResult result = executeQuery(statement, from(documentId));
        return result.rows().hasNext();
    }

    @Override
    public void delete(String id) {
        String statement = "DELETE FROM `" + bucket + "` USE KEYS $1";
        executeQuery(statement, from(id));
    }

    @Override
    public void deleteAll() {
        String statement = "DELETE FROM `" + bucket + "`";
        executeQuery(statement, from());
    }

    protected JsonObject findByDocumentKey(String key) {
        String statement = "SELECT * FROM `" + bucket + "` USE KEYS $1";
        N1qlQueryResult result = executeQuery(statement, from(key));
        return getDocument(bucket, result);
    }

    protected JsonObject getDocument(String rootNode, N1qlQueryResult result) {
        List<N1qlQueryRow> attributes = result.allRows();
        if (attributes.isEmpty()) {
            return null;
        }
        return attributes.get(0).value().getObject(rootNode);
    }

    protected N1qlQueryResult executeQuery(String statement, JsonArray parameters) {
        return retryTemplate.execute(context -> {
            N1qlQueryResult result = couchbaseTemplate.queryN1QL(parameterized(statement, parameters, build().consistency(queryConsistency)));
            if (hasQueryFailed(result)) {
                throw new CouchbaseQueryExecutionException("Error executing N1QL statement '" + statement + "'. " + result.errors());
            }
            return result;
        });
    }

    protected boolean hasQueryFailed(N1qlQueryResult result) {
        return !result.finalSuccess() || CollectionUtils.isNotEmpty(result.errors());
    }

    protected void deleteLastCharacter(StringBuilder statement) {
        statement.deleteCharAt(statement.length() - 1);
    }
}
