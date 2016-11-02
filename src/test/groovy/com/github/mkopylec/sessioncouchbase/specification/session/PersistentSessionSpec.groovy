package com.github.mkopylec.sessioncouchbase.specification.session

import com.github.mkopylec.sessioncouchbase.Message
import org.springframework.web.client.HttpServerErrorException

import static com.github.mkopylec.sessioncouchbase.assertions.Assertions.assertThat

class PersistentSessionSpec extends SessionSpec {

    def "Should copy HTTP session attributes when session ID was changed"() {
        given:
        def message = new Message(text: 'i cannot disappear!', number: 13)
        setSessionAttribute message
        def globalMessage = new Message(text: 'i cannot disappear too!', number: 12222)
        setGlobalSessionAttribute globalMessage
        startExtraApplicationInstance('wicked_application')
        def extraMessage = new Message(text: 'and me too!', number: 14100)
        setSessionAttributeToExtraInstance extraMessage

        when:
        changeSessionId()

        then:
        assertThat(getSessionAttribute())
                .hasBody(message)
        assertThat(getGlobalSessionAttributeFromExtraInstance())
                .hasBody(globalMessage)
        assertThat(getSessionAttributeFromExtraInstance())
                .hasBody(extraMessage)
    }

    def "Should fail to get principal HTTP session when principal HTTP sessions are disabled"() {
        given:
        setPrincipalSessionAttribute()

        when:
        getPrincipalSessions()

        then:
        thrown HttpServerErrorException
    }

    def "Should get global and application namespace HTTP session attribute names"() {
        given:
        def message = new Message(text: 'how do you do', number: 10)
        setGlobalSessionAttribute message
        setSessionAttribute message

        when:
        def response = getSessionAttributeNames()

        then:
        assertThat(response)
                .hasSessionAttributeNames(
                'attribute',
                'com.github.mkopylec.sessioncouchbase.core.CouchbaseSession.global.attribute',
                'com.github.mkopylec.sessioncouchbase.core.CouchbaseSession.global.$lastAccessedTime',
                'com.github.mkopylec.sessioncouchbase.core.CouchbaseSession.global.$creationTime',
                'com.github.mkopylec.sessioncouchbase.core.CouchbaseSession.global.$maxInactiveInterval'
        )
    }
}