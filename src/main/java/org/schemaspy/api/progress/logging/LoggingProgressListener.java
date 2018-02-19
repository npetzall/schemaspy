package org.schemaspy.api.progress.logging;

import org.schemaspy.api.progress.ProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.atomic.AtomicInteger;

public class LoggingProgressListener implements ProgressListener{

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final String taskName;

    private int tasks = 0;
    private AtomicInteger finishedTasks = new AtomicInteger(0);

    private long startedAt = 0;
    private long finishedAt = 0;

    public LoggingProgressListener(String taskName) {
        this.taskName = taskName;
    }

    @Override
    public ProgressListener starting(int tasks) {
        if (tasks > 0) {
            LOGGER.info("Starting work on '{}' consists of {} tasks", taskName, tasks );
        } else {
            LOGGER.info("Starting work on '{}'", taskName);
        }
        startedAt = System.currentTimeMillis();
        return this;
    }

    @Override
    public void finishedTask() {
        int taskNo = finishedTasks.incrementAndGet();
        if (taskNo == tasks) {
            finished();
        } else {
            if (tasks > 0) {
                LOGGER.debug("{} ({}/{})", taskName, taskNo, tasks);
            } else {
                LOGGER.debug("{} ({}/{})", taskName, taskNo, "?");
            }
        }
    }

    @Override
    public void finished() {
        if (finishedAt == 0) {
            finishedAt = System.currentTimeMillis();
            LOGGER.info("Finished '{}' after {} ms", taskName, (finishedAt - startedAt));
        }
    }
}
