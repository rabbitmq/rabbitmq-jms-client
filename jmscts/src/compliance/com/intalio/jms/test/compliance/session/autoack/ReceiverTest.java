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
 * $Id: ReceiverTest.java,v 1.3 2003/05/04 14:12:39 tanderson Exp $
 */
package org.exolab.jmscts.test.session.autoack;

import javax.jms.Destination;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.AckTypes;
import org.exolab.jmscts.core.DeliveryTypes;
import org.exolab.jmscts.core.JMSTestRunner;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessageTypes;
import org.exolab.jmscts.core.MessagingBehaviour;
import org.exolab.jmscts.core.MessagingHelper;
import org.exolab.jmscts.core.SessionHelper;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests the behaviour of receivers on sessions created with the 
 * Session.AUTO_ACKNOWLEDGE message acknowledgment mode.</br>
 * NOTE: this test only works for durable subscribers, and queues
 *
 * @author <a href="tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.3 $
 * @see AbstractSendReceiveTestCase
 */
public class ReceiverTest extends AbstractSendReceiveTestCase {

    /**
     * Requirements covered by this test case
     */
    private static final String[][] REQUIREMENTS = {
        {"testReceiver", "session.AUTO_ACKNOWLEDGE"}};

    /**
     * The destination to create prior to running the test
     */
    private static final String DESTINATION = "ReceiverTest";

    /**
     * Construct an instance of this class for a specific test case.
     * The test will be run against the AUTO_ACKNOWLEGE acknowledgement type,
     * all synchronous topic subscribers and queue consumer messaging 
     * behaviours, and using StreamMessage messages
     *
     * @param name the name of test case
     */
    public ReceiverTest(String name) {
        super(name, MessageTypes.OBJECT, DeliveryTypes.ASYNCHRONOUS, 
              REQUIREMENTS);
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
        AckTypes types = new AckTypes(AckTypes.AUTO_ACKNOWLEDGE);
        return TestCreator.createSendReceiveTest(ReceiverTest.class, 
                                                 types);
    }

    /**
     * Returns the list of destination names used by this test case. These
     * are used to pre-administer destinations prior to running the test case.
     *
     * @return the list of destinations used by this test case
     */
    public String[] getDestinations() {
        return new String[]{DESTINATION};
    }

    /**
     * Returns true if this test case only supports durable topic subscribers.
     * If false, it supports both durable and non-durable topic subscribers.
     * Note that this does not prevent the test case from supporting queue
     * consumers or browsers.
     *
     * @return true
     */
    public boolean getDurableOnly() {
        return true;
    }

    /**
     * Tests auto acknowledgement functionality. Creates a receiver,
     * send n messages, receives them, and closes the receiver. Creates
     * another receiver and verifies that no messages can be received.
     * This covers requirements:
     * <ul>
     *   <li>session.AUTO_ACKNOWLEDGE
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testReceiver() throws Exception {
        final int count = 10; // send count messages
        TestContext context = getContext();
        Destination destination = getDestination(DESTINATION);
        MessagingBehaviour behaviour = context.getMessagingBehaviour();
        boolean durable = behaviour.getDurable();
        String name = (durable) ? SessionHelper.getSubscriberName() : null;

        MessageReceiver receiver = null;
        
        try {            
            // construct the synchronous receiver
            receiver = SessionHelper.createReceiver(
                context, destination, name, null, false);

            // send count messages
            MessagingHelper.send(context, destination, count);

            // ...and receive them
            MessagingHelper.receive(context, receiver, count);
            
            // close the receiver, re-create it, and verify that no messages
            // are received
            receiver.close();
            receiver = SessionHelper.createReceiver(
                context, destination, name, null, false);
            MessagingHelper.receive(context, receiver, 0);
        } finally {
            receiver.remove();
        }
    }
             
} //-- ReceiverTest
