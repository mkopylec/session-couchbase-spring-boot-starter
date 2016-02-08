package com.github.mkopylec.sessioncouchbase.specification

import com.github.mkopylec.sessioncouchbase.BasicSpec
import com.github.mkopylec.sessioncouchbase.Message

import static com.github.mkopylec.sessioncouchbase.assertions.Assertions.assertThat

class NamespacesSpec extends BasicSpec {

    def "Should set and get HTTP session attribute using the same namespace"() {
        given:
        def message = new Message(text: 'i robot', number: 6)
        setSessionAttribute message
        startExtraApplicationInstance()

        when:
        def response = getSessionAttributeFromExtraInstance()

        then:
        assertThat(response)
                .hasOkStatus()
                .hasBody(message)
    }

    def "Should not get HTTP session attribute using different namespace"() {
        given:
        def message = new Message(text: 'i robot', number: 6)
        setSessionAttribute message
        startExtraApplicationInstance('other_namespace')

        when:
        def response = getSessionAttributeFromExtraInstance()

        then:
        assertThat(response)
                .hasOkStatus()
                .hasNoBody()
    }
}