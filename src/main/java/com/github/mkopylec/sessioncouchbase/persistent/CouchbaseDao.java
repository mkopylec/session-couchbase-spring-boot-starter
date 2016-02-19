package com.github.mkopylec.sessioncouchbase.persistent;

import com.couchbase.client.java.document.json.JsonObject;
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

    @SuppressWarnings("unchecked")
    public Map<String, Object> findSessionAttributes(String id, String namespace) {
        List<N1qlQueryRow> attributes = couchbase.queryN1QL(parameterized("SELECT * FROM default.data.`" + namespace + "` USE KEYS $1", from(id))).allRows();
        isTrue(attributes.size() < 2, "Invalid HTTP session state. Multiple namespaces '" + namespace + "' for session ID '" + id + "'");
        if (attributes.isEmpty()) {
            return null;
        }
        return (Map<String, Object>) attributes.get(0).value().toMap().get(namespace);
    }

    public SessionEntity findById(String id) {
        return couchbase.findById(id, SessionEntity.class);
    }

    public void save(SessionEntity entity) {
        couchbase.save(entity);
    }

    public void delete(String id) {
        couchbase.remove(id);
    }
}
