package org.schemaspy.app.progressbar;

import org.fusesource.jansi.Ansi;
import org.schemaspy.api.progress.ProgressListener;
import org.schemaspy.api.progress.ProgressListenerFactory;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.fusesource.jansi.Ansi.ansi;

public class ProgressManager implements ProgressListenerFactory {

    private final ProgressAwareOutputStream progressAwareOutputStream;
    private final Lock lock = new ReentrantLock();

    public ProgressManager() {
        progressAwareOutputStream = new ProgressAwareOutputStream(this, lock);
    }

    private List<ProgressLine> progressLineList = new LinkedList<>();

    public ProgressListener newProgressListener(String label) {
        return new ProgressLine(label, this);
    }

    public void starting(ProgressLine progressLine) {
        progressLineList.add(progressLine);
    }

    public void finished(ProgressLine progressLine) {
        progressLineList.remove(progressLine);
    }

    public void render(PrintStream ps){
        if(progressLineList.isEmpty()) {
            return;
        }
        lock.lock();
        try {
            Ansi ansi = ansi()/*.eraseLine().a("Running tasks:").newline()*/;
            for (int i = 0; i < progressLineList.size(); i++) {
                ansi.eraseLine().a(progressLineList.get(i).toString()).newline();
            }
            ansi.cursorUp(progressLineList.size() /*+ 1*/).cursorToColumn(0);
            ps.print(ansi);
            ps.flush();
        } finally {
            lock.unlock();
        }
    }
}
