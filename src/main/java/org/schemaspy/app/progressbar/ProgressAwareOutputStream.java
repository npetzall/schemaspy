package org.schemaspy.app.progressbar;


import org.fusesource.jansi.AnsiConsole;

import java.io.PrintStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.fusesource.jansi.Ansi.ansi;

public class ProgressAwareOutputStream extends PrintStream {

    private final ProgressManager progressManager;
    private final Lock lock = new ReentrantLock();
    private final ProgressUpdater progressUpdater;

    public ProgressAwareOutputStream(ProgressManager progressManager) {
        super(AnsiConsole.out(), false);
        this.progressManager = progressManager;
        this.progressUpdater = new ProgressUpdater(progressManager);
        System.setOut(this);
        System.setErr(this);
        startUpdater();
    }

    private void startUpdater() {
        Thread progressUpdaterThread = new Thread(progressUpdater, "ConsoleProgressUpdater");
        progressUpdaterThread.setDaemon(true);
        progressUpdaterThread.start();
    }

    @Override
    public void write(int b) {
        lock.lock();
        try {
            super.write(b);
            if ((b == '\n')) {
                progressManager.render();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        lock.lock();
        try {
            super.write(buf, off, len);
            progressManager.render();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void println(String x) {
        lock.lock();
        try {
            super.print(ansi().eraseLine().a(x).newline());
        } finally {
            lock.unlock();
        }
    }

    public Lock getLock() {
        return lock;
    }

    public PrintStream getProgressPrintStream() {
        return AnsiConsole.out();
    }
}
