package com.github.mkopylec.sessioncouchbase.persistent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Map.Entry;

import static com.github.mkopylec.sessioncouchbase.persistent.CouchbaseSession.globalAttributeName;
import static com.github.mkopylec.sessioncouchbase.persistent.CouchbaseSessionRepository.GLOBAL_NAMESPACE;
import static org.springframework.util.Assert.notNull;

public class RequestWrapper extends HttpServletRequestWrapper {

    public final String CURRENT_SESSION_ATTR = HttpServletRequestWrapper.class.getName();

    protected final CouchbaseDao dao;
    protected final String namespace;
    protected final Serializer serializer;

    public RequestWrapper(HttpServletRequest request, CouchbaseDao dao, String namespace, Serializer serializer) {
        super(request);
        this.dao = dao;
        this.namespace = namespace;
        this.serializer = serializer;
    }

    @Override
    public String changeSessionId() {
        SessionEntity oldEntity = dao.findById(getRequestedSessionId());
        notNull(oldEntity, "Cannot change HTTP session ID, because session with ID '" + getRequestedSessionId() + "' does not exist");

        removeAttribute(CURRENT_SESSION_ATTR);
        dao.delete(oldEntity.getId());

        HttpSession newSession = getSession();
        SessionEntity newEntity = new SessionEntity(newSession.getId(), oldEntity.getData());
        dao.save(newEntity);

        copyGlobalAttributes(oldEntity, newSession);
        copyNamespaceAttributes(oldEntity, newSession);

        return newEntity.getId();
    }

    protected void copyGlobalAttributes(SessionEntity oldEntity, HttpSession newSession) {
        Map<String, Object> attributes = oldEntity.getData().get(GLOBAL_NAMESPACE);
        if (attributes != null) {
            Map<String, Object> deserializedAttributes = serializer.deserializeSessionAttributes(attributes);
            for (Entry<String, Object> attribute : deserializedAttributes.entrySet()) {
                newSession.setAttribute(globalAttributeName(attribute.getKey()), attribute.getValue());
            }
        }
    }

    protected void copyNamespaceAttributes(SessionEntity oldEntity, HttpSession newSession) {
        Map<String, Object> attributes = oldEntity.getData().get(namespace);
        if (attributes != null) {
            Map<String, Object> deserializedAttributes = serializer.deserializeSessionAttributes(attributes);
            for (Entry<String, Object> attribute : deserializedAttributes.entrySet()) {
                newSession.setAttribute(attribute.getKey(), attribute.getValue());
            }
        }
    }
}
