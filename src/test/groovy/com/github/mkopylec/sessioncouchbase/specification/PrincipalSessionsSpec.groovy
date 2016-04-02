package com.github.mkopylec.sessioncouchbase.specification

import com.github.mkopylec.sessioncouchbase.BasicSpec
import org.springframework.test.context.TestPropertySource

import static com.github.mkopylec.sessioncouchbase.assertions.Assertions.assertThat

@TestPropertySource(properties = ['session-couchbase.persistent.principal-sessions.enabled: true'])
class PrincipalSessionsSpec extends BasicSpec {

    def "Should get principal HTTP sessions when they exist"() {
        given:
        def firstSessionId = setPrincipalSessionAttribute()
        clearSessionCookie()
        startExtraApplicationInstance()
        def secondSessionId = setPrincipalSessionAttributeToExtraInstance()

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