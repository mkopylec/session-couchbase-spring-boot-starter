package com.github.mkopylec.sessioncouchbase.specification.session

import org.springframework.test.context.ActiveProfiles

@ActiveProfiles('in-memory')
class InMemorySessionExpirationSpec extends SessionExpirationSpec {
}