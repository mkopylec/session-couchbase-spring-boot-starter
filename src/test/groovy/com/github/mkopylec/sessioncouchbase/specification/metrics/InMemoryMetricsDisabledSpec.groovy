package com.github.mkopylec.sessioncouchbase.specification.metrics

import org.springframework.test.context.ActiveProfiles

@ActiveProfiles('in-memory')
class InMemoryMetricsDisabledSpec extends MetricsDisabledSpec {
}
