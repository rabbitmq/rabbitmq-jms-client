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
 * $Id: RollbackClearTest.java,v 1.9 2004/02/03 21:52:09 tanderson Exp $
 */
package org.exolab.jmscts.test.message.clear;

import java.util.Iterator;
import java.util.List;

import javax.jms.Message;
import javax.jms.Session;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessageSender;
import org.exolab.jmscts.core.MessageVerifier;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;

import org.exolab.jmscts.test.message.util.MessagePopulatorVerifier;
import org.exolab.jmscts.test.message.util.MessagePropertyVerifier;
import org.exolab.jmscts.test.message.util.PopulatorVerifierFactory;


/**
 * This class tests the behaviour of <code>Message.clearBody()</code> and
 * <code>Message.clearProperties()</code> for transacted sessions where the
 * session is rolled back.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.9 $
 * @jmscts.session TRANSACTED
 * @jmscts.delivery all
 */
public class RollbackClearTest extends AbstractSendReceiveTestCase {

    /**
     * The destination used by this test case
     */
    private static final String DESTINATION = "RollbackClearTest";


    /**
     * Construct a new <code>RollbackClearTest</code>
     *
     * @param name the name of test case
     */
    public RollbackClearTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(RollbackClearTest.class);
    }

    /**
     * Returns the list of destination names used by this test case. These
     * are used to pre-create destinations prior to running the test case.
     *
     * @return the list of destinations used by this test case
     */
    @Override
    public String[] getDestinations() {
        return new String[] {DESTINATION};
    }

    /**
     * Verifies that clearing the properties and bodies of received messages
     * doesn't affect the messages received after rolling back the session
     *
     * @jmscts.requirement message.method.clearProperties
     * @jmscts.requirement message.method.clearBody
     * @throws Exception for any error
     */
    public void testRollback() throws Exception {
        final int count = 2;
        TestContext context = getContext();
        Message message = context.getMessage();
        MessagePopulatorVerifier properties = new MessagePropertyVerifier();
        MessagePopulatorVerifier body = PopulatorVerifierFactory.create(
            message);

        MessageReceiver receiver = createReceiver(DESTINATION);

        try {
            MessageSender sender = createSender(DESTINATION);
            for (int i = 0; i < count; ++i) {
                message.clearProperties();
                properties.populate(message);
                message.clearBody();
                body.populate(message);
                sender.send(message, 1);
            }

            // send the messages
            Session session = context.getSession();
            session.commit();

            // now receive and verify them
            List<?> messages = verify(receiver, properties, body, count);

            // clear the received messages
            Iterator<?> iter = messages.iterator();
            while (iter.hasNext()) {
                iter.next();
                message.clearProperties();
                message.clearBody();
            }

            // roll back the session, and re-receive and verify the messages
            // haven't changed
            session.rollback();
            verify(receiver, properties, body, count);
            session.commit();
        } finally {
            receiver.remove();
        }
    }

    /**
     * Receive messages, and verify them
     *
     * @param receiver the receiver
     * @param properties the message property verifier
     * @param body the message body verifier
     * @param count the no. of mesages to receive
     * @return the received messages
     * @throws Exception for any error
     */
    private List<?> verify(MessageReceiver receiver, MessageVerifier properties,
                        MessageVerifier body, int count) throws Exception {

        // receive the messages, and verify them
        List<?> messages = receive(receiver, count);
        Iterator<?> iter = messages.iterator();
        while (iter.hasNext()) {
            Message received = (Message) iter.next();
            properties.verify(received);
            body.verify(received);
        }
        return messages;
    }

}
