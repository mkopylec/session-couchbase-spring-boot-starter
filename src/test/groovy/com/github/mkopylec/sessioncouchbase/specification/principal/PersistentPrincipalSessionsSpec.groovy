package com.github.mkopylec.sessioncouchbase.specification.principal

import static com.github.mkopylec.sessioncouchbase.assertions.Assertions.assertThat

class PersistentPrincipalSessionsSpec extends PrincipalSessionsSpec {

    def "Should get distributed principal HTTP sessions when they exist"() {
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
}