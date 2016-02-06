package com.github.mkopylec.sessioncouchbase.assertions

import org.springframework.http.ResponseEntity

import static org.springframework.http.HttpStatus.OK

class ResponseAssert {

    private ResponseEntity actual

    protected ResponseAssert(ResponseEntity actual) {
        assert actual != null
        this.actual = actual
    }

    ResponseAssert hasBody(Object body) {
        assert actual.body == body
        return this
    }

    ResponseAssert hasNoBody() {
        assert actual.body == null
        return this
    }

    ResponseAssert hasOkStatus() {
        assert actual.statusCode == OK
        return this
    }
}
