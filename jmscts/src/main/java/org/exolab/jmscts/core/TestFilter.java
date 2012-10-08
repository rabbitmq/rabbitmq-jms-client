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
 * $Id: TestFilter.java,v 1.8 2005/06/16 08:14:00 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.util.Enumeration;
import java.util.HashMap;

import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.QueueConnectionFactory;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;
import javax.jms.TopicConnectionFactory;
import javax.jms.XAQueueConnectionFactory;
import javax.jms.XATopicConnectionFactory;

import junit.extensions.TestDecorator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;


import org.exolab.jmscts.core.filter.Exclude;
import org.exolab.jmscts.core.filter.Filter;
import org.exolab.jmscts.core.filter.Include;
import org.exolab.jmscts.core.filter.Selector;
import org.exolab.jmscts.core.types.DeliveryModeType;
import org.exolab.jmscts.core.types.DestinationType;
import org.exolab.jmscts.core.types.FactoryType;
import org.exolab.jmscts.core.types.MessageType;
import org.exolab.jmscts.core.types.ReceiverType;
import org.exolab.jmscts.core.types.SessionType;


/**
 * Instances of this class determine if a test case is to be tested or not,
 * based on a filter specified by {@link Filter}
 *
 * @version     $Revision: 1.8 $ $Date: 2005/06/16 08:14:00 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class TestFilter {

    /**
     * The filter
     */
    private Filter _filter;

    /**
     * The set of included tests
     */
    private Include[] _includes;

    /**
     * The set of excluded tests
     */
    private Exclude[] _excludes;

    /**
     * Pattern matcher for wildcarded tests
     */
    private PatternMatcher _matcher = new Perl5Matcher();

    /**
     * The logger
     */
    private static final Logger log = Logger.getLogger(TestFilter.class);

    /**
     * A map of connection factory classes, to their corresponding
     * <code>FactoryType</code>s
     */
    private static final HashMap<Class<?>, FactoryType> FACTORIES;

    /**
     * A map of <code>AckType</code>s to their corresponding
     * <code>SessionType</code>s
     */
    private static final HashMap<AckType, SessionType> SESSIONS;

    /**
     * A map of message classes, to their corresponding
     * <code>MessageType</code>s
     */
    private static final HashMap<Class<?>, MessageType> MESSAGES;


    /**
     * Construct a new <code>TestFilter</code>
     *
     * @param filter the filter
     */
    public TestFilter(Filter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Argument 'filter' is null");
        }
        _filter = filter;
        _includes = _filter.getInclude();
        _excludes = _filter.getExclude();
    }

    /**
     * Determines if a test is included by the filter
     *
     * @param factoryType the class of the connection factorry
     * @param test the test case
     * @return <code>true</code> if it is included, <code>false</code>
     * otherwise
     */
    public boolean includes(Class<?> factoryType, Test test) {
        boolean result = false;
        FactoryType factory = FACTORIES.get(factoryType);
        if (!excludes(factory, null, null, null, null, null, test)
            && includes(factory, null, null, null, null, null, test)) {
            result = true;
        }
        if (log.isDebugEnabled()) {
            log.debug("include(factory=" + factory
                      + ", test=" + getTestName(test) + ")=" + result);
        }
        return result;
    }

    /**
     * Determines if a test is included by the filter
     *
     * @param context the test context
     * @param mode the session acknowledgement mode
     * @param test the test case
     * @return <code>true</code> if it is included, <code>false</code>
     * otherwise
     */
    public boolean includes(TestContext context, AckType mode, Test test) {
        boolean result = false;
        FactoryType factory = FACTORIES.get(
                context.getConnectionFactoryType());
        SessionType session = SESSIONS.get(mode);

        if (!excludes(factory, session, null, null, null, null, test)
            && includes(factory, session, null, null, null, null, test)) {
            result = true;
        }
        if (log.isDebugEnabled()) {
            log.debug("include(factory=" + factory + ", session=" + session
                      + ", test=" + getTestName(test) + ")=" + result);
        }
        return result;
    }

    /**
     * Determines if a test is included by the filter
     *
     * @param context the test context
     * @param messageType the message type
     * @param test the test case
     * @return <code>true</code> if it is included, <code>false</code>
     * otherwise
     */
    public boolean includes(TestContext context, Class<?> messageType,
                            Test test) {
        boolean result = false;
        FactoryType factory = FACTORIES.get(
            context.getConnectionFactoryType());
        SessionType session = SESSIONS.get(context.getAckType());
        MessageType message = MESSAGES.get(messageType);

        if (!excludes(factory, session, null, null, null, message, test)
            && includes(factory, session, null, null, null, message, test)) {
            result = true;
        }
        if (log.isDebugEnabled()) {
            log.debug("include(factory=" + factory + ", session=" + session
                      + ", message=" + message + ", test=" + getTestName(test)
                      + ")=" + result);
        }
        return result;
    }

    /**
     * Determines if a test is included by the filter
     *
     * @param context the test context
     * @param behaviour the messaging behaviour
     * @param messageType the message type
     * @param test the test case
     * @return <code>true</code> if it is included, <code>false</code>
     * otherwise
     */
    public boolean includes(TestContext context, MessagingBehaviour behaviour,
                            Class<?> messageType, Test test) {
        boolean result = false;

        FactoryType factory = FACTORIES.get(
            context.getConnectionFactoryType());

        SessionType session = SESSIONS.get(
            context.getAckType());

        DestinationType destination = (behaviour.getAdministered())
            ? DestinationType.ADMINISTERED : DestinationType.TEMPORARY;

        DeliveryModeType delivery;
        if (behaviour.getDeliveryMode() == DeliveryMode.PERSISTENT) {
            delivery = DeliveryModeType.PERSISTENT;
        } else {
            delivery = DeliveryModeType.NON_PERSISTENT;
        }

        ReceiverType receiver = null;
        ReceiptType receipt = behaviour.getReceiptType();
        if (receipt != null) {
            if (receipt.equals(ReceiptType.SYNCHRONOUS)) {
                receiver = (behaviour.getDurable())
                    ? ReceiverType.DURABLE_SYNCHRONOUS
                    : ReceiverType.SYNCHRONOUS;
            } else if (receipt.equals(ReceiptType.ASYNCHRONOUS)) {
                receiver = (behaviour.getDurable())
                    ? ReceiverType.DURABLE_ASYNCHRONOUS
                    : ReceiverType.ASYNCHRONOUS;
            } else {
                receiver = ReceiverType.BROWSER;
            }
        }

        MessageType message = MESSAGES.get(messageType);

        if (!excludes(factory, session, destination, delivery, receiver,
                      message, test)
            && includes(factory, session, destination, delivery, receiver,
                        message, test)) {
            result = true;
        }
        if (log.isDebugEnabled()) {
            log.debug("include(factory=" + factory + ", session=" + session
                      + ", destination=" + destination + ", delivery="
                      + delivery + ", receiver=" + receiver + ", message="
                      + message + ", test=" + test + ")=" + result);
        }
        return result;
    }

    /**
     * Determines if a test is included by the filter
     *
     * @param factory the connection factory
     * @param session the session type
     * @param destination the destination type
     * @param delivery the delivery mode
     * @param receiver the receiver type
     * @param message the message type
     * @param test the test case
     * @return <code>true</code> if it is included, <code>false</code>
     * otherwise
     */
    private boolean includes(FactoryType factory, SessionType session,
                             DestinationType destination,
                             DeliveryModeType delivery, ReceiverType receiver,
                             MessageType message, Test test) {
        boolean result = (_includes == null || _includes.length == 0);
        if (!result) {
            for (int i = 0; i < _includes.length; ++i) {
                if (includes(_includes[i], factory, session, destination,
                             delivery, receiver, message, test)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Determines if a test is included by the filter
     *
     * @param selector the filter criteria
     * @param factory the connection factory
     * @param session the session type
     * @param destination the destination type
     * @param delivery the delivery mode
     * @param receiver the receiver type
     * @param message the message type
     * @param test the test case
     * @return <code>true</code> if it is included, <code>false</code>
     * otherwise
     */
    private boolean includes(Selector selector, FactoryType factory,
                             SessionType session, DestinationType destination,
                             DeliveryModeType delivery, ReceiverType receiver,
                             MessageType message, Test test) {
        boolean result = false;
        if (selector.getTest() == null) {
            result = true;
        } else if (test != null) {
            result = containsTest(test, selector.getTest());
        } else {
            result = true;
        }

        if (result) {
            result = includes(selector.getFactory(), factory)
                && includes(selector.getSession(), session)
                && includes(selector.getDestination(), destination)
                && includes(selector.getDeliveryMode(), delivery)
                && includes(selector.getReceiver(), receiver)
                && includes(selector.getMessage(), message);
        }
        return result;
    }

    /**
     * Determines if an object is included
     *
     * @param selector the filter criteria
     * @param object the object to compare
     * @return <code>true</code> if it is included, <code>false</code>
     * otherwise
     */
    private boolean includes(Object selector, Object object) {
        boolean result = false;
        if (selector == null || selector.equals(object) || object == null) {
            result = true;
        }
        if (log.isDebugEnabled()) {
            log.debug("includes(selector=" + selector + ", object=" + object
                      + ")=" + result);
        }
        return result;
    }

    /**
     * Determines if a test is excluded by the filter
     *
     * @param factory the connection factory
     * @param session the session type
     * @param destination the destination type
     * @param delivery the delivery mode
     * @param receiver the receiver type
     * @param message the message type
     * @param test the test case
     * @return <code>true</code> if it is excluded, <code>false</code>
     * otherwise
     */
    private boolean excludes(FactoryType factory, SessionType session,
                             DestinationType destination,
                             DeliveryModeType delivery, ReceiverType receiver,
                             MessageType message, Test test) {
        boolean result = false;
        if (_excludes != null && _excludes.length != 0) {
            for (int i = 0; i < _excludes.length; ++i) {
                if (excludes(_excludes[i], factory, session, destination,
                             delivery, receiver, message, test)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Determines if a test is excluded by the filter
     *
     * @param selector the filter criteria
     * @param factory the connection factory
     * @param session the session type
     * @param destination the destination type
     * @param delivery the delivery mode
     * @param receiver the receiver type
     * @param message the message type
     * @param test the test case
     * @return <code>true</code> if it is excluded, <code>false</code>
     * otherwise
     */
    private boolean excludes(Selector selector, FactoryType factory,
                             SessionType session, DestinationType destination,
                             DeliveryModeType delivery, ReceiverType receiver,
                             MessageType message, Test test) {
        boolean result = false;
        if (selector.getTest() != null && test != null) {
            result = containsTest(test, selector.getTest());
        }

        if (!result) {
            result = excludes(selector.getFactory(), factory)
                || excludes(selector.getSession(), session)
                || excludes(selector.getDestination(), destination)
                || excludes(selector.getDeliveryMode(), delivery)
                || excludes(selector.getReceiver(), receiver)
                || excludes(selector.getMessage(), message);
        }
        return result;
    }

    /**
     * Determines if an object is excluded
     *
     * @param selector the filter criteria
     * @param object the object to compare
     * @return <code>true</code> if it is excluded, <code>false</code>
     * otherwise
     */
    private boolean excludes(Object selector, Object object) {
        boolean result = false;
        if (selector != null && selector.equals(object)) {
            result = true;
        }
        if (log.isDebugEnabled()) {
            log.debug("excludes(selector=" + selector + ", object=" + object
                      + ")=" + result);
        }
        return result;
    }

    /**
     * Determines if a test is included by a regular expression, matching
     * on its name.
     *
     * @param test the test
     * @param regexp the regular expression
     * @return <code>true</code> if the test is included, <code>false</code>
     * otherwise
     */
    private boolean containsTest(Test test, String regexp) {
        Pattern pattern = null;
        try {
            Perl5Compiler compiler = new Perl5Compiler();
            pattern = compiler.compile(regexp);
        } catch (MalformedPatternException exception) {
            log.error("Invalid test case pattern: " + exception.getMessage());
        }

        return (pattern != null) ? containsTest(test, pattern) : false;
    }


    /**
     * Determines if a test is included by a regular expression, matching
     * on its name.
     * If the test is a suite of tests, each child test will be examined. If
     * there is any match, it returns true.
     *
     * @param test the test
     * @param pattern the regular expression pattern
     * @return <code>true</code> if the test is included, <code>false</code>
     * otherwise
     */
    private boolean containsTest(Test test, Pattern pattern) {
        boolean result = false;
        if (test instanceof TestSuite) {
            Enumeration<?> iterator = ((TestSuite) test).tests();
            while (iterator.hasMoreElements()) {
                Test contained = (Test) iterator.nextElement();
                if (containsTest(contained, pattern)) {
                    result = true;
                    break;
                }
            }
        } else if (test instanceof TestDecorator) {
            Test contained = ((TestDecorator) test).getTest();
            result = containsTest(contained, pattern);
        } else {
            String name = getTestName(test);
            result = _matcher.matches(name, pattern);
            if (log.isDebugEnabled()) {
                log.debug("containsTest(test=" + test + ", pattern="
                          + pattern.getPattern() + ")=" + result);
            }
        }
        return result;
    }

    /**
     * Helper to get the fully qualified name of a test
     *
     * @param test the test case
     * @return the fully qualified name of a test
     */
    private String getTestName(Test test) {
        String name = test.getClass().getName();
        if (test instanceof TestCase) {
            name += "." + ((TestCase) test).getName();
        }
        return name;
    }

    static {
        FACTORIES = new HashMap<Class<?>, FactoryType>();
        FACTORIES.put(QueueConnectionFactory.class,
                      FactoryType.QUEUECONNECTIONFACTORY);
        FACTORIES.put(TopicConnectionFactory.class,
                      FactoryType.TOPICCONNECTIONFACTORY);
        FACTORIES.put(XAQueueConnectionFactory.class,
                      FactoryType.XAQUEUECONNECTIONFACTORY);
        FACTORIES.put(XATopicConnectionFactory.class,
                      FactoryType.XATOPICCONNECTIONFACTORY);

        SESSIONS = new HashMap<AckType, SessionType>();
        SESSIONS.put(AckTypes.TRANSACTED, SessionType.TRANSACTED);
        SESSIONS.put(AckTypes.AUTO_ACKNOWLEDGE, SessionType.AUTO_ACKNOWLEDGE);
        SESSIONS.put(AckTypes.CLIENT_ACKNOWLEDGE,
                     SessionType.CLIENT_ACKNOWLEDGE);
        SESSIONS.put(AckTypes.DUPS_OK_ACKNOWLEDGE,
                     SessionType.DUPS_OK_ACKNOWLEDGE);

        MESSAGES = new HashMap<Class<?>, MessageType>();
        MESSAGES.put(BytesMessage.class, MessageType.BYTESMESSAGE);
        MESSAGES.put(MapMessage.class, MessageType.MAPMESSAGE);
        MESSAGES.put(ObjectMessage.class, MessageType.OBJECTMESSAGE);
        MESSAGES.put(StreamMessage.class, MessageType.STREAMMESSAGE);
        MESSAGES.put(TextMessage.class, MessageType.TEXTMESSAGE);
        MESSAGES.put(Message.class, MessageType.MESSAGE);
    }

}
