package com.github.mkopylec.sessioncouchbase.persistent;

import org.springframework.session.Session;
import org.springframework.session.web.http.CookieHttpSessionStrategy;
import org.springframework.session.web.http.MultiHttpSessionStrategy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DelegatingSessionStrategy implements MultiHttpSessionStrategy {

    protected final CookieHttpSessionStrategy sessionStrategy;
    protected final CouchbaseDao dao;
    protected final String namespace;
    protected final Serializer serializer;

    public DelegatingSessionStrategy(CookieHttpSessionStrategy sessionStrategy, CouchbaseDao dao, String namespace, Serializer serializer) {
        this.sessionStrategy = sessionStrategy;
        this.dao = dao;
        this.namespace = namespace;
        this.serializer = serializer;
    }

    @Override
    public String getRequestedSessionId(HttpServletRequest request) {
        return sessionStrategy.getRequestedSessionId(request);
    }

    @Override
    public void onNewSession(Session session, HttpServletRequest request, HttpServletResponse response) {
        sessionStrategy.onNewSession(session, request, response);
    }

    @Override
    public void onInvalidateSession(HttpServletRequest request, HttpServletResponse response) {
        sessionStrategy.onInvalidateSession(request, response);
    }

    @Override
    public HttpServletRequest wrapRequest(HttpServletRequest request, HttpServletResponse response) {
        RequestWrapper wrapper = new RequestWrapper(request, dao, namespace, serializer);
        return sessionStrategy.wrapRequest(wrapper, response);
    }

    @Override
    public HttpServletResponse wrapResponse(HttpServletRequest request, HttpServletResponse response) {
        return sessionStrategy.wrapResponse(request, response);
    }
}
