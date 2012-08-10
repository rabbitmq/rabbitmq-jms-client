package com.rabbitmq.integration.jmscts;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.naming.NamingException;

import org.exolab.jmscts.provider.Administrator;
import org.exolab.jmscts.provider.Provider;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.jms.admin.RMQDestination;

public class RabbitJmsCtsProvider implements Administrator, Provider {

    private HashMap<String,Object> lookup = new HashMap<String, Object>();
    private static SingleConnectionFactory factory = new SingleConnectionFactory();
    
    @Override
    public void cleanup(boolean arg0) throws JMSException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Administrator getAdministrator() throws JMSException {
        return this;
    }

    @Override
    public void initialise(boolean start) throws JMSException {
        lookup.put(getQueueConnectionFactory(), factory);
        lookup.put(getTopicConnectionFactory(), factory);
    }

    @Override
    public void createDestination(String name, boolean queue) throws JMSException {
        if (queue) {
            String exchangeName = "", routingKey = name;
            RMQDestination dest = new RMQDestination(name, exchangeName, routingKey, true, false);
            lookup.put(name, dest);
        } else {
            String topicName = name, exchangeName = "topic." + topicName, routingKey = name;
            RMQDestination dest = new RMQDestination(name, exchangeName, routingKey, false, false);
            lookup.put(name, dest);
        }
    }

    @Override
    public boolean destinationExists(String name) throws JMSException {
        // TODO Auto-generated method stub
        return lookup.containsKey(name);
    }

    @Override
    public void destroyDestination(String name) throws JMSException {
        
        
    }

    @Override
    public String getQueueConnectionFactory() {
        return RMQConnectionFactory.class.getName();
    }

    @Override
    public String getTopicConnectionFactory() {
        return RMQConnectionFactory.class.getName();
    }

    @Override
    public String getXAQueueConnectionFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getXATopicConnectionFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object lookup(String name) throws NamingException {
        return lookup.get(name);
    }
    
    private static class SingleConnectionFactory extends RMQConnectionFactory {
        /** TODO */
        private static final long serialVersionUID = 1L;
        private AtomicReference<Connection> connection = new AtomicReference<Connection>();
        @Override
        public Connection createConnection() throws JMSException {
            Connection result = connection.get();
            if (result==null) {
                result = super.createConnection();
                if (connection.compareAndSet(null, result)) {
                    
                } else {
                    result.close();
                }
            }
            return connection.get();
            
        }

        @Override
        public Connection createConnection(String userName, String password) throws JMSException {
            // TODO Auto-generated method stub
            return super.createConnection(userName, password);
        }
        
    }
    

}
