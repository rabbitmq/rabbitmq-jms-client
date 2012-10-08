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
 * $Id: ClientAcknowledgeTest.java,v 1.3 2003/05/04 14:12:39 tanderson Exp $
 */
package org.exolab.jmscts.test.session.clientack;

import java.util.List;

import javax.jms.Message;
import javax.jms.Session;

import org.apache.log4j.Logger;

import junit.framework.Test;

import org.exolab.jmscts.core.AckTypes;
import org.exolab.jmscts.core.DeliveryTypes;
import org.exolab.jmscts.core.DestinationHelper;
import org.exolab.jmscts.core.JMSTestRunner;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessageTypes;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests the behaviour of sessions created with the 
 * Session.CLIENT_ACKNOWLEDGE message acknowledgment mode.
 *
 * @author <a href="tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.3 $
 * @see SessionSendReceiveTestCase
 */
public class ClientAcknowledgeTest extends ClientAcknowledgeTestCase {

    /**
     * Requirements covered by this test case
     */
    private static final String[][] REQUIREMENTS = {
        {"testClientAcknowledge", "session.CLIENT_ACKNOWLEDGE", 
         "session.recover"},
        {"testPartialClientAcknowledge", "session.CLIENT_ACKNOWLEDGE", 
         "session.recover"},
        {"testRecover", "session.recover"}};

    /**
     * The destinations to create prior to running the test
     */
    private static final String[] DESTINATIONS = {"clientack1", "clientack2",
                                                  "clientack3"};

    /**
     * The logger
     */
    private static final Logger _log = 
        Logger.getLogger(ClientAcknowledgeTest.class.getName());


    /**
     * Construct an instance of this class for a specific test case.
     * The test will be run against the CLIENT_ACKNOWLEGE acknowledgement type,
     * all consumer messaging behaviours, and using TextMessage messages
     *
     * @param name the name of test case
     */
    public ClientAcknowledgeTest(String name) {
        super(name, MessageTypes.TEXT, DeliveryTypes.CONSUMER, REQUIREMENTS);
    }

    /**
     * The main line used to execute this test
     */
    public static void main(String[] args) {
        JMSTestRunner test = new JMSTestRunner(suite(), args);
        junit.textui.TestRunner.run(test);
    }

    /**
     * Sets up the test suite.
     */
    public static Test suite() {
        AckTypes types = new AckTypes(AckTypes.CLIENT_ACKNOWLEDGE);
        return TestCreator.createSendReceiveTest(ClientAcknowledgeTest.class, 
                                                 types);
    }

    /**
     * Returns the list of destination names used by this test case. These
     * are used to pre-administer destinations prior to running the test case.
     *
     * @return the list of destinations used by this test case
     */
    public String[] getDestinations() {
        return DESTINATIONS;
    }

    /**
     * Tests client acknowledgement functionality. For each destination,
     * send n messages, and then receive them. Acknowledge the last message
     * received, recover the session, and verify that no other messages are 
     * received. This covers requirements:
     * <ul>
     *   <li>session.CLIENT_ACKNOWLEDGE
     *   <li>session.recover
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testClientAcknowledge() throws Exception {
        final int count = 10; // send count messages to each destination

        TestContext context = getContext();
        Session session = context.getSession();

        // create the receivers prior to sending any messages
        MessageReceiver[] receivers = createReceivers();
        
        try {
            // send the messages, assigning the JMSXGroupSeq and JMSXGroupID 
            // properties to each message
            send(count);

            // acknowledge the last message received by the session, and
            // verify no other messages are received
            Message last = receive(receivers, count, 1, false);
            last.acknowledge();
            receive(receivers, 0);
            
            // recover the session and verify no messages are received
            session.recover();
            receive(receivers, 0);
        } finally {
            closeReceivers(receivers);
        }
    }

    /**
     * Tests client acknowledgement functionality. For each destination,
     * send n messages, and then receive them. Acknowledge the middle message
     * received for each destination, recover the session, and verify that
     * the messages can be received again. This covers requirements:
     * <ul>
     *   <li>session.CLIENT_ACKNOWLEDGE
     *   <li>session.recover
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testPartialClientAcknowledge() throws Exception {
        final int count = 10; // send count messages to each destination

        TestContext context = getContext();
        Session session = context.getSession();

        // create the receivers prior to sending any messages
        MessageReceiver[] receivers = createReceivers();
        
        try {
            // send the messages, assigning the JMSXGroupSeq and JMSXGroupID 
            // properties to each message
            send(count);

            // for each destination, receive all messages, verify they
            // have the correct sequence, and acknowledge the first half.
            // The session needs to be recovered each time to avoid 
            // acknowledging messages received from the other destinations.
            for (int i = 0; i < receivers.length; ++i) {
                List result = receive(receivers[i], count);

                // verify that each message has the correct properties
                String name = DestinationHelper.getName(
                    receivers[i].getDestination());
                checkProperties(result, name, 1, false);
                
                // Acknowledge the middle message. This should 
                // acknowledge all prior messages as well.
                Message received = (Message) result.get((count / 2) - 1);
                received.acknowledge();

                _log.debug("Recovering session to receive " +
                           "unacknowledged messages");
                session.recover();
            }

            // now receive the remaining messages, and acknowledge the last one
            int remaining = count - (count / 2);
            int sequence = count - remaining + 1;
            Message last = receive(receivers, remaining, sequence, true);
            last.acknowledge();

            // verify that no further messages are received
            receive(receivers, 0);
            session.recover();
            receive(receivers, 0);
        } catch (Exception exception) {
            throw exception;
        } finally {
            try {
                closeReceivers(receivers);
            } catch (Exception exception) {
                _log.debug(exception, exception);
            }
        }
    }

    /**
     * Tests session recovery. For each destination, send n messages,
     * and then receive them. Recover the session, and verify that the messages
     * can be received again. This covers requirements:
     * <ul>
     *   <li>session.recover
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testRecover() throws Exception {
        final int count = 10; // send count messages to each destination

        TestContext context = getContext();
        Session session = context.getSession();

        // create the receivers prior to sending any messages
        MessageReceiver[] receivers = createReceivers();

        try {
            send(count);
            receive(receivers, count, 1, false);
            session.recover();
            receive(receivers, count, 1, true);
        } catch (Exception exception) {
            throw exception;
        } finally {
            try {
                closeReceivers(receivers);
            } catch (Exception exception) {
                _log.error(exception, exception);
            }
        }
    }
             
} //-- ClientAcknowledgeTest
