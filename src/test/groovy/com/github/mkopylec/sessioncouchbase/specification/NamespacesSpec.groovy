package com.github.mkopylec.sessioncouchbase.specification

import com.github.mkopylec.sessioncouchbase.BasicSpec
import com.github.mkopylec.sessioncouchbase.Message

import static com.github.mkopylec.sessioncouchbase.assertions.Assertions.assertThat

class NamespacesSpec extends BasicSpec {

    def "Should set and get HTTP session attribute only from same namespace"() {
        given:
        def firstMessage = new Message(text: 'i robot', number: 6)
        setSessionAttribute firstMessage
        def secondMessage = new Message(text: 'you robot', number: 9)

        when:
        def response = getSessionAttribute()

        then:
        assertThat(response)
                .hasOkStatus()
                .hasBody(firstMessage)
    }
}