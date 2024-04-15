package org.example.bot.mail.rules.handler;

import jakarta.mail.Message;
import org.example.bot.mail.rules.domain.RuleType;

import java.util.Map;

public interface RuleHandler {

    RuleType ruleType();

    void handle(Long userId, Message message, Map<String, Object> additionalData);
}
