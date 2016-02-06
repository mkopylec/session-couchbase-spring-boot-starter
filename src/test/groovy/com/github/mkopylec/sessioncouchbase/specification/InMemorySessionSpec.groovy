package com.github.mkopylec.sessioncouchbase.specification

import org.springframework.test.context.TestPropertySource

@TestPropertySource(properties = ['session-couchbase.in-memory.enabled: true'])
class InMemorySessionSpec extends SessionSpec {
}