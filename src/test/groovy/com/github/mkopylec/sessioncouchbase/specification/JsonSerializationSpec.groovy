package com.github.mkopylec.sessioncouchbase.specification

import com.github.mkopylec.sessioncouchbase.BasicSpec
import com.github.mkopylec.sessioncouchbase.Message
import org.springframework.test.context.TestPropertySource

import static com.github.mkopylec.sessioncouchbase.assertions.Assertions.assertThat

@TestPropertySource(properties = ['session-couchbase.in-memory.enabled: false', 'session-couchbase.persistent.json-serialization: true'])
class JsonSerializationSpec extends BasicSpec {

    def "Should set and get HTTP session attribute"() {
        given:
        def message = new Message(text: 'america', number: 1492)
        setSessionAttribute message

        when:
        def response = getSessionAttribute()

        then:
        assertThat(response)
                .hasOkStatus()
                .hasBody(message)
    }
}