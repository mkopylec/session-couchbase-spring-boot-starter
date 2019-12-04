package com.github.mkopylec.sessioncouchbase.specification.metrics

import org.springframework.test.context.ActiveProfiles

@ActiveProfiles('persistent')
class PersistentMetricsDisabledSpec extends MetricsDisabledSpec {
}
