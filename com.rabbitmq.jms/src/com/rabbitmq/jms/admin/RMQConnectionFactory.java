/**
 *
 */
package com.rabbitmq.jms.admin;

import java.io.Serializable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;

/**
 * RabbitMQ Implementation of JMS {@link ConnectionFactory}
 */
public class RMQConnectionFactory implements
                                 ConnectionFactory,
                                 Referenceable,
                                 Serializable {

    /** Default serializable uid. */
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_USERNAME = "guest";
    private static final String DEFAULT_PASSWORD = "guest";

    @Override
    public Connection createConnection() throws JMSException {
        return createConnection(DEFAULT_USERNAME, DEFAULT_PASSWORD);
    }

    @Override
    public Connection
            createConnection(String userName, String password) throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Reference getReference() throws NamingException {
        // TODO Auto-generated method stub
        return null;
    }

}
