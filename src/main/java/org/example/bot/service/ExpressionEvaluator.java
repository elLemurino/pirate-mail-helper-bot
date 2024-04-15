package org.example.bot.service;

import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.example.bot.utils.MimeMessageParser;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class ExpressionEvaluator {

    private final ExpressionParser expressionParser;

    @SneakyThrows
    public Boolean evaluate(Message message, String expression) {
        EvaluationContext context = new StandardEvaluationContext();
        if (message instanceof MimeMessage mimeMessage) {
            MimeMessageParser parser = new MimeMessageParser(mimeMessage).parse();
            context.setVariable("from", parser.getFrom());
            if (CollectionUtils.isNotEmpty(parser.getTo())) {
                context.setVariable("to", parser.getTo());
            }
            if (CollectionUtils.isNotEmpty(parser.getCc())) {
                context.setVariable("cc", parser.getCc());
            }
            if (CollectionUtils.isNotEmpty(parser.getBcc())) {
                context.setVariable("bcc", parser.getBcc());
            }
            context.setVariable("subject", parser.getSubject());
            context.setVariable("body", parser.getPlainContent());
            context.setVariable("bodyHtml", parser.getHtmlContent());
        } else {
            if (ArrayUtils.isNotEmpty(message.getFrom())) {
                context.setVariable("from", message.getFrom()[0]);
            }
            if (ArrayUtils.isNotEmpty(message.getRecipients(Message.RecipientType.TO))) {
                context.setVariable("to", Arrays.asList(message.getRecipients(Message.RecipientType.TO)));
            }
            if (ArrayUtils.isNotEmpty(message.getRecipients(Message.RecipientType.CC))) {
                context.setVariable("cc", Arrays.asList(message.getRecipients(Message.RecipientType.CC)));
            }
            if (ArrayUtils.isNotEmpty(message.getRecipients(Message.RecipientType.BCC))) {
                context.setVariable("bcc", Arrays.asList(message.getRecipients(Message.RecipientType.BCC)));
            }
            context.setVariable("subject", message.getSubject());
        }

        return expressionParser.parseExpression(expression).getValue(context, Boolean.class);
    }
}
