package org.schemaspy.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;

import static org.fusesource.jansi.Ansi.ansi;

public class LogLevelColorizeConverter extends CompositeConverter<ILoggingEvent> {

    @Override
    protected String transform(ILoggingEvent event, String in) {
        if (event.getLevel() == Level.ERROR) {
            return ansi().fgRed().a(in).reset().toString();
        } else if (event.getLevel() == Level.WARN) {
            return ansi().fgYellow().a(in).reset().toString();
        } else {
            return ansi().fgGreen().a(in).reset().toString();
        }
    }
}
