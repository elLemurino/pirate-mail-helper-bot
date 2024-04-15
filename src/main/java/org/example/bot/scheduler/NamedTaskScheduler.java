package org.example.bot.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.bot.scheduler.model.NamedTask;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Component
@RequiredArgsConstructor
public class NamedTaskScheduler {

    private final TaskScheduler taskScheduler;

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new IdentityHashMap<>();

    public void scheduleAtFixedRate(NamedTask namedTask, Duration period) {
        ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(namedTask.getTask(), period);
        scheduledTasks.put(namedTask.getId(), future);
    }

    public void cancelScheduledTask(String id) {
        ScheduledFuture<?> future = scheduledTasks.get(id);
        if (null != future) {
            future.cancel(true);
        }
    }
}
