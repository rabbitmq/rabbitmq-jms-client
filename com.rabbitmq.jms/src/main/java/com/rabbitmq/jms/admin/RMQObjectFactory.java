package com.rabbitmq.jms.admin;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

// TODO implement JNDI creation for RMQConnectionFactory and RMQDestination
public class RMQObjectFactory implements ObjectFactory {

    @Override
    public Object getObjectInstance(Object arg0, Name arg1, Context arg2, Hashtable<?, ?> arg3) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
