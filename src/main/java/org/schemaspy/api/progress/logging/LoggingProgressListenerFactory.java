package org.schemaspy.api.progress.logging;

import org.schemaspy.api.progress.ProgressListener;
import org.schemaspy.api.progress.ProgressListenerFactory;

public class LoggingProgressListenerFactory implements ProgressListenerFactory {
    @Override
    public ProgressListener newProgressListener(String label) {
        return new LoggingProgressListener(label);
    }
}
