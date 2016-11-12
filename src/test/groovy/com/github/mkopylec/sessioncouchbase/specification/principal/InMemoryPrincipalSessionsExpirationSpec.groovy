package com.github.mkopylec.sessioncouchbase.specification.principal

import org.springframework.test.context.ActiveProfiles

@ActiveProfiles('in-memory')
class InMemoryPrincipalSessionsExpirationSpec extends PrincipalSessionsExpirationSpec {
}