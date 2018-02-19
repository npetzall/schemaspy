package org.schemaspy.app.progressbar;

import org.schemaspy.api.progress.ProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class ProgressLine implements ProgressListener {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final String label;
    private final ProgressManager progressManager;

    private int numberOfTasks = -1;
    private long startedAt = 0;
    private long finishedAt = 0;

    private int numberOfFinishedTasks = 0;

    public ProgressLine(String label, ProgressManager progressManager) {
        this.label = label;
        this.progressManager = progressManager;
    }

    @Override
    public ProgressListener starting(int numberOfTasks) {
        if (startedAt > 0) {
            LOG.warn("ProgressLine with label {} has already been started", label, new IllegalStateException());
            return this;
        }
        this.numberOfTasks = numberOfTasks > 0 ? numberOfTasks : -1;
        startedAt = System.currentTimeMillis();
        progressManager.starting(this);
        return this;
    }

    @Override
    public void finishedTask() {
        numberOfFinishedTasks++;
        if (numberOfFinishedTasks == numberOfTasks) {
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
        LOG.info("Finished {} in {} ms",label, finishedAt - startedAt);
    }

    public String toString() {
        if (numberOfTasks > 0) {
            return String.format("%s: (%s/%s)", label, numberOfFinishedTasks, numberOfTasks);
        } else {
            return String.format("%s: (%s/?)", label, numberOfFinishedTasks);
        }
    }
}
