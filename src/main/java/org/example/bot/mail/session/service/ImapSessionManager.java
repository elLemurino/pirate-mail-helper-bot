package org.example.bot.mail.session.service;

import jakarta.mail.Authenticator;
import jakarta.mail.Session;
import lombok.RequiredArgsConstructor;
import org.example.bot.jooq.tables.records.MailSettingsRecord;
import org.example.bot.mail.session.domain.SessionReadyEvent;
import org.example.bot.mail.settings.repository.MailSettingsRepository;
import org.example.bot.utils.DefaultAuthenticator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;

@Component
@RequiredArgsConstructor
public class ImapSessionManager implements CommandLineRunner {

    private final MailSettingsRepository mailSettingsRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void run(String... args) throws Exception {
        registerSessions();
    }

    private void registerSessions() {

        List<MailSettingsRecord> mailSettings = mailSettingsRepository.findAll();

        mailSettings.forEach(
                mailSettingsRecord -> {
                    Properties properties = new Properties();
                    properties.put("mail.imap.host", mailSettingsRecord.getMailImapHost());
                    properties.put("mail.imap.port", mailSettingsRecord.getMailImapPort());
                    properties.put("mail.imap.ssl.enable", mailSettingsRecord.getMailImapSslEnabled());
                    properties.put("mail.imap.auth", "true");
                    properties.put("mail.imap.connectiontimeout", 5000);
                    properties.put("mail.imap.timeout", 5000);

                    Authenticator authenticator = new DefaultAuthenticator(mailSettingsRecord.getMailUser(), mailSettingsRecord.getMailPassword());

                    Session session = Session.getInstance(properties, authenticator);

                    SessionReadyEvent sessionReadyEvent = SessionReadyEvent.builder(this)
                            .userId(mailSettingsRecord.getUserId())
                            .session(session)
                            .build();

                    applicationEventPublisher.publishEvent(sessionReadyEvent);
                });
    }
}
