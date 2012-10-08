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
 * $Id: RepublishTest.java,v 1.3 2004/02/03 21:52:12 tanderson Exp $
 */
package org.exolab.jmscts.test.session.clientack;

import java.util.List;

import javax.jms.Message;

import org.apache.log4j.Logger;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessageSender;
import org.exolab.jmscts.core.TestCreator;
import org.exolab.jmscts.core.TestContext;


/**
 * This class tests the behaviour of republishing a received message, and then
 * acknowledging it.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.3 $
 * @see AbstractSendReceiveTestCase
 * @see org.exolab.jmscts.core.SendReceiveTestRunner
 * @jmscts.session CLIENT_ACKNOWLEDGE
 * @jmscts.message StreamMessage
 * @jmscts.delivery PERSISTENT, administered, synchronous
 * @jmscts.delivery PERSISTENT, administered, asynchronous
 * @jmscts.durableonly true
 */
public class RepublishTest extends AbstractSendReceiveTestCase {

    /**
     * The destination that the message listener receives messages on
     */
    private static final String DEST1 = "Republish1";

    /**
     * The destination that the message listeners send messages to
     */
    private static final String DEST2 = "Republish2";

    /**
     * The destinations to create prior to running the test
     */
    private static final String[] DESTINATIONS = {DEST1, DEST2};

    /**
     * The logger
     */
    @SuppressWarnings("unused")
    private static final Logger log =
        Logger.getLogger(RepublishTest.class);


    /**
     * Construct a new <code>RepublishTest</code>
     *
     * @param name the name of test case
     */
    public RepublishTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(RepublishTest.class);
    }

    /**
     * Returns the list of destination names used by this test case. These
     * are used to pre-create destinations prior to running the test case.
     *
     * @return the list of destinations used by this test case
     */
    @Override
    public String[] getDestinations() {
        return DESTINATIONS;
    }

    /**
     * Verify the behaviour of republishing a received message, and then
     * acknowledging it.
     *
     * @throws Exception for any error
     * @jmscts.requirement session.CLIENT_ACKNOWLEDGE
     */
    public void testRepublish() throws Exception {
        MessageReceiver receiver = null;
        MessageSender sender1 = null;
        MessageSender sender2 = null;
        TestContext context = getContext();
        Message message = context.getMessage();

        try {
            receiver = createReceiver(DEST1);
            sender1 = createSender(DEST1);
            sender2 = createSender(DEST2);

            // send the message to DEST1
            sender1.send(message, 1);

            // ... and receive it
            List<?> messages = receive(receiver, 1);
            Message received = (Message) messages.get(0);

            // republish the message to DEST2
            sender2.send(received, 1);

            // now acknowledge the received message, and verify it has been
            // acknowledged by ensuring that it cannot be re-received
            received.acknowledge();
            receiver = recreate(receiver);
            receive(receiver, 0);
        } finally {
            close(receiver);
            close(sender1);
            close(sender2);
        }
    }

}

