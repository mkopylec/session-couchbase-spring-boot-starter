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

    def "Should set and remove HTTP session attribute"() {
        given:
        def message = new Message(text: 'twisted mind', number: 100)
        setSessionAttribute message

        when:
        deleteSessionAttribute()

        then:
        assertThat(getSessionAttribute())
                .hasOkStatus()
                .hasNoBody()
    }

    def "Should not get HTTP session attribute when session was invalidated"() {
        given:
        def message = new Message(text: 'godzilla', number: 13)
        setSessionAttribute message

        when:
        invalidateSession()

        then:
        assertThat(getSessionAttribute())
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
}