package ste.ai.viber.log;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class LogViewerHandler extends Handler {

    private static final Logger LOG = Logger.getLogger(LogViewerHandler.class.getName());

    private final String loggerName;
    private final LogViewer logViewer;

    public LogViewerHandler(final String loggerName, final LogViewer logViewer) {
        this.loggerName = loggerName;
        this.logViewer = logViewer;
        this.setLevel(Level.ALL);
        setFormatter(new java.util.logging.Formatter() {
            @Override
            public String format(final LogRecord record) {
                return new HTTPLogParser().json(record.getMessage()).toString();
            }
        });
    }

    @Override
    public void publish(final LogRecord record) {
        LOG.info("flter: " + getFilter());
        LOG.info("level: " + getLevel());
        LOG.info(() -> "publish log record from %s:%s %s".formatted(record.getLevel().toString(), record.getLoggerName(), record.getMessage()));
        if (!isLoggable(record) || (loggerName != null && !record.getLoggerName().startsWith(loggerName)) ) {
            return;
        }

        try {
            logViewer.log(getFormatter().format(record));
        } catch (Throwable x) {
            LOG.log(java.util.logging.Level.WARNING, "Failed to render log record: " + record.getMessage(), x);
            x.printStackTrace();
        }
    }

    @Override
    public void flush() {
        // No need to implement for this handler
    }

    @Override
    public void close() throws SecurityException {
        // No need to implement for this handler
    }
}
