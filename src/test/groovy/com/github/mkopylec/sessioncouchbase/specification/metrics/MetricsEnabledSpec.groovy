package com.github.mkopylec.sessioncouchbase.specification.metrics

import com.github.mkopylec.sessioncouchbase.BasicSpec
import com.github.mkopylec.sessioncouchbase.Message
import org.springframework.test.context.ActiveProfiles

import static com.github.mkopylec.sessioncouchbase.assertions.Assertions.assertThat

@ActiveProfiles(['metrics-enabled', 'principal-sessions'])
abstract class MetricsEnabledSpec extends BasicSpec {

    def "Should collect metrics when setting and reading HTTP session attributes"() {
        given:
        def message = new Message(text: 'metrics', number: 0)

        when:
        setSessionAttribute message
        getSessionAttribute()

        then:
        assertThat(registry)
                .hasCapturedLatency('session_couchbase.create_session')
                .hasCapturedLatency('session_couchbase.find_session_by_id')
                .hasCapturedRate('session_couchbase.create_session.created')
                .hasCapturedRate('session_couchbase.find_session_by_id.found')
    }

    def "Should collect metrics when setting and reading principal HTTP sessions"() {
        when:
        setPrincipalSessionAttribute()
        getPrincipalSessions()

        then:
        assertThat(registry)
                .hasCapturedLatency('session_couchbase.create_session')
                .hasCapturedLatency('session_couchbase.find_session_by_id')
                .hasCapturedLatency('session_couchbase.find_session_by_index_name_and_index_value')
                .hasCapturedRate('session_couchbase.create_session.created')
                .hasCapturedRate('session_couchbase.find_session_by_id.found')
                .hasCapturedRate('session_couchbase.find_session_by_index_name_and_index_value.found')
    }

    def "Should collect metrics when invalidating HTTP session"() {
        given:
        def message = new Message(text: 'metrics', number: 0)
        setSessionAttribute message

        when:
        invalidateSession()

        then:
        assertThat(registry)
                .hasCapturedLatency('session_couchbase.delete_session')
                .hasCapturedRate('session_couchbase.delete_session.deleted')
    }

    def "Should collect metrics when reading non-existing HTTP session"() {
        when:
        getNonExistingSessionId()

        then:
        assertThat(registry)
                .hasCapturedLatency('session_couchbase.find_session_by_id')
                .hasCapturedRate('session_couchbase.find_session_by_id.not_found')
    }
}
