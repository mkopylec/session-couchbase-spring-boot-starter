//package com.github.mkopylec.sessioncouchbase.core;
//
//import com.github.mkopylec.sessioncouchbase.data.SessionDao;
//import org.springframework.session.Session;
//import org.springframework.session.web.http.CookieHttpSessionStrategy;
//import org.springframework.session.web.http.MultiHttpSessionStrategy;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//public class DelegatingSessionStrategy implements MultiHttpSessionStrategy {
//
//    protected final CookieHttpSessionStrategy sessionStrategy;
//    protected final SessionDao dao;
//
//    public DelegatingSessionStrategy(CookieHttpSessionStrategy sessionStrategy, SessionDao dao) {
//        this.sessionStrategy = sessionStrategy;
//        this.dao = dao;
//    }
//
//    @Override
//    public String getRequestedSessionId(HttpServletRequest request) {
//        return sessionStrategy.getRequestedSessionId(request);
//    }
//
//    @Override
//    public void onNewSession(Session session, HttpServletRequest request, HttpServletResponse response) {
//        sessionStrategy.onNewSession(session, request, response);
//    }
//
//    @Override
//    public void onInvalidateSession(HttpServletRequest request, HttpServletResponse response) {
//        sessionStrategy.onInvalidateSession(request, response);
//    }
//
//    @Override
//    public HttpServletRequest wrapRequest(HttpServletRequest request, HttpServletResponse response) {
//        RequestWrapper wrapper = new RequestWrapper(request, dao);
//        return sessionStrategy.wrapRequest(wrapper, response);
//    }
//
//    @Override
//    public HttpServletResponse wrapResponse(HttpServletRequest request, HttpServletResponse response) {
//        return sessionStrategy.wrapResponse(request, response);
//    }
//}
