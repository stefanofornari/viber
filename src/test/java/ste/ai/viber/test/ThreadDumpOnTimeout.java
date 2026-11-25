package ste.ai.viber.test;

import java.lang.Thread.State;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

/**
 * Prints the stack of all live threads when a test fails because of a timeout,
 * to understand where it was stuck. Registered through extension autodetection.
 */
public class ThreadDumpOnTimeout implements TestExecutionExceptionHandler {

    @Override
    public void handleTestExecutionException(final ExtensionContext context, final Throwable throwable) throws Throwable {
        if (isTimeout(throwable)) {
            final StringBuilder dump = new StringBuilder("\n*** TIMEOUT in ")
                .append(context.getDisplayName()).append(" - thread dump ***\n");
            for (final Map.Entry<Thread, StackTraceElement[]> e : Thread.getAllStackTraces().entrySet()) {
                final Thread t = e.getKey();
                if (t.getState() == State.TERMINATED) {
                    continue;
                }
                dump.append('"').append(t.getName()).append("\" ").append(t.getState()).append('\n');
                for (final StackTraceElement el : e.getValue()) {
                    dump.append("    at ").append(el).append('\n');
                }
            }
            System.out.println(dump);
            System.out.flush();
        }
        throw throwable;
    }

    private boolean isTimeout(Throwable t) {
        for (; t != null; t = t.getCause()) {
            if (t instanceof TimeoutException) {
                return true;
            }
        }
        return false;
    }
}
