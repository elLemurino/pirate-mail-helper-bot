package org.example.bot.mail.session.service;

import jakarta.mail.Authenticator;
import jakarta.mail.Session;
import lombok.RequiredArgsConstructor;
import org.example.bot.jooq.tables.records.MailSettingsRecord;
import org.example.bot.mail.settings.repository.MailSettingsRepository;
import org.example.bot.utils.DefaultAuthenticator;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SmtpSessionManager {

    private final MailSettingsRepository mailSettingsRepository;

    private final Map<Long, Session> sessionByUserIdMap = new ConcurrentHashMap<>();

    public Session getOrCreateSession(Long userId) {
        MailSettingsRecord mailSettings = mailSettingsRepository.findByUserId(userId);
        return getOrCreateSession(userId, mailSettings);
    }

    public Session getOrCreateSession(Long userId, MailSettingsRecord mailSettings) {

        sessionByUserIdMap.computeIfAbsent(userId, (k) -> {
            Properties properties = new Properties();

            properties.put("mail.smtp.host", mailSettings.getMailSmtpHost());
            properties.put("mail.smtp.port", mailSettings.getMailSmtpPort());
            properties.put("mail.smtp.ssl.enable", mailSettings.getMailSmtpSslEnabled());
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.connectiontimeout", 5000);
            properties.put("mail.smtp.writetimeout", 5000);
            properties.put("mail.smtp.timeout", 5000);

            Authenticator authenticator = new DefaultAuthenticator(mailSettings.getMailUser(), mailSettings.getMailPassword());

            return Session.getInstance(properties, authenticator);
        });

        return sessionByUserIdMap.get(userId);
    }
}
