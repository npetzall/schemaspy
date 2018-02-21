package org.schemaspy.app.progressbar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressUpdater implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger("ProgressUpdater");

    private final ProgressManager progressManager;

    private volatile boolean shouldRun = true;
    private Thread thread;

    public ProgressUpdater(ProgressManager progressManager) {
        this.progressManager = progressManager;
    }

    public void stop() {
        shouldRun = false;
        thread.interrupt();
    }

    @Override
    public void run() {
        thread = Thread.currentThread();
        while (shouldRun) {
            progressManager.render();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOGGER.debug("interrupted while sleeping");
                Thread.currentThread().interrupt();
            }
        }
    }
}
