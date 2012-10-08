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
 * $Id: ClientAcknowledgeTestCase.java,v 1.3 2003/05/04 14:12:39 tanderson Exp $
 */
package org.exolab.jmscts.test.session.clientack;

import java.util.Iterator;
import java.util.List;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.log4j.Logger;

import junit.framework.Test;

import org.exolab.jmscts.core.AckTypes;
import org.exolab.jmscts.core.DeliveryTypes;
import org.exolab.jmscts.core.DestinationHelper;
import org.exolab.jmscts.core.JMSTestRunner;
import org.exolab.jmscts.core.MessagePopulator;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessageSender;
import org.exolab.jmscts.core.MessageTypes;
import org.exolab.jmscts.core.SequencePropertyPopulator;
import org.exolab.jmscts.core.SessionHelper;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.test.session.SessionSendReceiveTestCase;


/**
 * This class provides helper methods for CLIENT_ACKNOWLEDGE test cases
 *
 * @author <a href="tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.3 $
 * @see SessionSendReceiveTestCase
 */
abstract class ClientAcknowledgeTestCase extends SessionSendReceiveTestCase {

    /**
     * The logger
     */
    private static final Logger _log = 
        Logger.getLogger(ClientAcknowledgeTestCase.class.getName());


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
    public ClientAcknowledgeTestCase(String name, MessageTypes types,
                                     DeliveryTypes delivery, 
                                     String[][] requirements) {
        super(name, types, delivery, requirements);
    }

    /**
     * Helper to send messages to each destination, setting the JMSXGroupID 
     * and JMSXGroupSeq properties for each message
     *
     * @param count the number of messages to send
     * @throws Exception for any error
     */
    protected void send(int count) throws Exception {
        send(getContext(), count);
    }

    /**
     * Helper to send messages to each destination, setting the JMSXGroupID 
     * and JMSXGroupSeq properties for each message
     *
     * @param context the test context to use
     * @param count the number of messages to send
     * @throws Exception for any error
     */
    protected void send(TestContext context, int count) throws Exception {
        Message message = context.getMessage();

        String[] destinations = getDestinations();
        for (int i = 0; i < destinations.length; ++i) {
            Destination destination = getDestination(destinations[i]);
            _log.debug("Creating sender for destination=" + destination);
            MessageSender sender = SessionHelper.createSender(
                context, destination);

            // use the destination name as the group name
            String group = DestinationHelper.getName(destination);
            MessagePopulator populator = new SequencePropertyPopulator(group);

            // send count messages, setting the JMSXGroupID and 
            // JMSXGroupSeq properties for each message
            sender.send(message, count, populator);
            sender.close();
        }
    }

    /**
     * Helper to receive messages from a list of receivers and verify they
     * are in the correct sequence.
     *
     * @param receivers the list of receivers to receive messages with.
     * @param count the number of expected messages to receive
     * @param sequence the starting sequence
     * @param redelivered the expected value of JMSRedelivered
     * @return the last message returned by the session
     * @throws Exception for any error
     */
    protected Message receive(MessageReceiver[] receivers, int count, 
                              int sequence, boolean redelivered) 
        throws Exception {
        return receive(getContext(), receivers, count, sequence, redelivered);
    }

    /**
     * Helper to receive messages from a list of receivers and verify they
     * are in the correct sequence.
     *
     * @param context the test context to use
     * @param receivers the list of receivers to receive messages with.
     * @param count the number of expected messages to receive
     * @param sequence the starting sequence
     * @param redelivered the expected value of JMSRedelivered
     * @return the last message returned by the session
     * @throws Exception for any error
     */
    protected Message receive(TestContext context, MessageReceiver[] receivers,
                              int count, int sequence, boolean redelivered) 
        throws Exception {
        
        Message last = null;
        for (int i = 0; i < receivers.length; ++i) {
            List messages = receive(context, receivers[i], count);
            
            // verify that each message has the correct properties
            String name = DestinationHelper.getName(
                receivers[i].getDestination());
            checkProperties(messages, name, sequence, redelivered);

            last = (Message) messages.get(count - 1);
        }
        return last;
    }

    /**
     * Verifies that the JMSXGroupID and JMXSGroupSeq properties are set
     * and that the messages are in sequence
     *
     * @param messages the list of messages to verify
     * @param group the expected value of JMSXGroupID
     * @param sequence the starting sequence number
     * @param redelivered the expected value of JMSRedelivered
     * @throws Exception for any error
     */
    protected void checkProperties(List messages, String group, int sequence,
                                   boolean redelivered) throws Exception {
        String groupProperty = SequencePropertyPopulator.GROUP_ID;
        String seqProperty = SequencePropertyPopulator.GROUP_SEQ;
        Iterator iter = messages.iterator();    
        while (iter.hasNext()) {
            Message message = (Message) iter.next();
            String groupValue = message.getStringProperty(groupProperty);
            if (!group.equals(groupValue)) {
                String msg = "Expected " + groupProperty + "=" + group + 
                    " but got " + groupValue;
                fail(msg);
            }
            int seqValue = message.getIntProperty(seqProperty);
            if (sequence != seqValue) {
                String msg = "Expected " + seqProperty + "=" + sequence + 
                    " but got " + seqValue;
                fail(msg);
            }
            ++sequence;
            if (message.getJMSRedelivered() != redelivered) {
                fail("Expected message to have JMSRedelivered=" + 
                     redelivered + ", but got JMSRedelivered=" + 
                     message.getJMSRedelivered());
            }
        }
    }
            
} //-- ClientAcknowledgeTestCase
