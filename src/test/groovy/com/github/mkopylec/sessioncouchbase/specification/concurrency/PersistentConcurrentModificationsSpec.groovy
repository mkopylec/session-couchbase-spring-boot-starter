package com.github.mkopylec.sessioncouchbase.specification.concurrency

import com.github.mkopylec.sessioncouchbase.BasicSpec
import com.github.mkopylec.sessioncouchbase.Message
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.HttpServerErrorException

@ActiveProfiles(['persistent', 'retryable-queries'])
class PersistentConcurrentModificationsSpec extends BasicSpec {

    def "Should not fail to concurrently set HTTP session attributes when retry is enabled"() {
        given:
        def message = new Message(text: 'message', number: 0)
        setSessionAttribute message
        startExtraApplicationInstance()
        def serverErrorOccurred = false

        when:
        executeConcurrently {
            try {
                setSessionAttribute message
                setSessionAttributeToExtraInstance message
            } catch (HttpServerErrorException ex) {
                serverErrorOccurred = true
            }
        }

        then:
        !serverErrorOccurred
    }
}