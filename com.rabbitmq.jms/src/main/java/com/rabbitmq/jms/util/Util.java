package com.rabbitmq.jms.util;

import javax.jms.JMSException;

public class Util {
    private static final Util util = new Util();
    public static Util util() {
        return util;
    }
    
    public JMSException handleException(Exception x) throws JMSException {
        JMSException jx = new JMSException(x.getMessage());
        jx.initCause(x);
        throw jx;
        
    }
}
