package com.github.mkopylec.sessioncouchbase.specification.session

import org.springframework.test.context.TestPropertySource

@TestPropertySource(properties = ['session-couchbase.in-memory.enabled: true'])
class InMemorySessionExpirationSpec extends SessionExpirationSpec {
}