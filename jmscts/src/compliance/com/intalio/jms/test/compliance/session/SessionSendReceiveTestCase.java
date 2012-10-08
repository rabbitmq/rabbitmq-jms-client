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
 *    please contact jima@intalio.com.
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
 * Copyright 2001, 2003 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: SessionSendReceiveTestCase.java,v 1.5 2003/05/04 14:12:38 tanderson Exp $
 */
package org.exolab.jmscts.test.session;

import java.util.List;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.log4j.Logger;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.AckTypes;
import org.exolab.jmscts.core.DeliveryTypes;
import org.exolab.jmscts.core.DestinationHelper;
import org.exolab.jmscts.core.JMSTestRunner;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessageSender;
import org.exolab.jmscts.core.MessageTypes;
import org.exolab.jmscts.core.SessionHelper;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class provides helper methods for session send/receive test cases
 *
 * @author <a href="tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.5 $
 * @see AbstractSendReceiveTestCase
 */
public class SessionSendReceiveTestCase extends AbstractSendReceiveTestCase {

    /**
     * The logger
     */
    private static final Logger _log = 
        Logger.getLogger(SessionSendReceiveTestCase.class.getName());


    /**
     * Construct an instance of this class for a specific test case,
     * against all delivery types, and a set of message types
     *
     * @param name the name of test case
     * @param types the message types to test against
     * @param requirements a list of test methods and requirements that 
     * they cover. The first element in each list is the method name and the 
     * remaining elements the requirements that it covers.
     */
    public SessionSendReceiveTestCase(String name, MessageTypes types,
                                      String[][] requirements) {
        super(name, types, requirements);
    }

    /**
     * Construct an instance of this class for a specific test case,
     * against and a set of message and delivery types
     *
     * @param name the name of test case
     * @param types the message types to test against
     * @param delivery the message delivery types to test against
     * @param requirements a list of test methods and requirements that 
     * they cover. The first element in each list is the method name and the 
     * remaining elements the requirements that it covers.
     */
    public SessionSendReceiveTestCase(String name, MessageTypes types,
                                      DeliveryTypes delivery, 
                                      String[][] requirements) {
        super(name, types, delivery, requirements);
    }

    /**
     * Helper to create MessageReceiver instances for each destination returned
     * by {@link #getDestinations}
     *
     * @return a list of message receivers
     * @throws Exception for any error
     */
    protected MessageReceiver[] createReceivers() throws Exception {
        return createReceivers(getContext());
    }

    /**
     * Helper to create MessageReceiver instances for each destination returned
     * by {@link #getDestinations}
     *
     * @param context the test context to use
     * @return a list of message receivers
     * @throws Exception for any error
     */
    protected MessageReceiver[] createReceivers(TestContext context) 
        throws Exception {
        String[] destinations = getDestinations();
        MessageReceiver[] receivers = new MessageReceiver[destinations.length];
        for (int i = 0; i < destinations.length; ++i) {
            Destination destination = getDestination(destinations[i]);
            _log.debug("Creating receiver for destination=" + 
                       DestinationHelper.getName(destination));
            receivers[i] = SessionHelper.createReceiver(context, destination);
        }
        return receivers;
    }

    /**
     * Helper to create MessageSender instances for each destination returned
     * by {@link #getDestinations}
     *
     * @return a list of message senders
     * @throws Exception for any error
     */
    protected MessageSender[] createSenders() throws Exception {
        return createSenders(getContext());
    }

    /**
     * Helper to create MessageSender instances for each destination returned
     * by {@link #getDestinations}
     *
     * @param context the test context to use
     * @return a list of message senders
     * @throws Exception for any error
     */
    protected MessageSender[] createSenders(TestContext context) 
        throws Exception {
        String[] destinations = getDestinations();
        MessageSender[] senders = new MessageSender[destinations.length];
        for (int i = 0; i < destinations.length; ++i) {
            Destination destination = getDestination(destinations[i]);
            _log.debug("Creating sender for destination=" + 
                       DestinationHelper.getName(destination));
            senders[i] = SessionHelper.createSender(context, destination);
        }
        return senders;
    }

