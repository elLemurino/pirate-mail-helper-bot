package org.example.bot.mail.rules.repository;

import lombok.RequiredArgsConstructor;
import org.example.bot.jooq.tables.MailRules;
import org.example.bot.jooq.tables.records.MailRulesRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.jooq.impl.DSL.exists;

@Repository
@RequiredArgsConstructor
public class MailRulesRepository {

    private final DSLContext jooq;

    public boolean existRulesByUserId(Long userId) {
        return jooq.fetchValue(exists(
                jooq.selectOne()
                        .from(MailRules.MAIL_RULES)
                        .where(MailRules.MAIL_RULES.USER_ID.eq(userId))
        ));
    }

    public List<MailRulesRecord> findAllByUserId(Long userId) {
        return jooq.selectFrom(MailRules.MAIL_RULES)
                .where(MailRules.MAIL_RULES.USER_ID.eq(userId))
                .fetchStream().toList();
    }


}
