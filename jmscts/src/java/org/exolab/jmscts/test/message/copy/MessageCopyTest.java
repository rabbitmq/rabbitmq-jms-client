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
 * $Id: MessageCopyTest.java,v 1.8 2006/09/19 19:55:12 donh123 Exp $
 */
package org.exolab.jmscts.test.message.copy;

import java.util.LinkedList;
import java.util.List;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.StreamMessage;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.ChainedMessagePopulator;
import org.exolab.jmscts.core.MessageCreator;
import org.exolab.jmscts.core.MessageBodyComparer;
import org.exolab.jmscts.core.MessagePopulator;
import org.exolab.jmscts.core.MessagePropertyComparer;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessageSender;
import org.exolab.jmscts.core.MessageTypes;
import org.exolab.jmscts.core.SequenceMessagePopulator;
import org.exolab.jmscts.core.SequencePropertyPopulator;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests that message properties and message bodies are copied
 * on send and receive.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.8 $
 * @see AbstractSendReceiveTestCase
 * @see org.exolab.jmscts.core.SendReceiveTestRunner
 */
public class MessageCopyTest extends AbstractSendReceiveTestCase {

    /**
     * The destination used by this test case
     */
    private static final String DESTINATION = "MessageCopyTest";


    /**
     * Construct a new <code>MessageCopyTest</code>
     *
     * @param name the name of test case
     */
    public MessageCopyTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(MessageCopyTest.class);
    }

    /**
     * Get the message populator. This implementation always returns null
     *
     * @return null
     */
    @Override
    public MessagePopulator getMessagePopulator() {
        return null;
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
     * Verifies that messages are copied by the provider on send.
     *
     * @jmscts.requirement message.send
     * @throws Exception for any error
     */
    public void testCopyOnSend() throws Exception {
        final int count = 10;
        TestContext context = getContext();
        Session session = context.getSession();
        Message message = context.getMessage();
        List<Message> expected = new LinkedList<Message>();

        MessageReceiver receiver = createReceiver(DESTINATION);
        MessageSender sender = createSender(DESTINATION);

        MessagePopulator property = new SequencePropertyPopulator();
        MessagePopulator body = new SequenceMessagePopulator();
        MessagePopulator propertyDuplicator = new SequencePropertyPopulator();
        MessagePopulator bodyDuplicator = new SequenceMessagePopulator();

        // the message populator
        MessagePopulator populator = new ChainedMessagePopulator(
            new MessagePopulator[] {property, body});

        // the message populator to create duplicate copies
        MessagePopulator duplicator = new ChainedMessagePopulator(
            new MessagePopulator[] {propertyDuplicator, bodyDuplicator});

        List<?> messages = null;
        boolean appender = (message instanceof BytesMessage
                            || message instanceof StreamMessage);

        Class<?> type = MessageTypes.getType(message);
        try {
            for (int i = 0; i < count; ++i) {
                sender.send(message, 1, populator);
                MessageCreator creator;
                Message copy;
                if (appender) {
                    creator = new MessageCreator(session, propertyDuplicator);
                    copy = creator.create(type);
                    bodyDuplicator = new SequenceMessagePopulator();
                    for (int j = 0; j <= i; ++j) {
                        bodyDuplicator.populate(copy);
                    }
                } else {
                    creator = new MessageCreator(session, duplicator);
                    copy = creator.create(type);
                }
                expected.add(copy);
            }
            if (session.getTransacted() && !(session instanceof javax.jms.XASession)) {
                session.commit();
            }
            messages = receive(receiver, count);

            MessagePropertyComparer propertyComparer =
                new MessagePropertyComparer(true);
            MessageBodyComparer bodyComparer = new MessageBodyComparer();
            for (int i = 0; i < count; ++i) {
                Message sent = expected.get(i);
                Message received = (Message) messages.get(i);

                // need to reset BytesMessage and StreamMessage instances so
                // they can be read
                if (sent instanceof BytesMessage) {
                    ((BytesMessage) sent).reset();
                    ((BytesMessage) received).reset();
                } else if (sent instanceof StreamMessage) {
                    ((StreamMessage) sent).reset();
                    ((StreamMessage) received).reset();
                }

                if (!propertyComparer.compare(sent, received)) {
                    String msg = "Received message has different properties "
                        + "to that sent";
                    fail(msg);
                }
                if (!bodyComparer.compare(sent, received)) {
                    String msg = "Received message has different data "
                        + "to that sent";
                    fail(msg);
                }
            }
            acknowledge(messages);
        } finally {
            receiver.remove();
            sender.close();
        }
    }

}
