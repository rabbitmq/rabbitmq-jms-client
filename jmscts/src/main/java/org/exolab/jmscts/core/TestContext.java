/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact tma@netspace.net.au.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001-2004 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: TestContext.java,v 1.7 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.XAQueueConnection;
import javax.jms.XAQueueConnectionFactory;
import javax.jms.XAQueueSession;
import javax.jms.XATopicConnection;
import javax.jms.XATopicConnectionFactory;
import javax.jms.XATopicSession;

import org.exolab.jmscts.provider.Administrator;

/**
 * Instances of this class maintain the context of the current test case.
 * <br/>
 * Root contexts are constructed with {@link TestContext#TestContext(TestCoverage) TestContext(TestConverage)}
 * or {@link TestContext#TestContext(TestStatistics) TestContext(TestStatistics)}.
 * <br/>
 * All other <code>TestContext</code>s are created by supplying a parent context.
 *
 * @version     $Revision: 1.7 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         JMSTestCase
 * @see         JMSTestRunner
 * @see         ProviderTestRunner
 * @see         ConnectionTestRunner
 * @see         SessionTestRunner
 * @see         MessageTestRunner
 * @see         SendReceiveTestRunner
 * @see         TestCoverage
 */
public final class TestContext {

    /**
     * The parent context
     */
    private TestContext _parent = null;

    /**
     * The test coverage of requirements
     */
    private TestCoverage _coverage = null;

    /**
     * The test statistics
     */
    private TestStatistics _statistics = null;

    /**
     * The JMS provider administration interface
     */
    private Administrator _administrator = null;

    /**
     * The current connection factory
     */
    private ConnectionFactory _factory = null;

    /**
     * The connection factory interface class. This is required to identify
     * the connection factory implementation as some vendors
     * (eg. Sun) implement queue and topic connection factories using the
     * same class, but fail to create connections if the factory was intended
     * to create a different connection type.
     */
    private Class<?> _factoryType = null;

    /**
     * The current connection
     */
    private Connection _connection = null;

    /**
     * The current session
     */
    private Session _session = null;

    /**
     * The current session's message acknowledgement type
     */
    private AckType _type = null;

    /**
     * The message type
     */
    private Class<?> _messageType;

    /**
     * The current message
     */
    private Message _message = null;

    /**
     * The messaging behaviour
     */
    private MessagingBehaviour _behaviour = null;

    /**
     * Indicates if the context is invalid
     */
    private boolean _invalid = false;


    /**
     * Construct a requirements test root context
     *
     * @param coverage the test coverage of requirements
     */
    public TestContext(TestCoverage coverage) {
        if (coverage == null) {
            throw new IllegalArgumentException("Argument 'coverage' is null");
        }
        _coverage = coverage;
    }

    /**
     * Construct a stress test root context
     *
     * @param statistics the stress test statistics
     */
    public TestContext(TestStatistics statistics) {
        if (statistics == null) {
            throw new IllegalArgumentException(
                "Argument 'statistics' is null");
        }
        _statistics = statistics;
    }

    /**
     * Construct an administrator context. The parent must be a root context.
     *
     * @param parent a root context
     * @param administrator the JMS provider administration interface
     */
    public TestContext(TestContext parent, Administrator administrator) {
        if (parent == null) {
            throw new IllegalArgumentException("Argument 'parent' is null");
        }
        if (administrator == null) {
            throw new IllegalArgumentException(
                "Argument 'administrator' is null");
        }
        _parent = parent;
        _administrator = administrator;
    }

