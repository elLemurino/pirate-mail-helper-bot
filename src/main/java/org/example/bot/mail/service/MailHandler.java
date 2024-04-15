package org.example.bot.mail.service;

import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Store;
import jakarta.mail.search.FlagTerm;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.bot.mail.rules.service.RulesEvaluator;

@Slf4j
public class MailHandler {

    private final Long userId;
    private final Store store;
    private final RulesEvaluator rulesEvaluator;

    public MailHandler(Long userId, Store store, RulesEvaluator rulesEvaluator) {
        this.userId = userId;
        this.store = store;
        this.rulesEvaluator = rulesEvaluator;
    }

    @SneakyThrows
    public void handle() {
        try (Folder folder = store.getFolder("INBOX")) {
            folder.open(Folder.READ_WRITE);
            Message[] messages = folder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            for (Message message : messages) {
                if (message.isSet(Flags.Flag.SEEN)) {
                    continue;
                }
                boolean seen = true;
                try {
                    rulesEvaluator.evaluateRules(userId, message);
                } catch (Exception ex) {
                    log.warn("An exception occurred while evaluating rules", ex);
                    seen = false;
                }

                message.setFlag(Flags.Flag.SEEN, seen);
            }
        }
    }
}
