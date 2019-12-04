package com.github.mkopylec.sessioncouchbase.assertions

import io.micrometer.core.instrument.MeterRegistry

class MetricsAssertion {

    private MeterRegistry registry

    protected MetricsAssertion(MeterRegistry registry) {
        assert registry != null
        this.registry = registry
    }

    MetricsAssertion hasCapturedLatency(String metricsName) {
        assert registry.timer(metricsName).count() > 0
        return this
    }

    MetricsAssertion hasCapturedRate(String metricsName) {
        assert registry.counter(metricsName).count() > 0
        return this
    }

    MetricsAssertion hasCapturedNothing() {
        assert registry.meters.isEmpty()
        return this
    }
}
