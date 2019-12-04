package com.github.mkopylec.sessioncouchbase.assertions

import org.springframework.http.ResponseEntity

class ResponseAssertion {

    private ResponseEntity actual

    protected ResponseAssertion(ResponseEntity actual) {
        assert actual != null
        this.actual = actual
    }

    ResponseAssertion hasSessionIds(String... sessionIds) {
        hasElements(sessionIds)
    }

    ResponseAssertion hasSessionAttributeNames(String... attributeNames) {
        return hasElements(attributeNames)
    }

    ResponseAssertion hasNoSessionIds() {
        assert actual.body != null
        def actualSessionIds = actual.body as Set
        assert actualSessionIds.isEmpty()
        return this
    }

    ResponseAssertion hasBody(Object body) {
        assert actual.body == body
        return this
    }

    ResponseAssertion hasNoBody() {
        assert actual.body == null
        return this
    }

    private ResponseAssertion hasElements(String... elements) {
        assert actual.body != null
        def actualSessionIds = actual.body as List
        assert actualSessionIds.size() == elements.size()
        assert actualSessionIds.containsAll(elements)
        return this
    }
}
