package com.rabbitmq.jms.util;

import java.util.Calendar;

public final class RJMSLogger {
    private static final boolean LOGGING = false;
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
    public final void log(String s, Exception x, Object ... c) {
        if (LOGGING) {
            log("Exception ("+x+") in "+s, c);
        }
    }

    public final void log(String s, Object ... c) {
        if (LOGGING) {
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
        if (LOGGING)
            System.err.printf("[%1$tT.%1$tL] %2$s %3$s%n", Calendar.getInstance(), this.logTemplate.template(), s);
    }

    public static abstract class LogTemplate {
        public abstract String template();
    }

}
