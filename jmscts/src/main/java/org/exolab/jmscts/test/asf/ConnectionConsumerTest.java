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
 * $Id: ConnectionConsumerTest.java,v 1.2 2004/02/03 21:52:09 tanderson Exp $
 */
package org.exolab.jmscts.test.asf;

import javax.jms.ConnectionConsumer;
import javax.jms.Destination;

import org.apache.log4j.Logger;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.AckType;
import org.exolab.jmscts.core.ConnectionHelper;
import org.exolab.jmscts.core.CountingListener;
import org.exolab.jmscts.core.DestinationHelper;
import org.exolab.jmscts.core.MessagingBehaviour;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;
import org.exolab.jmscts.jms.asf.BasicServerSessionPool;


/**
 * This class tests the behaviour of <code>ConnectionConsumer</code>s
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.2 $
 * @jmscts.message TextMessage
 */
public class ConnectionConsumerTest extends AbstractSendReceiveTestCase {

    /**
     * The destination to create prior to running the test
     */
    private static final String DESTINATION = "ConnectionConsumerTest";

    /**
     * The logger
     */
    @SuppressWarnings("unused")
    private static final Logger log =
        Logger.getLogger(ConnectionConsumerTest.class.getName());

    /**
     * Create a new <code>ConnectionConsumerTest</code>.
     *
     * @param name the name of test case
     */
    public ConnectionConsumerTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(ConnectionConsumerTest.class);
    }

    /**
     * Returns the list of destination names used by this test case. These
     * are used to pre-create destinations prior to running the test case.
     *
     * @return the list of destinations used by this test case
     */
    @Override
    public String[] getDestinations() {
        return new String[]{DESTINATION};
    }

    /**
     * Verifies that a <code>ConnectionConsumer</code> can be created
     *
     * @jmscts.requirement asf.connectionconsumer
     * @throws Exception for any error
     */
    public void test() throws Exception {
        final int maxMessages = 1;
        final int threads = 1;
        final int count = 10;

        TestContext context = getContext();
        AckType type = context.getAckType();
        MessagingBehaviour behaviour = context.getMessagingBehaviour();
        Destination destination = getDestination(DESTINATION);

        CountingListener listener = new CountingListener(count);
        BasicServerSessionPool pool = new BasicServerSessionPool(
            threads, context.getConnection(), type.getTransacted(),
            type.getAcknowledgeMode(), listener);
        ConnectionConsumer consumer = null;

        try {
            consumer = ConnectionHelper.createConnectionConsumer(
                context, destination, null, null, pool, maxMessages);

            // send messages, and wait for the consumer to receive them
            send(destination, count);

            listener.waitForCompletion(count * behaviour.getTimeout());

            if (listener.getReceived() != count) {
                fail("Expected " + count + " messages from destination="
                     +  DestinationHelper.getName(destination) + " but got "
                     + listener.getReceived());
            }

        } finally {
            if (consumer != null) {
                consumer.close();
            }
            pool.close();
        }

    }

}
