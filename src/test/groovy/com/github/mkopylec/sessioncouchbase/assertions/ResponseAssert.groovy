package com.github.mkopylec.sessioncouchbase.assertions

import org.springframework.http.ResponseEntity

class ResponseAssert {

    private ResponseEntity actual

    protected ResponseAssert(ResponseEntity actual) {
        assert actual != null
        this.actual = actual
    }

    ResponseAssert hasSessionIds(String... sessionIds) {
        assert actual.body != null
        def actualSessionIds = actual.body as Set
        assert actualSessionIds.size() == sessionIds.size()
        assert actualSessionIds.containsAll(sessionIds)
        return this
    }

    ResponseAssert hasNoSessionIds() {
        assert actual.body != null
        def actualSessionIds = actual.body as Set
        assert actualSessionIds.isEmpty()
        return this
    }

    ResponseAssert hasBody(Object body) {
        assert actual.body == body
        return this
    }

    ResponseAssert hasNoBody() {
        assert actual.body == null
        return this
    }
}
