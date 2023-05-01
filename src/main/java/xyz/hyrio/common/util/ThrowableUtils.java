package xyz.hyrio.common.util;

import org.slf4j.Logger;
import org.slf4j.event.Level;

@Deprecated
public final class ThrowableUtils {
    private ThrowableUtils() {
    }

    public static final int DEFAULT_MAX_STACK_TRACES = 20;

    public static void logWithThrowable(Logger log, Level level, Throwable t, int maxStackTraces, String fmt, Object... args) {
        if (!log.isEnabledForLevel(level)) return;

        String msg = String.format(fmt, args);
        StackTraceElement[] stackTraceElements = t.getStackTrace();
        int stackTraceElementsSize = Math.min(stackTraceElements.length, maxStackTraces);
        boolean hasMoreStackTraces = stackTraceElements.length > maxStackTraces;
        String finalMsg;
        if (stackTraceElementsSize == 0) {
            finalMsg = msg + "\n\n" + t + "\n";
        } else {
            StringBuilder sb = new StringBuilder(msg);
            sb.append("\n\n").append(t).append("\n");
            for (int i = 0; i < stackTraceElementsSize; i++) {
                sb.append("\tat ").append(stackTraceElements[i]).append("\n");
            }
            if (hasMoreStackTraces) {
                sb.append("\t... ").append(stackTraceElements.length - maxStackTraces).append(" more\n");
            }
            finalMsg = sb.toString();
        }
        log.atLevel(level).log(finalMsg);
    }

    public static void logWithThrowable(Logger log, Level level, Throwable e, String fmt, Object... args) {
        logWithThrowable(log, level, e, DEFAULT_MAX_STACK_TRACES, fmt, args);
    }
}
