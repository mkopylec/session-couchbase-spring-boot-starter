package com.github.mkopylec.sessioncouchbase.specification.metrics

import com.github.mkopylec.sessioncouchbase.BasicSpec

abstract class MetricsDisabledSpec extends BasicSpec {

    def "Should not collect metrics when it is disabled"() {
        expect:
        registry == null
    }
}
