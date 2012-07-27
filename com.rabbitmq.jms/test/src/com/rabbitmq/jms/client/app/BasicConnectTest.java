/**
 * 
 */
package com.rabbitmq.jms.client.app;

import java.io.UnsupportedEncodingException;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Stand-alone test we can connect and create a session.
 */
public class BasicConnectTest {

    /**
     * @param args - none expected
     * @throws NamingException if cannot get {@link ConnectionFactory} or
     *             {@link Destination}
     * @throws JMSException if cannot create {@link Connection}
     */
    public static void main(String[] args) throws NamingException, JMSException {
        Context msging = new InitialContext();
        ConnectionFactory connectionFactory = (ConnectionFactory) msging.lookup("TestConnectionFactory");

        Connection connection = connectionFactory.createConnection();

        Session session = connection.createSession(false, 0);

        Destination destination = (Destination) msging.lookup("TestDestination");

        MessageProducer producer = session.createProducer(destination);

        BytesMessage bytesMessage = session.createBytesMessage();

        bytesMessage.clearBody();
        try {
            bytesMessage.writeBytes("Test bytes".getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) { // cannot happen
            e.printStackTrace(System.err);
            System.exit(1);
        }

        producer.send(bytesMessage);

        connection.close();
    }

}
