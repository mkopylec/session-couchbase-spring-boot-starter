package com.github.mkopylec.sessioncouchbase.specification.namespaces

import com.github.mkopylec.sessioncouchbase.BasicSpec
import com.github.mkopylec.sessioncouchbase.Message
import org.springframework.test.context.ActiveProfiles

import static com.github.mkopylec.sessioncouchbase.assertions.Assertions.assertThat

@ActiveProfiles('persistent')
class PersistentNamespacesSpec extends BasicSpec {

    def "Should set and get global HTTP session attribute using the same namespace"() {
        given:
        def message = new Message(text: 'i robot 1', number: 1)
        setGlobalSessionAttribute message
        startExtraApplicationInstance()

        when:
        def response = getGlobalSessionAttributeFromExtraInstance()

        then:
        assertThat(response)
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
                .hasBody(message)
    }

    def "Should set and get global HTTP session attribute using different namespace"() {
        given:
        def message = new Message(text: 'i robot 3', number: 3)
        setGlobalSessionAttribute message
        startExtraApplicationInstance('different-namespace')

        when:
        def response = getGlobalSessionAttributeFromExtraInstance()

        then:
        assertThat(response)
                .hasBody(message)
    }

    def "Should not get HTTP session attribute using different namespace"() {
        given:
        def message = new Message(text: 'i robot 4', number: 4)
        setSessionAttribute message
        startExtraApplicationInstance('different-namespace')

        when:
        def response = getSessionAttributeFromExtraInstance()

        then:
        assertThat(response)
                .hasNoBody()
    }

    def "Should set and remove global HTTP session attribute using different namespace"() {
        given:
        def message = new Message(text: 'delete me', number: 71830)
        setGlobalSessionAttribute message
        startExtraApplicationInstance('different-namespace')

        when:
        deleteGlobalSessionAttribute()

        then:
        assertThat(getGlobalSessionAttributeFromExtraInstance())
                .hasNoBody()
    }
}