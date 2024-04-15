package org.example.bot.mail.rules.handler;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.SharedByteArrayInputStream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.jooq.tables.records.MailSettingsRecord;
import org.example.bot.mail.rules.domain.RuleType;
import org.example.bot.mail.session.service.SmtpSessionManager;
import org.example.bot.mail.settings.repository.MailSettingsRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedirectToAnotherMailboxRuleHandler implements RuleHandler {

    private final MailSettingsRepository mailSettingsRepository;
    private final SmtpSessionManager smtpSessionManager;

    @Override
    public RuleType ruleType() {
        return RuleType.REDIRECT_TO_ANOTHER_MAILBOX;
    }

    @Override
    @SneakyThrows
    public void handle(Long userId, Message message, Map<String, Object> additionalData) {

        MailSettingsRecord mailSettings = mailSettingsRepository.findByUserId(userId);
        Session session = smtpSessionManager.getOrCreateSession(userId, mailSettings);

        ByteArrayOutputStream bos;
        int size = message.getSize();
        if (size > 0) {
            bos = new ByteArrayOutputStream(size);
        } else {
            bos = new ByteArrayOutputStream();
        }
        message.writeTo(bos);

        MimeMessage messageToSend = new MimeMessage(session, new SharedByteArrayInputStream(bos.toByteArray()));

        messageToSend.setRecipients(Message.RecipientType.TO, String.format("%s", additionalData.get("to")));
        if (additionalData.get("cc") != null) {
            messageToSend.setRecipients(Message.RecipientType.CC, String.format("%s", additionalData.get("cc")));
        }
        if (additionalData.get("bcc") != null) {
            messageToSend.setRecipients(Message.RecipientType.BCC, String.format("%s", additionalData.get("bcc")));
        }
        messageToSend.setSubject(message.getSubject());
        messageToSend.setFrom(mailSettings.getMailUser());

        Transport.send(messageToSend);

        log.info("Email sent successfully");
    }
}
