package org.schemaspy.app.progressbar;


import org.fusesource.jansi.AnsiConsole;

import java.io.PrintStream;
import java.util.concurrent.locks.Lock;

import static org.fusesource.jansi.Ansi.ansi;

public class ProgressAwareOutputStream extends PrintStream {

    private final ProgressManager progressManager;
    private final Lock lock;
    private final ProgressUpdater progressUpdater;

    public ProgressAwareOutputStream(ProgressManager progressManager, Lock lock) {
        super(AnsiConsole.out(), false);
        this.progressManager = progressManager;
        this.lock = lock;
        this.progressUpdater = new ProgressUpdater(progressManager, AnsiConsole.out());
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
                progressManager.render(AnsiConsole.out());
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
            progressManager.render(AnsiConsole.out());
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
}
