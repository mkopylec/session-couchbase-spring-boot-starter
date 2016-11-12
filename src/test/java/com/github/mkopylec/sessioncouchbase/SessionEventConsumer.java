package com.github.mkopylec.sessioncouchbase;

import org.springframework.context.event.EventListener;
import org.springframework.session.events.SessionCreatedEvent;
import org.springframework.session.events.SessionDeletedEvent;
import org.springframework.session.events.SessionExpiredEvent;
import org.springframework.stereotype.Component;

@Component
public class SessionEventConsumer {

    private boolean sessionCreated;
    private boolean sessionExpired;
    private boolean sessionDeleted;

    @EventListener
    public void onSessionCreated(SessionCreatedEvent createdEvent) {
        sessionCreated = true;
    }

    @EventListener
    public void onSessionExpired(SessionExpiredEvent expiredEvent) {
        sessionExpired = true;
    }

    @EventListener
    public void onSessionDeleted(SessionDeletedEvent deletedEvent) {
        sessionDeleted = true;
    }

    public boolean isSessionCreated() {
        return sessionCreated;
    }

    public boolean isSessionExpired() {
        return sessionExpired;
    }

    public boolean isSessionDeleted() {
        return sessionDeleted;
    }

    public void resetAssertions() {
        sessionCreated = false;
        sessionExpired = false;
        sessionDeleted = false;
    }
}
