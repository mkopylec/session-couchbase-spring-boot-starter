package com.github.mkopylec.sessioncouchbase.data;

import java.util.ArrayList;
import java.util.List;

public class PrincipalSessionsDocument {

    protected final String principal;
    protected final List<String> sessionIds;

    public PrincipalSessionsDocument(String principal, List<String> sessionIds) {
        this.principal = principal;
        this.sessionIds = sessionIds;
    }

    public String getPrincipal() {
        return principal;
    }

    public List<String> getSessionIds() {
        return sessionIds == null ? new ArrayList<>() : sessionIds;
    }
}
