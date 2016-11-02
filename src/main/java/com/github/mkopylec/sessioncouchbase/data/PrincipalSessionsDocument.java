package com.github.mkopylec.sessioncouchbase.data;

import com.couchbase.client.java.repository.annotation.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Document
public class PrincipalSessionsDocument {

    @Id
    protected final String principal;
    @Field
    protected final Set<String> sessionIds;

    public PrincipalSessionsDocument(String principal, Set<String> sessionIds) {
        this.principal = principal;
        this.sessionIds = sessionIds;
    }

    public String getPrincipal() {
        return principal;
    }

    public Set<String> getSessionIds() {
        return sessionIds == null ? new HashSet<>() : sessionIds;
    }
}
