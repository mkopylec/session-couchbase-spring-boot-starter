package com.github.mkopylec.sessioncouchbase.specification.principal

import org.springframework.test.context.TestPropertySource

@TestPropertySource(properties = ['session-couchbase.in-memory.enabled: true'])
class InMemoryPrincipalSessionsSpec extends PrincipalSessionsSpec {
}