package com.github.mkopylec.sessioncouchbase.assertions

import org.springframework.http.ResponseEntity

class Assertions {

    static ResponseAssert assertThat(ResponseEntity response) {
        return new ResponseAssert(response)
    }
}
