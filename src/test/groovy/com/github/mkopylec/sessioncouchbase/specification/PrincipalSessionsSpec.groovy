package com.github.mkopylec.sessioncouchbase.specification

import com.github.mkopylec.sessioncouchbase.BasicSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.couchbase.core.CouchbaseTemplate

import static com.github.mkopylec.sessioncouchbase.SessionController.PRINCIPAL_NAME
import static com.github.mkopylec.sessioncouchbase.assertions.Assertions.assertThat

class PrincipalSessionsSpec extends BasicSpec {

    @Autowired
    private CouchbaseTemplate couchbase

    def "Should get principal sessions when they exist"() {
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

    def "Should not get principal session when it was invalidated"() {
        given:
        setPrincipalSessionAttribute()

        when:
        invalidateSession()

        then:
        assertThat(getPrincipalSessions())
                .hasNoSessionIds()
    }

    void setup() {
        if (couchbase.exists(PRINCIPAL_NAME)) {
            couchbase.remove(PRINCIPAL_NAME)
//            sleep(10000)
        }
    }
}