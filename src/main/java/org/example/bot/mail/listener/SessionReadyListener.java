package org.example.bot.mail.listener;

import jakarta.mail.MessagingException;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.exceptions.NoSuchMailProviderException;
import org.example.bot.exceptions.StoreMessagingException;
import org.example.bot.mail.rules.service.RulesEvaluator;
import org.example.bot.mail.service.MailHandler;
import org.example.bot.mail.session.domain.SessionReadyEvent;
import org.example.bot.scheduler.NamedTaskScheduler;
import org.example.bot.scheduler.model.NamedTask;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionReadyListener implements ApplicationListener<SessionReadyEvent> {

    private final NamedTaskScheduler taskScheduler;
    private final RulesEvaluator rulesEvaluator;

    @Override
    public void onApplicationEvent(SessionReadyEvent event) {

        Runnable task = () -> {

            final Long userId = event.getUserId();

            if (!rulesEvaluator.rulesExist(userId)) {
                log.info("no rules -> nothing to do");
                return;
            }

            Session session = event.getSession();

            try {
                try (Store store = session.getStore("imap")) {
                    store.connect();
                    MailHandler mailHandler = new MailHandler(userId, store, rulesEvaluator);
                    mailHandler.handle();
                }
            } catch (NoSuchProviderException e) {
                throw new NoSuchMailProviderException(e);
            } catch (MessagingException e) {
                throw new StoreMessagingException(e);
            }
        };

        NamedTask namedTask = NamedTask.builder()
                .id(String.format("%s", event.getUserId()))
                .task(task)
                .build();

        taskScheduler.scheduleAtFixedRate(namedTask, Duration.ofSeconds(30));
    }
}
