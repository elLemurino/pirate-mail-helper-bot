package org.example.bot.scheduler.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NamedTask {

    private String id;
    private Runnable task;
}
