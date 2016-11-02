package com.github.mkopylec.sessioncouchbase.specification.concurrency

import com.github.mkopylec.sessioncouchbase.BasicSpec
import com.github.mkopylec.sessioncouchbase.Message
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.HttpServerErrorException

@ActiveProfiles('in-memory')
class InMemoryConcurrentModificationsSpec extends BasicSpec {

    def "Should not fail to concurrently set HTTP session attributes"() {
        given:
        def message = new Message(text: 'message', number: 0)
        setSessionAttribute message
        def serverErrorOccurred = false

        when:
        executeConcurrently {
            try {
                setSessionAttribute message
            } catch (HttpServerErrorException ex) {
                serverErrorOccurred = true
            }
        }

        then:
        !serverErrorOccurred
    }
}