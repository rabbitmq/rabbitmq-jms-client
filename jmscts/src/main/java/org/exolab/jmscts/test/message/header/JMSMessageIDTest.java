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
 * Copyright 2004 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: JMSMessageIDTest.java,v 1.3 2006/09/19 19:55:12 donh123 Exp $
 */
package org.exolab.jmscts.test.message.header;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jms.Message;
import javax.jms.Session;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessageSender;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests the JMSMessageID message property
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.3 $
 * @see AbstractSendReceiveTestCase
 */
public class JMSMessageIDTest extends AbstractSendReceiveTestCase {

    /**
     * The destination name
     */
    private static final String DESTINATION = "JMSMessageIDTest";


    /**
     * Construct a new <code>JMSMessageIDTest</code>
     *
     * @param name the name of test case
     */
    public JMSMessageIDTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(JMSMessageIDTest.class);
    }

    /**
     * Returns the list of destination names used by this test case. These
     * are used to pre-administer destinations prior to running the test case.
     *
     * @return the list of destinations used by this test case
     */
    @Override
    public String[] getDestinations() {
        return new String[]{DESTINATION};
    }

    /**
     * Verifies that the JMSMessageID is assigned when a message is sent,
     * that the received message has the same JMSMessageID value,
     * as that sent, and that a new JMSMessageID is assigned when the
     * message is resent
     *
     * @jmscts.requirement message.identifier
     * @jmscts.delivery consumer
     * @throws Exception for any error
     */
    public void testJMSMessageID() throws Exception {
        TestContext context = getContext();
        Message message = context.getMessage();

//         assertNull("JMSMessageID should initially be null",
//                    message.getJMSMessageID());
//     Comment out for now. The spec doesn't state this, although its
//     a reasonable assumption IMHO.

        // send the message, and receive it from the same destination
        Message received1 = sendReceive(message, DESTINATION);
        String sentID1 = message.getJMSMessageID();
        String receivedID1 = received1.getJMSMessageID();
        assertNotNull("JMSMessageID after send should not be null", sentID1);
        assertNotNull("JMSMessageID on received message should not be null",
                      receivedID1);
        assertEquals(
            "JMSMessageID on received message not equal to that sent:",
            sentID1, receivedID1);

        // resend the message, and verify that a new JMSMessageID is allocated
        Message resent = received1;
        Message received2 = sendReceive(resent, DESTINATION);
        String sentID2 = resent.getJMSMessageID();
        String receivedID2 = received2.getJMSMessageID();
        assertNotNull("JMSMessageID after resend should not be null", sentID2);
        assertNotNull("JMSMessageID on received message should not be null",
                      receivedID2);
        if (sentID1.equals(sentID2)) {
            fail("A new JMSMessageID wasn't allocated for the resent message");
        }
        assertEquals(
            "JMSMessageID on received message not equal to that sent:",
            sentID2, receivedID2);
        acknowledge(received2);
    }

    /**
     * Verifies that the JMSMessageID is assigned each time a message is sent
     *
     * @jmscts.requirement message.identifier
     * @throws Exception for any error
     */
    public void testJMSMessageIDAssignment() throws Exception {
        final int count = 3;
        List<String> messageIds = new ArrayList<String>();
        TestContext context = getContext();
        Session session = context.getSession();
        Message message = context.getMessage();
        MessageSender sender = null;
        MessageReceiver receiver = null;
        context.getMessagingBehaviour().getTimeout();

        try {
            sender = createSender(DESTINATION);
            receiver = createReceiver(DESTINATION);

            for (int i = 0; i < count; ++i) {
                sender.send(message, 1);
                String messageId = message.getJMSMessageID();
                assertNotNull(messageId);
                if (messageIds.contains(messageId)) {
                    fail("Allocated JMSMessageID is not unique");
                }
                messageIds.add(messageId);
            }

            if (!(session instanceof javax.jms.XASession) && session.getTransacted()) {
                session.commit();
            }

            // now receive the messages, and verify they have the correct
            // identifiers. The messages have equal JMSPriority values,
            // so should be received in the same order as they were sent
            List<?> messages = receive(receiver, count);
            Iterator<?> iterator = messages.iterator();
            List<String> receivedIds = new ArrayList<String>();
            while (iterator.hasNext()) {
                Message received = (Message) iterator.next();
                receivedIds.add(received.getJMSMessageID());
            }

            if (!(session instanceof javax.jms.XASession) && session.getTransacted()) {
                session.commit();
            }
            assertEquals("JMSMessageIDs of received messages don't match "
                         + "those sent: ", messageIds, receivedIds);
            acknowledge(messages);
        } finally {
            close(sender);
            close(receiver);
        }
    }

    /**
     * Verifies that JMSMessageID is prefixed with 'ID:'
     *
     * @jmscts.requirement message.identifier.prefix
     * @jmscts.delivery all-send
     * @throws Exception for any error
     */
    public void testJMSMessageIDPrefix() throws Exception {
        final String prefix = "ID:";
        TestContext context = getContext();
        Message message = context.getMessage();

        // send the message
        send(message, DESTINATION);
        String messageID = message.getJMSMessageID();
        if (!messageID.startsWith(prefix)) {
            fail("JMSMessageID must start with prefix=" + prefix);
        }
    }

}
