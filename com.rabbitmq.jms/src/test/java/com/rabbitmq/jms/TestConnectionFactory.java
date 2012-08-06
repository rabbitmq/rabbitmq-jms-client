package com.rabbitmq.jms;

import java.util.Properties;

public abstract class TestConnectionFactory {
    public abstract javax.jms.ConnectionFactory getConnectionFactory();
    
    public static TestConnectionFactory getTestConnectionFactory() throws Exception {
        final String propertyName = "test.factory.class";
        
        Properties properties = new Properties() ;
        try {
            properties.load(TestConnectionFactory.class.getResourceAsStream("/test.properties"));
        }catch (Exception x) {
            System.err.println("Error: Unable to load test.properties");
            properties.put(propertyName,"com.rabbitmq.jms.basic.SpringAMQPTestFactory");
        }
        String name = properties.getProperty(propertyName);
        String className = System.getProperty(propertyName, properties.getProperty(propertyName));
        Class clazz = Class.forName(className, true, Thread.currentThread().getContextClassLoader());
        return (TestConnectionFactory)clazz.newInstance();
    }
}