    /**
     * Construct a connection factory context. The parent context must be
     * an administrator context.
     *
     * @param parent an administrator context
     * @param factory the connection factory
     * @param type the connection factory interface class. This is required to
     * identify the connection factory implementation as some vendors
     * (eg. Sun) implement queue and topic connection factories using the
     * same class, but fail to create connections if the factory was intended
     * to create a different connection type.
     */
    public TestContext(TestContext parent, ConnectionFactory factory,
                       Class<?> type) {
        if (parent == null) {
            throw new IllegalArgumentException("Argument 'parent' is null");
        }
        if (factory == null) {
            throw new IllegalArgumentException("Argument 'factory' is null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Argument 'type' is null");
        }
        if (!type.equals(QueueConnectionFactory.class)
            && !type.equals(TopicConnectionFactory.class)
            && !type.equals(XAQueueConnectionFactory.class)
            && !type.equals(XATopicConnectionFactory.class)) {
            throw new IllegalArgumentException(
                "Argument 'type' is not a valid connection factory interface");
        }
        _parent = parent;
        _factory = factory;
        _factoryType = type;
    }

    /**
     * Construct a connection context. The parent context must be a connection
     * factory context.
     *
     * @param parent a connection factory context
     * @param connection the connection
     */
    public TestContext(TestContext parent, Connection connection) {
        if (parent == null) {
            throw new IllegalArgumentException("Argument 'parent' is null");
        }
        if (connection == null) {
            throw new IllegalArgumentException(
                "Argument 'connection' is null");
        }
        _parent = parent;
        _connection = connection;
    }

    /**
     * Construct a session context. The parent context must be a connection
     * context.
     *
     * @param parent a connection context
     * @param session the session
     * @param type the message acknowledgement type of the session
     */
    public TestContext(TestContext parent, Session session, AckType type) {
        if (parent == null) {
            throw new IllegalArgumentException("Argument 'parent' is null");
        }
        if (session == null) {
            throw new IllegalArgumentException("Argument 'session' is null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Argument 'type' is null");
        }
        _parent = parent;
        _session = session;
        _type = type;
    }

    /**
     * Construct a message context. The parent context must be a session
     * context.
     *
     * @param parent a session context
     * @param messageType the message type
     */
    public TestContext(TestContext parent, Class<?> messageType) {
        if (parent == null) {
            throw new IllegalArgumentException("Argument 'parent' is null");
        }
        if (messageType == null) {
            throw new IllegalArgumentException(
                "Argument 'messageType' is null");
        }
        _parent = parent;
        _messageType = messageType;
    }

    /**
     * Construct a message context. The parent context must be a session
     * context.
     *
     * @param parent a session context
     * @param message the message
     */
    public TestContext(TestContext parent, Message message) {
        if (parent == null) {
            throw new IllegalArgumentException("Argument 'parent' is null");
        }
        if (message == null) {
            throw new IllegalArgumentException("Argument 'message' is null");
        }
        _parent = parent;
        _message = message;
        _messageType = MessageTypes.getType(message);
    }

    /**
     * Construct a send/receive context. The parent context must be a session
     * context.
     *
     * @param parent a session context
     * @param messageType the message type
     * @param behaviour the messaging behaviour
     */
    public TestContext(TestContext parent, Class<?> messageType,
                       MessagingBehaviour behaviour) {
        this(parent, messageType);
        if (behaviour == null) {
            throw new IllegalArgumentException("Argument 'behaviour' is null");
        }
        _behaviour = behaviour;
    }

    /**
     * Construct a send/receive context. The parent context must be a session
     * context.
     *
     * @param parent a session context
     * @param message the message
     * @param behaviour the messaging behaviour
     */
    public TestContext(TestContext parent, Message message,
                       MessagingBehaviour behaviour) {
        this(parent, message);
        if (behaviour == null) {
            throw new IllegalArgumentException("Argument 'behaviour' is null");
        }
        _behaviour = behaviour;
    }

    /**
     * Returns the test coverage of requirements
     *
     * @return the test coverage of requirements
     */
    public TestCoverage getCoverage() {
        TestCoverage coverage = _coverage;
        if (coverage == null && _parent != null) {
            coverage = _parent.getCoverage();
        }
        return coverage;
    }

    /**
     * Returns the test statistics
     *
     * @return the test statistics
     */
    public TestStatistics getStatistics() {
        TestStatistics statistics = _statistics;
        if (statistics == null && _parent != null) {
            statistics = _parent.getStatistics();
        }
        return statistics;
    }