    /**
     * Helper to verify that each consumer receives the correct number of 
     * messages 
     *
     * @param receivers a list of {@link MessageReceiver} instances
     * @param count the number of expected messages
     * @throws Exception for any error
     */
    protected void receive(MessageReceiver[] receivers, int count) 
        throws Exception {
        TestContext context = getContext();
        for (int i = 0; i < receivers.length; ++i) {
            receive(context, receivers[i], count);
        }
    }

    /**
     * Helper to verify that each consumer receives the correct number of 
     * messages 
     *
     * @param context the test context to use
     * @param receivers a list of {@link MessageReceiver} instances
     * @param count the number of expected messages
     * @throws Exception for any error
     */
    protected void receive(TestContext context, MessageReceiver[] receivers,
                           int count) throws Exception {
        for (int i = 0; i < receivers.length; ++i) {
            receive(context, receivers[i], count);
        }
    }

    /**
     * Helper to verify that a consumer receives the correct number of 
     * messages 
     *
     * @param receiver the message receiver
     * @param count the number of expected messages     
     * @return the list of messages received, or null, if none were expected
     * @throws Exception for any error
     */
    protected List receive(MessageReceiver receiver, int count) 
        throws Exception {
        return receive(getContext(), receiver, count);
    }

    /**
     * Helper to verify that a consumer receives the correct number of 
     * messages 
     *
     * @param context the test context to use
     * @param receiver the message receiver
     * @param count the number of expected messages     
     * @return the list of messages received, or null, if none were expected
     * @throws Exception for any error
     */
    protected List receive(TestContext context, MessageReceiver receiver, 
                           int count) throws Exception {
        String name = DestinationHelper.getName(receiver.getDestination());
        long timeout = context.getMessagingBehaviour().getTimeout();

        List result = receiver.receive(count, timeout);
        if (result == null) {
            if (count != 0) {
                String msg = "Failed to receive any messages from " +
                    "destination=" + name + ". Test context=" + context;
                fail(msg);
            }
        } else if (result.size() != count) {
            String msg = "Expected " + count + " messages from destination=" + 
                name + ", but got " + result.size() + ". Test context=" +
                context;
            fail(msg);
        }
        return result;
    }

    /**
     * Helper to send messages to each destination
     *
     * @param senders a list of {@link MessageSender} instances
     * @param message the message to send
     * @param count the number of messages to send
     * @throws Exception for any error
     */
    protected void send(MessageSender[] senders, Message message, int count)
        throws Exception {

        for (int i = 0; i < senders.length; ++i) {
            senders[i].send(message, count);
        }
    }

    /**
     * Helper to close a list of MessageReceivers
     * The references are removed from the list.
     *
     * @param receivers a list of {@link MessageReceiver} instances
     * @throws Exception for any error
     */
    protected void closeReceivers(MessageReceiver[] receivers) 
        throws Exception {
        TestContext context = getContext();
        for (int i = 0; i < receivers.length; ++i) {
            if (receivers[i] != null) {
                _log.debug(
                    "Closing receiver for destination=" +
                    DestinationHelper.getName(receivers[i].getDestination()));
                // invoke remove to destroy any durable topic subscriptions
                receivers[i].remove();
                receivers[i] = null;
            } 
        }
    }

    /**
     * Helper to close a list of MessageSenders.
     * The references are removed from the list.
     *
     * @param receivers a list of {@link MessageSender} instances
     * @throws Exception for any error
     */
    protected void closeSenders(MessageSender[] senders) throws Exception {
        TestContext context = getContext();
        for (int i = 0; i < senders.length; ++i) {
            if (senders[i] != null) {
                _log.debug(
                    "Closing sender for destination=" +
                    DestinationHelper.getName(senders[i].getDestination()));
                senders[i].close();
                senders[i] = null;
            }
        }
    }
          
} //-- SessionSendReceiveTestCase
