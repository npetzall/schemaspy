package org.schemaspy.app.progressbar;

import org.fusesource.jansi.AnsiConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;

public class ProgressUpdater implements Runnable {

    private static final Logger log = LoggerFactory.getLogger("ProgressUpdater");

    private final ProgressManager progressManager;
    private final PrintStream printStream;

    private volatile boolean shouldRun = true;
    private Thread thread;

    public ProgressUpdater(ProgressManager progressManager, PrintStream printStream) {
        this.progressManager = progressManager;
        this.printStream = printStream;
    }

    public void stop() {
        shouldRun = false;
        thread.interrupt();
    }

    @Override
    public void run() {
        thread = Thread.currentThread();
        while (shouldRun) {
            progressManager.render(AnsiConsole.out());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.debug("interrupted while sleeping");
            }
        }
    }
}
