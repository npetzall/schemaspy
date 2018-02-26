package org.schemaspy.app.progressbar;

import org.schemaspy.api.progress.ProgressListener;
import org.schemaspy.util.DurationFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.atomic.AtomicInteger;

public class ProgressLine implements ProgressListener {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final String label;
    private final ProgressManager progressManager;

    private int numberOfTasks = -1;
    private long startedAt = 0;
    private long finishedAt = 0;

    private AtomicInteger numberOfFinishedTasks = new AtomicInteger(0);

    public ProgressLine(String label, ProgressManager progressManager) {
        this.label = label;
        this.progressManager = progressManager;
    }

    @Override
    public ProgressListener starting(int numberOfTasks) {
        if (startedAt > 0) {
            LOG.warn("ProgressLine with label {} has already been started", label, new IllegalStateException("Already Started"));
            return this;
        }
        this.numberOfTasks = numberOfTasks > 0 ? numberOfTasks : -1;
        startedAt = System.currentTimeMillis();
        progressManager.starting(this);
        return this;
    }

    @Override
    public void finishedTask() {
        int finished = numberOfFinishedTasks.incrementAndGet();
        if (finished == numberOfTasks) {
            finished();
        }
    }

    @Override
    public void finished() {
        if (finishedAt > 0) {
            return;
        }
        finishedAt = System.currentTimeMillis();
        progressManager.finished(this);
        LOG.info("Finished {} in {}",label, DurationFormatter.formatMS(finishedAt - startedAt));
    }

    public String toString() {
        int finished = numberOfFinishedTasks.get();
        if (numberOfTasks > 0) {
            if (finished > 0) {
                return String.format("%s: (%s/%s) est left: %s", label, finished, numberOfTasks, getEstTimeLeft(finished, System.currentTimeMillis()));
            } else {
                return String.format("%s: (%s/%s)", label, finished, numberOfTasks);
            }
        } else {
            return String.format("%s: (%s/?)", label, finished);
        }
    }

    private String getEstTimeLeft(int finished, long now) {
        long duration = now - startedAt;
        double perTask = (double)duration/finished;
        return DurationFormatter.formatMS((long)((numberOfTasks-finished)*perTask));
    }
}
