package com.github.mkopylec.sessioncouchbase.specification.principal

import com.github.mkopylec.sessioncouchbase.BasicSpec
import org.springframework.test.context.ActiveProfiles

import static com.github.mkopylec.sessioncouchbase.assertions.Assertions.assertThat

@ActiveProfiles(['quick-expiration', 'principal-sessions'])
abstract class PrincipalSessionsExpirationSpec extends BasicSpec {

    def "Should not get principal HTTP session when HTTP session have expired"() {
        given:
        setPrincipalSessionAttribute()
        sleep(sessionTimeout + 100)

        when:
        def response = getPrincipalSessions()

        then:
        sessionExpiredEventSent()
        assertThat(response)
                .hasNoSessionIds()
        !currentPrincipalSessionsExists()
    }
}