package org.example.bot.mail.session.domain;

import jakarta.mail.Session;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;

@Getter
@Setter(AccessLevel.PRIVATE)
public class SessionReadyEvent extends ApplicationEvent {

    private Long userId;
    private Session session;

    public SessionReadyEvent(Object source) {
        super(source);
    }

    public SessionReadyEvent(Object source, Clock clock) {
        super(source, clock);
    }


    public static SessionReadyEventBuilder builder(Object source) {
        return new SessionReadyEventBuilder(source);
    }

    public static final class SessionReadyEventBuilder {

        private final Object source;
        private Long userId;
        private Session session;

        private SessionReadyEventBuilder(Object source) {
            this.source = source;
        }

        public SessionReadyEventBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public SessionReadyEventBuilder session(Session session) {
            this.session = session;
            return this;
        }

        public SessionReadyEvent build() {
            SessionReadyEvent sessionReadyEvent = new SessionReadyEvent(source);
            sessionReadyEvent.setUserId(userId);
            sessionReadyEvent.setSession(session);
            return sessionReadyEvent;
        }
    }
}
