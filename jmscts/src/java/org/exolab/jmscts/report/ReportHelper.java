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
 * Copyright 2003-2004 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: ReportHelper.java,v 1.4 2004/02/02 03:50:24 tanderson Exp $
 */
package org.exolab.jmscts.report;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.jms.BytesMessage;
import javax.jms.DeliveryMode;
import javax.jms.Session;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.exolab.jmscts.core.AckType;
import org.exolab.jmscts.core.JMSTestCase;
import org.exolab.jmscts.core.MessagingBehaviour;
import org.exolab.jmscts.core.ReceiptType;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.types.DeliveryModeType;
import org.exolab.jmscts.core.types.DestinationType;
import org.exolab.jmscts.core.types.FactoryType;
import org.exolab.jmscts.core.types.MessageType;
import org.exolab.jmscts.core.types.ReceiverType;
import org.exolab.jmscts.core.types.SessionType;


/**
 * Helper class to map between types
 *
 * @version     $Revision: 1.4 $ $Date: 2004/02/02 03:50:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public final class ReportHelper {

    /**
     * Prevent construction of utility class
     */
    private ReportHelper() {
    }

    /**
     * Maps a {@link JMSTestCase} instance to a {@link TestRuns}
     *
     * @param test the test case
     * @return a new <code>TestRuns</code> instance
     */
    public static TestRuns getTestRuns(JMSTestCase test) {
        TestRuns result = new TestRuns();
        result.setTest(getName(test));
        return result;
    }

    /**
     * Maps a {@link JMSTestCase} instance to a {@link TestRun}
     *
     * @param test the test case
     * @return a new <code>TestRun</code> instance
     */
    public static TestRun getTestRun(JMSTestCase test) {
        return getTestRun(test, test.getContext());
    }

    /**
     * Maps a {@link JMSTestCase} instance to a {@link TestRun},
     * using the specified context
     *
     * @param test the test case
     * @param context the test context. May be null.
     * @return a new <code>TestRun</code> instance
     */
    public static TestRun getTestRun(JMSTestCase test, TestContext context) {
        TestRun result = new TestRun();
        if (context != null) {
            result.setContext(getContext(context));
        }
        return result;
    }

    /**
     * Maps exceptions to a {@link Failure}
     *
     * @param cause the exception to map
     * @param rootCause the root cause of the exception.
     * May be <code>null</code>
     * @return the mapped exceptions
     */
    public static Failure getFailure(Throwable cause, Throwable rootCause) {
        String description = cause.getMessage();
        if (description == null) {
            description = cause.getClass().getName();
        }
        return getFailure(description, cause, rootCause);
    }

    /**
     * Maps exceptions to a {@link Failure}
     *
     * @param description a description of the failure
     * @param cause the exception to map
     * @param rootCause the root cause of the exception.
     * May be <code>null</code>
     * @return the mapped exceptions
     */
    public static Failure getFailure(String description, Throwable cause,
                                     Throwable rootCause) {
        Failure failure = new Failure();
        failure.setDescription(description);
        Cause reason = new Cause();
        reason.addAnyObject(getStackTrace(cause));
        failure.setCause(reason);
        if (rootCause != null) {
            RootCause rootReason = new RootCause();
            rootReason.addAnyObject(getStackTrace(rootCause));
            failure.setRootCause(rootReason);
        }
        return failure;
    }

    /**
     * Return a fully qualified name for a test case
     *
     * @param test the test case
     * @return the fully qualified name of the test case
     */
    public static String getName(JMSTestCase test) {
        return test.getClass().getName() + "." + test.getName();
    }

    /**
     * Maps a {@link TestContext} instance to a {@link Context}
     *
     * @param context the test context
     * @return the mapped context
     */
    public static Context getContext(TestContext context) {
        Context result = new Context();
        result.setFactory(getFactory(context));
        result.setSession(getSession(context));
        result.setMessage(getMessage(context));
        result.setBehaviour(getBehaviour(context));
        return result;
    }

    /**
     * Maps the factory in a {@link TestContext} to a {@link Factory}
     *
     * @param context the text context
     * @return the mapped factory
     */
    public static Factory getFactory(TestContext context) {
        Factory result = new Factory();

        if (context.isQueueConnectionFactory()) {
            result.setType(FactoryType.QUEUECONNECTIONFACTORY);
        } else if (context.isTopicConnectionFactory()) {
            result.setType(FactoryType.TOPICCONNECTIONFACTORY);
        } else if (context.isXAQueueConnectionFactory()) {
            result.setType(FactoryType.XAQUEUECONNECTIONFACTORY);
        } else {
            result.setType(FactoryType.XATOPICCONNECTIONFACTORY);
        }
        return result;
    }

    /**
     * Maps the session in a {@link TestContext} to a {@link SessionType}
     *
     * @param context the text context
     * @return the mapped session type, or null, if the context doesn't contain
     * a session
     */
    public static SessionType getSession(TestContext context) {
        SessionType result = null;
        AckType ack = context.getAckType();
        if (ack != null) {
            if (ack.getTransacted()) {
                result = SessionType.TRANSACTED;
            } else {
                switch (ack.getAcknowledgeMode()) {
                    case Session.AUTO_ACKNOWLEDGE:
                        result = SessionType.AUTO_ACKNOWLEDGE;
                        break;
                    case Session.CLIENT_ACKNOWLEDGE:
                        result = SessionType.CLIENT_ACKNOWLEDGE;
                        break;
                    default:
                        result = SessionType.DUPS_OK_ACKNOWLEDGE;
                }
            }
        }
        return result;
    }

    /**
     * Maps the message in a {@link TestContext} to a {@link MessageType}
     *
     * @param context the text context
     * @return the mapped message type, or null, if the context doesn't contain
     * a message
     */
    public static MessageType getMessage(TestContext context) {
        MessageType result = null;
        Class<?> type = context.getMessageType();
        if (type != null) {
            if (type == BytesMessage.class) {
                result = MessageType.BYTESMESSAGE;
            } else if (type == MapMessage.class) {
                result = MessageType.MAPMESSAGE;
            } else if (type == ObjectMessage.class) {
                result = MessageType.OBJECTMESSAGE;
            } else if (type == StreamMessage.class) {
                result = MessageType.STREAMMESSAGE;
            } else if (type == TextMessage.class) {
                result = MessageType.TEXTMESSAGE;
            } else if (type == Message.class) {
                result = MessageType.MESSAGE;
            }
        }
        return result;
    }

    /**
     * Maps the messaging behaviour in a {@link TestContext} to a
     * {@link Behaviour}
     *
     * @param context the text context
     * @return the mapped behaviour, or null, if the context doesn't contain
     * a {@link MessagingBehaviour} instance
     */
    public static Behaviour getBehaviour(TestContext context) {
        Behaviour result = null;
        MessagingBehaviour behaviour = context.getMessagingBehaviour();
        if (behaviour != null) {
            result = new Behaviour();
            if (behaviour.getDeliveryMode() == DeliveryMode.PERSISTENT) {
                result.setDeliveryMode(DeliveryModeType.PERSISTENT);
            } else {
                result.setDeliveryMode(DeliveryModeType.NON_PERSISTENT);
            }

            ReceiptType receipt = behaviour.getReceiptType();
            if (ReceiptType.SYNCHRONOUS.equals(receipt)) {
                if (behaviour.getDurable()) {
                    result.setReceiver(ReceiverType.DURABLE_SYNCHRONOUS);
                } else {
                    result.setReceiver(ReceiverType.SYNCHRONOUS);
                }
            } else if (ReceiptType.ASYNCHRONOUS.equals(receipt)) {
                if (behaviour.getDurable()) {
                    result.setReceiver(ReceiverType.DURABLE_ASYNCHRONOUS);
                } else {
                    result.setReceiver(ReceiverType.ASYNCHRONOUS);
                }
            } else if (ReceiptType.BROWSER.equals(receipt)) {
                result.setReceiver(ReceiverType.BROWSER);
            }

            if (behaviour.getAdministered()) {
                result.setDestination(DestinationType.ADMINISTERED);
            } else {
                result.setDestination(DestinationType.TEMPORARY);
            }
        }
        return result;
    }

    /**
     * Returns a stack trace as a string
     *
     * @param error the exception to get the stack trace from
     * @return the stack trace of <code>error</code>
     */
    private static String getStackTrace(Throwable error) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintStream print = new PrintStream(stream);
        error.printStackTrace(print);
        print.flush();
        String result = stream.toString();
        return result;
    }

}
