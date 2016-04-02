package com.github.mkopylec.sessioncouchbase.specification

import com.github.mkopylec.sessioncouchbase.BasicSpec
import org.springframework.test.context.TestPropertySource

import static com.github.mkopylec.sessioncouchbase.assertions.Assertions.assertThat

@TestPropertySource(properties = ['session-couchbase.principal-sessions.enabled: true', 'session-couchbase.timeout-in-seconds: 1'])
class PrincipalSessionsExpirationSpec extends BasicSpec {

    def "Should not get principal HTTP session when HTTP session have expired"() {
        given:
        setPrincipalSessionAttribute()

        when:
        sleep(sessionTimeout + 1000)

        then:
        assertThat(getPrincipalSessions())
                .hasNoSessionIds()
    }
}