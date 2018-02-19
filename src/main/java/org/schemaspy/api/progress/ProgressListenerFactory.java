package org.schemaspy.api.progress;

public interface ProgressListenerFactory {

    ProgressListener newProgressListener(String label);
}
