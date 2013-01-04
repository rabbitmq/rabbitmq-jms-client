package com.rabbitmq.jms.util;

import java.util.Calendar;

import net.jcip.annotations.GuardedBy;

public final class RJMSLogger {
    private static Object lock = new Object();
    @GuardedBy("lock") private static boolean LOGGING = false;
    private final LogTemplate logTemplate;

    public RJMSLogger(LogTemplate lt) {
        this.logTemplate = lt;
    }

    public RJMSLogger(final String name) {
        this(new LogTemplate(){
            @Override
            public String template() {
                return name;
            }
        });
    }

    public static boolean setLogging(boolean logging) {
        synchronized(RJMSLogger.lock) {
            boolean oldLOGGING = LOGGING;
            LOGGING = logging;
            return oldLOGGING;
        }
    }

    private static boolean IS_LOGGING() {
        synchronized(RJMSLogger.lock) {
            return LOGGING;
        }
    }

    public final void log(String s, Exception x, Object ... c) {
        if (IS_LOGGING()) {
            log("Exception ("+x+") in "+s, c);
        }
    }

    public final void log(String s, Object ... c) {
        if (IS_LOGGING()) {
            StringBuilder sb = new StringBuilder(s).append('(');
            boolean first = true;
            for (Object obj : c) {
                if (first) first = false;
                else sb.append(", ");
                sb.append(String.valueOf(obj));
            }
            log(sb.append(')').toString());
        }
    }

    public final void log(String s) {
        if (IS_LOGGING()) {
            System.err.printf("[%2$tT.%2$tL] %1$s: %3$s %4$s%n", Thread.currentThread().getName(), Calendar.getInstance(), this.logTemplate.template(), s);
        }
    }

    public static abstract class LogTemplate {
        public abstract String template();
    }

}
