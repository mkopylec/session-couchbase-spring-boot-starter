package com.github.mkopylec.sessioncouchbase.utils

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry

class MeterRegistryProvider {

    private static MeterRegistry registry = new SimpleMeterRegistry()

    static MeterRegistry meterRegistry() {
        return registry
    }

    static void clearMetrics() {
        registry.meters.each { registry.remove(it.id) }
    }
}
