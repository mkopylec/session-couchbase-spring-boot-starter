package com.github.mkopylec.sessioncouchbase.specification.session

import org.springframework.test.context.ActiveProfiles

@ActiveProfiles('persistent')
class PersistentSessionExpirationSpec extends SessionExpirationSpec {
}