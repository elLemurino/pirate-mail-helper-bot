package org.example.bot.mail.rules.handler;

import jakarta.mail.Message;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.mail.rules.domain.RuleType;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
// fixme move to telegram package
public class RedirectToTelegramRuleHandler implements RuleHandler {

    @Override
    public RuleType ruleType() {
        return RuleType.REDIRECT_TO_TELEGRAM;
    }

    @Override
    @SneakyThrows
    public void handle(Long userId, Message message, Map<String, Object> additionalData) {
        log.info(message.getSubject());
    }
}
