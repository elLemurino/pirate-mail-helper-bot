package org.example.bot.mail.rules.handler;

import io.micrometer.common.util.StringUtils;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.example.bot.jooq.tables.records.MailSettingsRecord;
import org.example.bot.mail.rules.domain.RuleType;
import org.example.bot.mail.session.service.SmtpSessionManager;
import org.example.bot.mail.settings.repository.MailSettingsRepository;
import org.example.bot.utils.MimeMessageParser;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForwardToAnotherMailboxRuleHandler implements RuleHandler {

    private final MailSettingsRepository mailSettingsRepository;
    private final SmtpSessionManager smtpSessionManager;


    @Override
    public RuleType ruleType() {
        return RuleType.FORWARD_TO_ANOTHER_MAILBOX;
    }

    @Override
    @SneakyThrows
    public void handle(Long userId, Message message, Map<String, Object> additionalData) {

        MailSettingsRecord mailSettings = mailSettingsRepository.findByUserId(userId);
        Session session = smtpSessionManager.getOrCreateSession(userId, mailSettings);

        if (message instanceof MimeMessage mimeMessage) {

            MimeMessageParser parser = new MimeMessageParser(mimeMessage);
            parser.parse();

            StringBuilder builder = new StringBuilder();
            builder.append("-------- Forwarded message --------\n");
            if (ArrayUtils.isNotEmpty(message.getFrom())) {
                builder.append("From: ");
                builder.append(parser.getFrom());
                builder.append("\n");
            }
            if (ArrayUtils.isNotEmpty(message.getRecipients(Message.RecipientType.TO))) {
                builder.append("To: ");
                builder.append(
                        parser.getTo().stream()
                                .map(a -> ((InternetAddress) a).getAddress())
                                .collect(Collectors.joining(", "))
                );
                builder.append("\n");
            }
            if (ArrayUtils.isNotEmpty(message.getRecipients(Message.RecipientType.CC))) {
                builder.append("Cc: ");
                builder.append(
                        Arrays.stream(message.getRecipients(Message.RecipientType.CC))
                                .map(a -> ((InternetAddress) a).getAddress())
                                .collect(Collectors.joining(", "))
                );
                builder.append("\n");
            }
            if (ArrayUtils.isNotEmpty(message.getRecipients(Message.RecipientType.BCC))) {
                builder.append("Bcc: ");
                builder.append(
                        Arrays.stream(message.getRecipients(Message.RecipientType.BCC))
                                .map(a -> ((InternetAddress) a).getAddress())
                                .collect(Collectors.joining(", "))
                );
                builder.append("\n");
            }
            if (StringUtils.isNotBlank(message.getSubject())) {
                byte[] subjectBytes = message.getSubject().getBytes(StandardCharsets.UTF_8);
                builder.append("Subject: ");
                builder.append(new String(subjectBytes));
                builder.append("\n");
            }
            builder.append("\n");

            MimeBodyPart forwardPart = new MimeBodyPart();
            forwardPart.setText(builder.toString(), StandardCharsets.UTF_8.name());


            MimeBodyPart forwardedPart = new MimeBodyPart();
            if (parser.isMimeType(mimeMessage, MimeTypeUtils.TEXT_PLAIN_VALUE)) {
                forwardedPart.setContent(parser.getPlainContent(), MimeTypeUtils.TEXT_PLAIN_VALUE);
            } else if (parser.isMimeType(mimeMessage, MimeTypeUtils.TEXT_HTML_VALUE)) {
                forwardedPart.setContent(parser.getHtmlContent(), MimeTypeUtils.TEXT_HTML_VALUE);
            } else if (parser.isMimeType(mimeMessage, "multipart/*")) {
                forwardedPart.setContent(parser.getMultipartContent(), "multipart/" + parser.getMimeTypeSubtype(mimeMessage));
            }

            Multipart multipart = new MimeMultipart("mixed");
            multipart.addBodyPart(forwardPart);
            multipart.addBodyPart(forwardedPart);

            MimeMessage messageToSend = new MimeMessage(session);

            messageToSend.setRecipients(Message.RecipientType.TO, String.format("%s", additionalData.get("to")));
            if (additionalData.get("cc") != null) {
                messageToSend.setRecipients(Message.RecipientType.CC, String.format("%s", additionalData.get("cc")));
            }
            if (additionalData.get("bcc") != null) {
                messageToSend.setRecipients(Message.RecipientType.BCC, String.format("%s", additionalData.get("bcc")));
            }
            messageToSend.setSubject("Fwd: " + message.getSubject());
            messageToSend.setFrom(mailSettings.getMailUser());
            messageToSend.setContent(multipart);

            Transport.send(messageToSend);
            log.info("Email sent successfully");
            return;
        }

        MimeBodyPart forwardedPart = new MimeBodyPart();
        forwardedPart.setContent(message, "message/rfc822");

        Multipart multipart = new MimeMultipart("mixed", forwardedPart);

        MimeMessage messageToSend = new MimeMessage(session);

        messageToSend.setRecipients(Message.RecipientType.TO, String.format("%s", additionalData.get("to")));
        if (additionalData.get("cc") != null) {
            messageToSend.setRecipients(Message.RecipientType.CC, String.format("%s", additionalData.get("cc")));
        }
        if (additionalData.get("bcc") != null) {
            messageToSend.setRecipients(Message.RecipientType.BCC, String.format("%s", additionalData.get("bcc")));
        }
        messageToSend.setSubject("Fwd: " + message.getSubject());
        messageToSend.setFrom(mailSettings.getMailUser());
        messageToSend.setContent(multipart);

        Transport.send(messageToSend);
        log.info("Email sent successfully");
    }
}
