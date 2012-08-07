package com.rabbitmq.jms.util;

import javax.jms.JMSException;

public class Util {
    private static final Util util = new Util();

    public static Util util() {
        return util;
    }

    public JMSException handleException(Exception x, String message) throws JMSException {
        JMSException jx = new JMSException(message==null?x.getMessage():message);
        jx.initCause(x);
        throw jx;
    }
    public JMSException handleException(Exception x) throws JMSException {
        return handleException(x, x.getMessage());
    }
    
    public void checkClosed(boolean closed, String msg) throws JMSException {
        if (closed) {
            throw new JMSException(msg!=null?msg:"Closed");
        }
    }
}
