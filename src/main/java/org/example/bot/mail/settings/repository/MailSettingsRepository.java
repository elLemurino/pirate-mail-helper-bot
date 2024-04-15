package org.example.bot.mail.settings.repository;

import lombok.RequiredArgsConstructor;
import org.example.bot.jooq.tables.MailSettings;
import org.example.bot.jooq.tables.records.MailSettingsRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MailSettingsRepository {

    private final DSLContext jooq;

    public List<MailSettingsRecord> findAll() {
        return jooq.selectFrom(MailSettings.MAIL_SETTINGS).fetchStream().toList();
    }

    public MailSettingsRecord findByUserId(Long userId) {
        return jooq.selectFrom(MailSettings.MAIL_SETTINGS).fetchSingle();
    }
}