    /**
     * Returns the JMS provider's administration interface
     *
     * @return the administration interface
     */
    public Administrator getAdministrator() {
        Administrator administrator = _administrator;
        if (administrator == null && _parent != null) {
            administrator = _parent.getAdministrator();
        }
        return administrator;
    }

    /**
     * Returns the current connection factory
     *
     * @return the current connection factory
     */
    public ConnectionFactory getConnectionFactory() {
        ConnectionFactory factory = _factory;
        if (factory == null && _parent != null) {
            factory = _parent.getConnectionFactory();
        }
        return factory;
    }

    /**
     * Returns the connection factory type
     *
     * @return the connection factory type
     */
    public Class<?> getConnectionFactoryType() {
        Class<?> type = _factoryType;
        if (type == null && _parent != null) {
            type = _parent.getConnectionFactoryType();
        }
        return type;
    }

    /**
     * Determines if the connection factory is a QueueConnectionFactory
     *
     * @return <code>true</code> if the connection factory is a
     * <code>QueueConnectionFactory</code>
     */
    public boolean isQueueConnectionFactory() {
        Class<?> type = getConnectionFactoryType();
        return (type != null && type.equals(QueueConnectionFactory.class));
    }

    /**
     * Determines if the connection factory is a TopicConnectionFactory
     *
     * @return <code>true</code> if the connection factory is a
     * <code>TopicConnectionFactory</code>
     */
    public boolean isTopicConnectionFactory() {
        Class<?> type = getConnectionFactoryType();
        return (type != null && type.equals(TopicConnectionFactory.class));
    }

    /**
     * Determines if the connection factory is an XAQueueConnectionFactory
     *
     * @return <code>true</code> if the connection factory is an
     * <code>XAQueueConnectionFactory</code>
     */
    public boolean isXAQueueConnectionFactory() {
        Class<?> type = getConnectionFactoryType();
        return (type != null && type.equals(XAQueueConnectionFactory.class));
    }

    /**
     * Determines if the connection factory is an XATopicConnectionFactory
     *
     * @return <code>true</code> if the connection factory is an
     * <code>XATopicConnectionFactory</code>
     */
    public boolean isXATopicConnectionFactory() {
        Class<?> type = getConnectionFactoryType();
        return (type != null && type.equals(XATopicConnectionFactory.class));
    }

    /**
     * Determines if the context is a queue context, i.e., point-to-point
     *
     * @return <code>true</code> if this is a topic context
     */
    public boolean isQueue() {
        return isQueueConnectionFactory() || isXAQueueConnectionFactory();
    }

    /**
     * Determines if the context is a topic context, i.e.,
     * publish-and-subscribe
     *
     * @return <code>true</code> if this is a topic context
     */
    public boolean isTopic() {
        return !isQueue();
    }

    /**
     * Returns the current connection
     *
     * @return the current connection, or null, if this not a child of a
     * connection factory context
     */
    public Connection getConnection() {
        Connection connection = _connection;
        if (connection == null && _parent != null) {
            connection = _parent.getConnection();
        }
        return connection;
    }

    /**
     * Returns the current connection type
     *
     * @return the current connection type, or null, if this not a child of a
     * connection factory context
     */
    public Class<?> getConnectionType() {
        Class<?> result = null;
        Connection connection = getConnection();
        if (connection != null) {
            Class<?> type = getConnectionFactoryType();
            if (type.equals(QueueConnectionFactory.class)) {
                result = QueueConnection.class;
            } else if (type.equals(TopicConnectionFactory.class)) {
                result = TopicConnection.class;
            } else if (type.equals(XAQueueConnectionFactory.class)) {
                result = XAQueueConnection.class;
            } else {
                result = XATopicConnection.class;
            }
        }
        return result;
    }

    /**
     * Returns the current session
     *
     * @return the current session, or null, if this not a child of a
     * connection context
     */
    public Session getSession() {
        Session session = _session;
        if (session == null && _parent != null) {
            session = _parent.getSession();
        }
        return session;
    }

