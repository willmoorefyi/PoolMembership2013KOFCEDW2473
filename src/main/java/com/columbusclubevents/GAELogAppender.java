package com.columbusclubevents;

import java.util.logging.Logger;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * 
 * @author Eurig Jones
 */
public class GAELogAppender extends AppenderBase<ILoggingEvent> {
    private static final Logger log = Logger.getLogger("logback");
    PatternLayoutEncoder encoder;

    public GAELogAppender() {
    }

    @Override
    protected void append(ILoggingEvent event) {

    Level level = event.getLevel();

    //To optimize, reorder according to how often levels are hit
    if (level.equals(Level.ERROR))
        log.severe(encoder.getLayout().doLayout(event));
    else if (level.equals(Level.WARN))
        log.warning(encoder.getLayout().doLayout(event));
    else if (level.equals(Level.INFO))
        log.info(encoder.getLayout().doLayout(event));
    else if (level.equals(Level.DEBUG))
        log.fine(encoder.getLayout().doLayout(event));
    else
        // if (level.equals(Level.TRACE))
        log.finest(encoder.getLayout().doLayout(event));
    }

    public PatternLayoutEncoder getEncoder() {
        return encoder;
    }

    public void setEncoder(PatternLayoutEncoder encoder) {
        this.encoder = encoder;
    }
}