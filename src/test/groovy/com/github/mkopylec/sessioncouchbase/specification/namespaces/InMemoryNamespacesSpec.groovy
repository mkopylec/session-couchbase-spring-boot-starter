package com.github.mkopylec.sessioncouchbase.specification.namespaces

import com.github.mkopylec.sessioncouchbase.BasicSpec
import com.github.mkopylec.sessioncouchbase.Message
import org.springframework.test.context.ActiveProfiles

import static com.github.mkopylec.sessioncouchbase.assertions.Assertions.assertThat

@ActiveProfiles('in-memory')
class InMemoryNamespacesSpec extends BasicSpec {

    def "Should set and get global HTTP session attribute using the same namespace"() {
        given:
        def message = new Message(text: 'i robot 1', number: 1)
        setGlobalSessionAttribute message

        when:
        def response = getGlobalSessionAttribute()

        then:
        assertThat(response)
                .hasBody(message)
    }

    def "Should set and get HTTP session attribute using the same namespace"() {
        given:
        def message = new Message(text: 'i robot 2', number: 2)
        setSessionAttribute message

        when:
        def response = getSessionAttribute()

        then:
        assertThat(response)
                .hasBody(message)
    }
}