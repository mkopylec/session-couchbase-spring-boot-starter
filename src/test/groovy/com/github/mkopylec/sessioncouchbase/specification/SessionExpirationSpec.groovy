package com.github.mkopylec.sessioncouchbase.specification

import com.github.mkopylec.sessioncouchbase.BasicSpec
import com.github.mkopylec.sessioncouchbase.Message
import org.springframework.test.context.TestPropertySource

import static com.github.mkopylec.sessioncouchbase.assertions.Assertions.assertThat

@TestPropertySource(properties = ['session-couchbase.timeout-in-seconds: 1'])
abstract class SessionExpirationSpec extends BasicSpec {

    def "Should not get HTTP session attribute when session has expired"() {
        given:
        def message = new Message(text: 'power rangers', number: 123)
        setSessionAttribute message
        sleep(sessionTimeout + 100)

        when:
        def response = getSessionAttribute()

        then:
        assertThat(response)
                .hasOkStatus()
                .hasNoBody()
    }
}