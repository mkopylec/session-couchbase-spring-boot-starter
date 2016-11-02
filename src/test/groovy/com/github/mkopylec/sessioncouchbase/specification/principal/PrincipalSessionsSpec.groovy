package com.github.mkopylec.sessioncouchbase.specification.principal

import com.github.mkopylec.sessioncouchbase.BasicSpec
import org.springframework.test.context.TestPropertySource

import static com.github.mkopylec.sessioncouchbase.assertions.Assertions.assertThat

@TestPropertySource(properties = ['session-couchbase.principal-sessions.enabled: true'])
abstract class PrincipalSessionsSpec extends BasicSpec {

    def "Should get principal HTTP sessions when they exist"() {
        given:
        def firstSessionId = setPrincipalSessionAttribute()
        clearSessionCookie()
        def secondSessionId = setPrincipalSessionAttribute()

        when:
        def response = getPrincipalSessions()

        then:
        assertThat(response)
                .hasSessionIds(firstSessionId, secondSessionId)
    }

    def "Should not get principal HTTP session when it was invalidated"() {
        given:
        setPrincipalSessionAttribute()

        when:
        invalidateSession()

        then:
        assertThat(getPrincipalSessions())
                .hasNoSessionIds()
    }
}