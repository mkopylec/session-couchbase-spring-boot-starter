package com.github.mkopylec.sessioncouchbase.specification

import com.github.mkopylec.sessioncouchbase.BasicSpec
import com.github.mkopylec.sessioncouchbase.Message

import static com.github.mkopylec.sessioncouchbase.assertions.Assertions.assertThat

abstract class SessionSpec extends BasicSpec {

    def "Should set and get HTTP session attribute"() {
        given:
        def message = new Message(text: 'flying sausage', number: 666)
        setSessionAttribute message

        when:
        def response = getSessionAttribute()

        then:
        assertThat(response)
                .hasOkStatus()
                .hasBody(message)
    }

    def "Should not get HTTP session attribute when session was invalidated"() {
        given:
        def message = new Message(text: 'godzilla', number: 13)
        setSessionAttribute message
        invalidateSession()

        when:
        def response = getSessionAttribute()

        then:
        assertThat(response)
                .hasOkStatus()
                .hasNoBody()
    }

    def "Should set and get HTTP session scoped bean"() {
        given:
        def message = new Message(text: 'i am batman', number: 69)
        setSessionBean message

        when:
        def response = getSessionBean()

        then:
        assertThat(response)
                .hasOkStatus()
                .hasBody(message)
    }

    def "Should not get HTTP session scoped bean when session was invalidated"() {
        given:
        def message = new Message(text: 'mariusz kopylec', number: 1)
        setSessionBean message
        invalidateSession()

        when:
        def response = getSessionBean()

        then:
        assertThat(response)
                .hasOkStatus()
                .hasBody(new Message(text: null, number: null))
    }

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