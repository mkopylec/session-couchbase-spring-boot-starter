package com.github.mkopylec.sessioncouchbase.specification

import com.github.mkopylec.sessioncouchbase.Message
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.couchbase.core.CouchbaseTemplate

class PersistentSessionExpirationSpec extends SessionExpirationSpec {

    @Autowired
    private CouchbaseTemplate couchbaseTemplate

    def "Should not get Couchbase session document when session has expired"() {
        given:
        def message = new Message(text: 'power rangers 2', number: 10001)
        setSessionAttribute message
        sleep(sessionTimeout + 1000)

        when:
        def session = couchbaseTemplate.findById(currentSessionId, Object)

        then:
        session == null
    }
}