package org.schemaspy.api.progress;

public interface ProgressListener {
    ProgressListener starting(int tasks);
    void finishedTask();
    void finished();
}
