package org.example.bot.mail.rules.service;

import jakarta.mail.Message;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.jooq.tables.MailRules;
import org.example.bot.jooq.tables.records.MailRulesRecord;
import org.example.bot.mail.rules.domain.RuleType;
import org.example.bot.mail.rules.handler.RuleHandler;
import org.example.bot.mail.rules.repository.MailRulesRepository;
import org.example.bot.service.ExpressionEvaluator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Service
@Slf4j
public class RulesEvaluator {

    private final ExpressionEvaluator expressionEvaluator;
    private final MailRulesRepository rulesRepository;
    private final Map<RuleType, RuleHandler> ruleHandlers;

    public RulesEvaluator(ExpressionEvaluator expressionEvaluator,
                          MailRulesRepository rulesRepository,
                          List<RuleHandler> ruleHandlers) {
        this.expressionEvaluator = expressionEvaluator;
        this.rulesRepository = rulesRepository;
        this.ruleHandlers = ruleHandlers.stream()
                .collect(toMap(RuleHandler::ruleType, Function.identity()));
    }

    public boolean rulesExist(Long userId) {
        return rulesRepository.existRulesByUserId(userId);
    }

    public void evaluateRules(Long userId, Message message) {

        List<MailRulesRecord> rules = rulesRepository.findAllByUserId(userId);

        for (MailRulesRecord rule : rules) {
            Boolean result = expressionEvaluator.evaluate(message, rule.getRuleExpression());
            if (!Boolean.TRUE.equals(result)) {
                log.debug(String.format("rule %s is not suitable for message %s", rule, message));
                continue;
            }
            RuleType ruleType = RuleType.valueOf(rule.getRuleType());
            RuleHandler ruleHandler = ruleHandlers.get(ruleType);
            if (ruleHandler == null) {
                log.warn("Rule handler wasn't found, rule type {}", ruleType);
                continue;
            }

            ruleHandler.handle(userId, message, rule.getValue(MailRules.MAIL_RULES.ADDITIONAL_DATA, Map.class));
        }
    }
}
