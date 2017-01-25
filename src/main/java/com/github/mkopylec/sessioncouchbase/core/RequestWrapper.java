package com.github.mkopylec.sessioncouchbase.core;

import com.github.mkopylec.sessioncouchbase.data.SessionDao;
import com.github.mkopylec.sessioncouchbase.data.SessionDocument;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.session.web.http.SessionRepositoryFilter.SESSION_REPOSITORY_ATTR;
import static org.springframework.util.Assert.notNull;

public class RequestWrapper extends HttpServletRequestWrapper {

    public final String CURRENT_SESSION_ATTR = SESSION_REPOSITORY_ATTR + ".CURRENT_SESSION";

    private static final Logger log = getLogger(RequestWrapper.class);

    protected final SessionDao dao;

    public RequestWrapper(HttpServletRequest request, SessionDao dao) {
        super(request);
        this.dao = dao;
    }

    @Override
    public String changeSessionId() {
        SessionDocument oldDocument = dao.findById(getRequestedSessionId());
        notNull(oldDocument, "Cannot change HTTP session ID, because session document with ID '" + getRequestedSessionId() + "' does not exist in data storage");
        HttpSession oldSession = getSession(false);
        notNull(oldSession, "Cannot change HTTP session ID, because session with ID '" + getRequestedSessionId() + "' does not exist");

        removeAttribute(CURRENT_SESSION_ATTR);
        dao.delete(oldDocument.getId());

        HttpSession newSession = getSession();
        SessionDocument newDocument = new SessionDocument(newSession.getId(), oldDocument.getData());
        dao.save(newDocument);

        copyAttributes(oldSession, newSession);

        log.debug("HTTP session ID has changed from {} to {}", oldSession.getId(), newDocument.getId());

        return newDocument.getId();
    }

    protected void copyAttributes(HttpSession oldSession, HttpSession newSession) {
        Map<String, Object> attributes = new HashMap<>();
        Enumeration<String> attributeNames = oldSession.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            attributes.put(name, oldSession.getAttribute(name));
        }
        attributes.forEach(newSession::setAttribute);
    }
}
