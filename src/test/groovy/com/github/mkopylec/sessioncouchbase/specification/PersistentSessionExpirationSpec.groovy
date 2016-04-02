package com.github.mkopylec.sessioncouchbase.specification

import com.github.mkopylec.sessioncouchbase.Message

class PersistentSessionExpirationSpec extends SessionExpirationSpec {

    def "Should not get Couchbase session document when HTTP session has expired"() {
        given:
        def message = new Message(text: 'power rangers 2', number: 10001)
        setSessionAttribute message

        when:
        sleep(sessionTimeout + 1000)

        then:
        !currentSessionExists()
    }
}