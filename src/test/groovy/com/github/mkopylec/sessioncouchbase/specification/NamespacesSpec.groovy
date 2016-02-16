package com.github.mkopylec.sessioncouchbase.specification

import com.github.mkopylec.sessioncouchbase.BasicSpec
import com.github.mkopylec.sessioncouchbase.Message

import static com.github.mkopylec.sessioncouchbase.assertions.Assertions.assertThat

class NamespacesSpec extends BasicSpec {

    def "Should set and get global HTTP session attribute using the same namespace"() {
        given:
        def message = new Message(text: 'i robot 1', number: 1)
        setGlobalSessionAttribute message
        startExtraApplicationInstance()

        when:
        def response = getGlobalSessionAttributeFromExtraInstance()

        then:
        assertThat(response)
                .hasOkStatus()
                .hasBody(message)
    }

    def "Should set and get HTTP session attribute using the same namespace"() {
        given:
        def message = new Message(text: 'i robot 2', number: 2)
        setSessionAttribute message
        startExtraApplicationInstance()

        when:
        def response = getSessionAttributeFromExtraInstance()

        then:
        assertThat(response)
                .hasOkStatus()
                .hasBody(message)
    }

    def "Should set and get global HTTP session attribute using different namespace"() {
        given:
        def message = new Message(text: 'i robot 3', number: 3)
        setGlobalSessionAttribute message
        startExtraApplicationInstance('other_namespace')

        when:
        def response = getGlobalSessionAttributeFromExtraInstance()

        then:
        assertThat(response)
                .hasOkStatus()
                .hasBody(message)
    }

    def "Should not get HTTP session attribute using different namespace"() {
        given:
        def message = new Message(text: 'i robot 4', number: 4)
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