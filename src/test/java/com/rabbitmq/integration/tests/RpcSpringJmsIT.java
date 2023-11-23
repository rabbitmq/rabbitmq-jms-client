// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2018-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.util.RMQJMSException;
import javax.jms.JMSContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.support.MessageBuilder;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
public class RpcSpringJmsIT {

    private static final String QUEUE_NAME = "test.queue.com.rabbitmq.integration.tests.RpcSpringJmsIT";
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcSpringJmsIT.class);

    Connection serverConnection, clientConnection;

    RpcServer rpcServer;

    protected static void drainQueue(Session session, Queue queue) throws Exception {
        MessageConsumer receiver = session.createConsumer(queue);
        Message msg = receiver.receiveNoWait();
        while (msg != null) {
            msg = receiver.receiveNoWait();
        }
    }

    @BeforeEach
    public void init() throws Exception {
        ConnectionFactory connectionFactory = AbstractTestConnectionFactory.getTestConnectionFactory()
            .getConnectionFactory();
        clientConnection = connectionFactory.createConnection();
        clientConnection.start();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (rpcServer != null) {
            rpcServer.close();
        }
        if (clientConnection != null) {
            clientConnection.close();
        }
        if (serverConnection != null) {
            serverConnection.close();
        }
        com.rabbitmq.client.ConnectionFactory cf = new com.rabbitmq.client.ConnectionFactory();
        try (com.rabbitmq.client.Connection c = cf.newConnection()) {
            c.createChannel().queueDelete(QUEUE_NAME);
        }
    }

    @Test
    public void rpcWithSpringJmsTemplate() throws Exception {
        setupRpcServer();

        String messageContent = UUID.randomUUID().toString();
        JmsTemplate tpl = new JmsTemplate(new SingleConnectionFactory(clientConnection));
        tpl.setReceiveTimeout(2000);
        Message response = tpl.sendAndReceive(QUEUE_NAME, session -> {
            TextMessage message = session.createTextMessage(messageContent);
            message.setJMSCorrelationID(messageContent);
            return message;
        });

        assertThat(response).isNotNull().isInstanceOf(TextMessage.class);
        assertThat(response.getJMSCorrelationID()).isEqualTo(messageContent);
        assertThat(((TextMessage) response).getText()).isEqualTo("*** " + messageContent + " ***");
    }

    @Test
    public void rpcWithSpringJmsTemplateDirectReplyTo() throws Exception {
        setupRpcServer();

        String messageContent = UUID.randomUUID().toString();
        JmsTemplate tpl = new JmsTemplate(new SingleConnectionFactory(clientConnection));
        Message response = tpl.execute(new DirectReplyToSessionCallback(messageContent));

        assertThat(response).isNotNull().isInstanceOf(TextMessage.class);
        assertThat(response.getJMSCorrelationID()).isEqualTo(messageContent);
        assertThat(((TextMessage) response).getText()).isEqualTo("*** " + messageContent + " ***");
    }

    @Test
    public void rpcWithSpringJmsTemplateAndSpringBasedRpcServer() throws Exception {
        try (AnnotationConfigApplicationContext ignored = new AnnotationConfigApplicationContext(JmsSpringConfiguration.class)) {
            String messageContent = UUID.randomUUID().toString();
            JmsTemplate tpl = new JmsTemplate(new SingleConnectionFactory(clientConnection));
            tpl.setReceiveTimeout(2000);
            Message response = tpl.sendAndReceive(QUEUE_NAME, session -> {
                TextMessage message = session.createTextMessage(messageContent);
                message.setJMSCorrelationID(messageContent);
                return message;
            });

            assertThat(response).isNotNull().isInstanceOf(TextMessage.class);
            assertThat(response.getJMSCorrelationID()).isEqualTo(messageContent);
            assertThat(((TextMessage) response).getText()).isEqualTo("*** " + messageContent + " ***");
        }
    }

    @Test
    public void rpcWithSpringJmsTemplateAndSpringBasedRpcServerDirectReplyTo() throws Exception {
        try (AnnotationConfigApplicationContext ignored = new AnnotationConfigApplicationContext(JmsSpringConfiguration.class)) {
            String messageContent = UUID.randomUUID().toString();
            JmsTemplate tpl = new JmsTemplate(new SingleConnectionFactory(clientConnection));

            Message response = tpl.execute(new DirectReplyToSessionCallback(messageContent));

            assertThat(response).isNotNull().isInstanceOf(TextMessage.class);
            assertThat(response.getJMSCorrelationID()).isEqualTo(messageContent);
            assertThat(((TextMessage) response).getText()).isEqualTo("*** " + messageContent + " ***");
        }
    }

    void setupRpcServer() throws Exception {
        RMQConnectionFactory connectionFactory = (RMQConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory()
            .getConnectionFactory();
        connectionFactory.setDeclareReplyToDestination(false);
        serverConnection = connectionFactory.createConnection();
        serverConnection.start();
        Session session = serverConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue(QUEUE_NAME);
        drainQueue(session, queue);
        session.close();
        rpcServer = new RpcServer(serverConnection);
    }

    private static class DirectReplyToSessionCallback implements SessionCallback<Message> {

        final String content;

        private DirectReplyToSessionCallback(String content) {
            this.content = content;
        }

        @Override
        public Message doInJms(Session session) throws JMSException {
            TextMessage message = session.createTextMessage(content);
            message.setJMSCorrelationID(content);

            RMQDestination replyQueue = new RMQDestination(
                "amq.rabbitmq.reply-to", "", "amq.rabbitmq.reply-to", "amq.rabbitmq.reply-to"
            );
            replyQueue.setDeclared(true);
            MessageProducer producer = session.createProducer(session.createQueue(QUEUE_NAME));

            MessageConsumer responseConsumer = session.createConsumer(replyQueue);
            BlockingQueue<Message> queue = new ArrayBlockingQueue<>(1);
            responseConsumer.setMessageListener(msg -> queue.add(msg));
            message.setJMSReplyTo(replyQueue);
            producer.send(message);
            try {
                Message response = queue.poll(2, TimeUnit.SECONDS);
                responseConsumer.close();
                return response;
            } catch (InterruptedException e) {
                throw new RMQJMSException(e);
            }
        }
    }

    @Configuration
    @EnableJms
    public static class JmsSpringConfiguration implements DisposableBean {

        @Autowired
        Connection connection;

        @Bean
        Connection connection() throws Exception {
            RMQConnectionFactory cf = (RMQConnectionFactory) AbstractTestConnectionFactory.getTestConnectionFactory().getConnectionFactory();
            cf.setDeclareReplyToDestination(false);
            return cf.createConnection();
        }

        @Bean
        ConnectionFactory connectionFactory() throws Exception {
            return new SingleConnectionFactory(connection());
        }

        @Bean
        DefaultJmsListenerContainerFactory jmsListenerContainerFactory() throws Exception {
            DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
            factory.setConnectionFactory(connectionFactory());
            return factory;
        }

        @JmsListener(destination = QUEUE_NAME)
        public org.springframework.messaging.Message<String> consume(String content,
            @Header("jms_correlationId")
                String correlationId) {
            return MessageBuilder.withPayload("*** " + content + " ***")
                .setHeader("jms_correlationId", correlationId)
                .setHeader("JMSType", "TextMessage")
                .build();
        }

        @Override
        public void destroy() throws Exception {
            connection.close();
        }
    }

    private static class RpcServer {

        Session session;

        public RpcServer(Connection connection) throws JMSException {
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(QUEUE_NAME);
            MessageProducer replyProducer = session.createProducer(null);
            MessageConsumer consumer = session.createConsumer(destination);
            consumer.setMessageListener(msg -> {
                TextMessage message = (TextMessage) msg;
                try {
                    String text = message.getText();
                    Destination replyQueue = message.getJMSReplyTo();
                    if (replyQueue != null) {
                        TextMessage replyMessage = session.createTextMessage("*** " + text + " ***");
                        replyMessage.setJMSCorrelationID(message.getJMSCorrelationID());
                        replyMessage.setStringProperty("JMSType", "TextMessage");
                        replyProducer.send(replyQueue, replyMessage);
                    }
                } catch (JMSException e) {
                    LOGGER.warn("Error in RPC server", e);
                }
            });
        }

        void close() {
            try {
                session.close();
            } catch (Exception e) {

            }
        }
    }

    private static class SingleConnectionFactory implements ConnectionFactory {

        private final Connection connection;

        private SingleConnectionFactory(Connection connection) {
            this.connection = connection;
        }

        @Override
        public Connection createConnection() {
            return connection;
        }

        @Override
        public Connection createConnection(String userName, String password) {
            return connection;
        }

        @Override
        public JMSContext createContext() {
            // TODO JMS 2.0
            throw new UnsupportedOperationException();
        }

        @Override
        public JMSContext createContext(String userName, String password) {
            // TODO JMS 2.0
            throw new UnsupportedOperationException();
        }

        @Override
        public JMSContext createContext(String userName, String password, int sessionMode) {
            // TODO JMS 2.0
            throw new UnsupportedOperationException();
        }

        @Override
        public JMSContext createContext(int sessionMode) {
            // TODO JMS 2.0
            throw new UnsupportedOperationException();
        }

    }


}
