// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2018-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.integration.tests;

import com.rabbitmq.jms.admin.RMQConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class RpcIT {

    private static final String QUEUE_NAME = "test.queue." + RpcIT.class.getCanonicalName();
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcIT.class);

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
        setupRpcServer();
    }

    @AfterEach
    public void tearDown() throws Exception {
        rpcServer.close();
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
    public void rpc() throws Exception {
        String messageContent = UUID.randomUUID().toString();
        Message response = doRpc(messageContent);
        assertThat(response).isNotNull().isInstanceOf(TextMessage.class);
        assertThat(response.getJMSCorrelationID()).isEqualTo(messageContent);
        assertThat(((TextMessage) response).getText()).isEqualTo("*** " + messageContent + " ***");
    }

    Message doRpc(String messageContent) throws Exception {
        Session session = clientConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        TextMessage message = session.createTextMessage(messageContent);
        message.setJMSCorrelationID(messageContent);

        Destination replyQueue = session.createTemporaryQueue();
        MessageProducer producer = session.createProducer(session.createQueue(QUEUE_NAME));

        MessageConsumer responseConsumer = session.createConsumer(replyQueue);
        BlockingQueue<Message> queue = new ArrayBlockingQueue<>(1);
        responseConsumer.setMessageListener(msg -> queue.add(msg));
        message.setJMSReplyTo(replyQueue);
        producer.send(message);
        Message response = queue.poll(2, TimeUnit.SECONDS);
        responseConsumer.close();
        return response;
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
}