    /**
     * Returns the current session type
     *
     * @return the current session type, or null, if this not a child of a
     * connection context
     */
    public Class<?> getSessionType() {
        Class<?> result = null;
        Session session = getSession();
        if (session != null) {
            Class<?> type = getConnectionFactoryType();
            if (type.equals(QueueConnectionFactory.class)) {
                result = QueueSession.class;
            } else if (type.equals(TopicConnectionFactory.class)) {
                result = TopicSession.class;
            } else if (type.equals(XAQueueConnectionFactory.class)) {
                result = XAQueueSession.class;
            } else {
                result = XATopicSession.class;
            }
        }
        return result;
    }

    /**
     * Returns the current session's message acknowledgement type
     *
     * @return the message acknowledgement type of the session, or null,
     * if this is not a child of a connection context
     */
    public AckType getAckType() {
        AckType type = _type;
        if (type == null && _parent != null) {
            type = _parent.getAckType();
        }
        return type;
    }

    /**
     * Returns the current message
     *
     * @return the current message, or null, if this not a child of a message
     * context
     */
    public Message getMessage() {
        Message message = _message;
        if (message == null && _parent != null) {
            message = _parent.getMessage();
        }
        return message;
    }

    /**
     * Returns the current message type
     *
     * @return the current message type, or null, if this not a child of a
     * message context
     */
    public Class<?> getMessageType() {
        Class<?> type = _messageType;
        if (type == null && _parent != null) {
            type = _parent.getMessageType();
        }
        return type;
    }

    /**
     * Returns the current messaging behaviour
     *
     * @return the current messaging behaviour, or null, if this not a child of
     * a send/receive  context
     */
    public MessagingBehaviour getMessagingBehaviour() {
        MessagingBehaviour behaviour = _behaviour;
        if (behaviour == null && _parent != null) {
            behaviour = _parent.getMessagingBehaviour();
        }
        return behaviour;
    }

    /**
     * Invalidate the connection and/or session managed by this context
     */
    public void invalidate() {
        TestContext context = this;
        while (context._session == null && context._parent != null) {
            context = context._parent;
        }
        context._session = null;
        context._invalid = true;

        context = this;
        while (context._connection == null && context._parent != null) {
            context = context._parent;
        }
        context._connection = null;
        context._invalid = true;
    }

    /**
     * Determines if this context is invalid
     *
     * @return <code>true</code> if this context is invalid
     */
    public boolean isInvalid() {
        TestContext context = this;
        while (!context._invalid && context._parent != null) {
            context = context._parent;
        }
        return context._invalid;
    }

    /**
     * Returns the parent context
     *
     * @return the parent context, or <code>null</code> if there is no parent
     * context
     */
    public TestContext getParent() {
        return _parent;
    }

    /**
     * Close this context
     *
     * @throws JMSException if a JMS error is encountered
     */
    public void close() throws JMSException {
        if (_session != null) {
            _session.close();
        }

        if (_connection != null) {
            _connection.close();
        }
    }

    /**
     * Returns a string representation of the context
     *
     * @return a string representation of the context
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        String type = null;

        if (getConnectionFactoryType() != null) {
            if (isQueueConnectionFactory()) {
                type = "queue";
            } else if (isTopicConnectionFactory()) {
                type = "topic";
            } else if (isXAQueueConnectionFactory()) {
                type = "XA queue";
            } else {
                type = "XA topic";
            }
        }

        ConnectionFactory factory = getConnectionFactory();
        Connection connection = getConnection();
        Session session = getSession();
        Message message = getMessage();
        MessagingBehaviour behaviour = getMessagingBehaviour();

        if (factory != null && connection == null) {
            result.append(type);
            result.append(" connection factory");
        } else if (connection != null && session == null) {
            result.append(type);
            result.append(" connection");
        } else if (session != null) {
            result.append(type);
            result.append(" session, mode=");
            result.append(getAckType().toString());
            if (message != null) {
                result.append(", message type=");
                result.append(MessageTypes.getType(message).getName());
            }
            if (behaviour != null) {
                result.append(", ");
                result.append(behaviour.toString());
            }
        }
        return result.toString();
    }

}
