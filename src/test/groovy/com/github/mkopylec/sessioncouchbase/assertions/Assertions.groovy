package com.github.mkopylec.sessioncouchbase.assertions

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.http.ResponseEntity

class Assertions {

    static ResponseAssertion assertThat(ResponseEntity response) {
        return new ResponseAssertion(response)
    }

    static MetricsAssertion assertThat(MeterRegistry registry) {
        return new MetricsAssertion(registry)
    }
}
