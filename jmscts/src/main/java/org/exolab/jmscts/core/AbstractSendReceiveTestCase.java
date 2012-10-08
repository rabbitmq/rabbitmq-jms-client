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
 * $Id: AbstractSendReceiveTestCase.java,v 1.10 2005/06/16 04:03:39 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.util.List;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;

import org.apache.log4j.Logger;


/**
 * This class provides a default implementation of the
 * {@link SendReceiveTestCase} interface.
 *
 * @version     $Revision: 1.10 $ $Date: 2005/06/16 04:03:39 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         SendReceiveTestCase
 * @see         SendReceiveTestRunner
 */
public abstract class AbstractSendReceiveTestCase
    extends AbstractMessageTestCase implements SendReceiveTestCase {

    /**
     * The destinations to test against. This is a map of Destination
     * instances keyed by name.
     */
    private Map<?, ?> _destinations = null;

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(AbstractSendReceiveTestCase.class);


    /**
     * Construct an instance of this class for a specific test case
     *
     * @param name the name of test case
     */
    public AbstractSendReceiveTestCase(String name) {
        super(name);
    }

    /**
     * Returns the list of destination names used by this test case. These
     * are used to pre-create destinations prior to running the test case.
     *
     * @return this implementation returns <code>null</code>.
     */
    @Override
    public String[] getDestinations() {
        return null;
    }

    /**
     * Sets the pre-created destinations corresponding to those returned by
     * {@link #getDestinations}.
     *
     * @param destinations a map of Destination instances keyed on their name
     */
    @Override
    public void setDestinations(Map<?, ?> destinations) {
        _destinations = destinations;
    }

    /**
     * Returns the named destination
     *
     * @param name the destination name
     * @return the destination, or null if none exists with the specified name
     */
    public Destination getDestination(String name) {
        return (Destination) _destinations.get(name);
    }

    /**
     * Return the delivery types to run this test case against
     *
     * This implementation returns the values of any
     * <code>jmscts.delivery</code> javadoc tags associated with the test
     * case.
     * The none are specified, returns {@link DeliveryTypes#ALL}
     *
     * @return the delivery types to run this test case against
     */
    @Override
    public DeliveryTypes getDeliveryTypes() {
        DeliveryTypes result = null;
        String test = getName();
        String[] types = AttributeHelper.getAttributes(
            getClass(), test, "jmscts.delivery", false);

        if (types.length == 0) {
            result = DeliveryTypes.ALL;
        } else {
            result = DeliveryTypes.fromString(types);
        }
        return result;
    }

    /**
     * Determines if this test case only supports durable topic subscribers.
     * If false, it supports both durable and non-durable topic subscribers.
     * Note that this does not prevent the test case from supporting queue
     * consumers or browsers.
     *
     * This implementation returns the values of any
     * <code>jmscts.durableonly</code> javadoc tags associated with the test
     * case.
     * The none are specified, returns <code>false</code>
     *
     * @return <code>true</code> if this test only supports durable topic
     * subcribers; otherwise <code>false</code>
     */
    @Override
    public boolean getDurableOnly() {
        String test = getName();
        return AttributeHelper.getBoolean(getClass(), test,
                                          "jmscts.durableonly", false);
    }

    /**
     * Helper to send a message
     *
     * @param message the message to send
     * @param destination the destination
     * @throws Exception if the message cannot be sent.
     */
    protected void send(Message message, Destination destination)
        throws Exception {
        send(getContext(), message, destination, 1);
    }

    /**
     * Helper to send a message
     *
     * @param message the message to send
     * @param name the name of the destination
     * @throws Exception if the message cannot be sent.
     */
    protected void send(Message message, String name) throws Exception {
        Destination destination = getDestination(name);
        if (destination == null) {
            throw new Exception("No destination associated with name="
                                + name + " exists");
        }
        send(message, destination);
    }

    /**
     * Helper to send messages to a destination
     *
     * @param destination the destination
     * @param count the no. of messages to send
     * @throws Exception if messages cannot be sent.
     */
    protected void send(Destination destination, int count) throws Exception {
        send(getContext(), destination, count);
    }

    /**
     * Helper to send messages to a destination
     *
     * @param name the destination name. This must correspond to a destination
     * returned by {@link #getDestinations}.
     * @param count the no. of messages to send
     * @throws Exception if messages cannot be sent.
     */
    protected void send(String name, int count) throws Exception {
        Destination destination = getDestination(name);
        if (destination == null) {
            throw new Exception("No destination associated with name="
                                + name + " exists");
        }
        send(destination, count);
    }

    /**
     * Helper to send messages to a destination
     *
     * @param context the context to use
     * @param name the destination name. This must correspond to a destination
     * returned by {@link #getDestinations}.
     * @param count the no. of messages to send
     * @throws Exception if messages cannot be sent.
     */
    protected void send(TestContext context, String name, int count)
        throws Exception {
        Destination destination = getDestination(name);
        if (destination == null) {
            throw new Exception("No destination associated with name="
                                + name + " exists");
        }
        send(context, destination, count);
    }

    /**
     * Helper to send messages to a destination
     *
     * @param context the context to use
     * @param destination the destination
     * @param count the no. of messages to send
     * @throws Exception if messages cannot be sent.
     */
    protected void send(TestContext context, Destination destination,
                        int count)
        throws Exception {
        send(context, context.getMessage(), destination, count);
    }

    /**
     * Helper to send messages to a destination
     *
     * @param context the context to use
     * @param message the message to send
     * @param destination the destination
     * @param count the no. of messages to send
     * @throws Exception if messages cannot be sent.
     */
    protected void send(TestContext context, Message message,
                        Destination destination, int count)
        throws Exception {
        MessagingHelper.send(context, message, destination, count);
    }

    /**
     * Helper to send a message to a destination, and return a message from
     * the same destination.
     * <p/>
     * Note: the persistent state of the receiver is removed on completion,
     * so tests for durable topic subcribers should not use this method if
     * more than one message is expected.
     *
     * @param message the message to send
     * @param destination the destination
     * @return Message the message received from the destination
     * @throws Exception if the message cannot be sent, no message is received,
     * or more than one message is received.
     */
    protected Message sendReceive(Message message, Destination destination)
        throws Exception {

        return MessagingHelper.sendReceive(getContext(), message, destination);
    }

    /**
     * Helper to send a message to a destination, and return a message from
     * the same destination
     *
     * @param message the message to send
     * @param name the destination name. This must correspond to a destination
     * returned by {@link #getDestinations}.
     * @return Message the message received from the destination
     * @throws Exception if the message cannot be sent, no message is received,
     * or more than one message is received.
     */
    protected Message sendReceive(Message message, String name)
        throws Exception {

        Destination destination = getDestination(name);
        if (destination == null) {
            throw new Exception("No destination associated with name="
                                + name + " exists");
        }
        return sendReceive(message, destination);
    }


    /**
     * Helper to create a MessageReceiver for a single destination returned
     * by {@link #getDestination}, using the default test context
     *
     * @param name the destination name
     * @return the receiver
     * @throws Exception for any error
     */
    protected MessageReceiver createReceiver(String name) throws Exception {
        return createReceiver(getContext(), name);
    }

    /**
     * Helper to create a MessageReceiver for a single destination returned
     * by {@link #getDestination}, using the specified test context
     *
     * @param context the test context to use
     * @param name the destination name
     * @return the receiver
     * @throws Exception for any error
     */
    protected MessageReceiver createReceiver(TestContext context, String name)
        throws Exception {

        Destination destination = getDestination(name);
        if (log.isDebugEnabled()) {
            log.debug("Creating receiver for destination="
                      + DestinationHelper.getName(destination));
        }
        return SessionHelper.createReceiver(context, destination);
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
            receivers[i] = createReceiver(context, destinations[i]);
        }
        return receivers;
    }

    /**
     * Helper to recreate a MessageReceiver. This closes the receiver,
     * and recreates it to consume from the same destination, selector,
     * and for durable topic subscribers, subscription and no-local value
     *
     * @param receiver the receiver to re-create
     * @return the re-created receiver
     * @throws Exception for any error
     */
    protected MessageReceiver recreate(MessageReceiver receiver)
        throws Exception {

        Destination destination = receiver.getDestination();
        String name = receiver.getName();
        String selector = receiver.getSelector();
        boolean noLocal = receiver.getNoLocal();
        receiver.close();
        return SessionHelper.createReceiver(getContext(), destination, name,
                                            selector, noLocal);
    }

    /**
     * Helper to create a MessageSender for a single destination returned
     * by {@link #getDestination}, using the default test context
     *
     * @param name the destination name
     * @return the sender
     * @throws Exception for any error
     */
    protected MessageSender createSender(String name) throws Exception {
        return createSender(getContext(), name);
    }

    /**
     * Helper to create a MessageSender for a single destination returned
     * by {@link #getDestination}, using the specified test context
     *
     * @param context the test context to use
     * @param name the destination name
     * @return the sender
     * @throws Exception for any error
     */
    protected MessageSender createSender(TestContext context, String name)
        throws Exception {
        Destination destination = getDestination(name);
        if (log.isDebugEnabled()) {
            log.debug("Creating sender for destination="
                      + DestinationHelper.getName(destination));
        }
        return SessionHelper.createSender(context, destination);
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
            senders[i] = createSender(context, destinations[i]);
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
     * Helper to verify that a consumer receives a single message
     *
     * @param receiver the message receiver
     * @return the received message
     * @throws Exception for any error
     */
    protected Message receive(MessageReceiver receiver)
        throws Exception {
        return receive(getContext(), receiver);
    }

    /**
     * Helper to verify that a consumer receives a single message
     *
     * @param context the test context to use
     * @param receiver the message receiver
     * @return the received message
     * @throws Exception for any error
     */
    protected Message receive(TestContext context, MessageReceiver receiver)
        throws Exception {
        List<?> messages = receive(context, receiver, 1);
        return (Message) messages.get(0);
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
    protected List<?> receive(MessageReceiver receiver, int count)
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
    protected List<?> receive(TestContext context, MessageReceiver receiver,
                           int count) throws Exception {
        String name = DestinationHelper.getName(receiver.getDestination());
        long timeout = context.getMessagingBehaviour().getTimeout();

        List<?> result = receiver.receive(count, timeout);
        if (result == null) {
            if (count != 0) {
                String msg = "Failed to receive any messages from "
                    + "destination=" + name + ". Test context=" + context;
                fail(msg);
            }
        } else if (result.size() != count) {
            String msg = "Expected " + count + " messages from destination="
                + name + ", but got " + result.size() + ". Test context="
                + context;
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
    protected void close(MessageReceiver[] receivers) throws Exception {
        for (int i = 0; i < receivers.length; ++i) {
            close(receivers[i]);
            receivers[i] = null;
        }
    }

    /**
     * Helper to close a MessageReceiver
     *
     * @param receiver the receiver
     * @throws Exception for any error
     */
    protected void close(MessageReceiver receiver) throws Exception {
        if (receiver != null) {
            if (log.isDebugEnabled()) {
                Destination destination = receiver.getDestination();
                log.debug("Closing receiver for destination="
                          + DestinationHelper.getName(destination));
            }
            // invoke remove to destroy any durable topic subscription
            receiver.remove();
        }
    }

    /**
     * Helper to close a list of MessageSenders.
     * The references are removed from the list.
     *
     * @param senders a list of {@link MessageSender} instances
     * @throws Exception for any error
     */
    protected void close(MessageSender[] senders) throws Exception {
        for (int i = 0; i < senders.length; ++i) {
            close(senders[i]);
            senders[i] = null;
        }
    }

    /**
     * Helper to close a MessageSender
     *
     * @param sender the sender
     * @throws Exception for any error
     */
    protected void close(MessageSender sender) throws Exception {
        if (sender != null) {
            if (log.isDebugEnabled()) {
                Destination destination = sender.getDestination();
                log.debug("Closing sender for destination="
                          + DestinationHelper.getName(destination));
            }
            sender.close();
        }
    }

    /**
     * Helper to create a MessageConsumer for a single destination returned
     * by {@link #getDestination}, using the default test context
     *
     * @param name the destination name
     * @return the receiver
     * @throws Exception for any error
     */
    protected MessageConsumer createConsumer(String name) throws Exception {
        return createConsumer(getContext(), name);
    }

    /**
     * Helper to create a MessageConsumer for a single destination returned
     * by {@link #getDestination}, using the specified test context
     *
     * @param context the test context to use
     * @param name the destination name
     * @return the consumer
     * @throws Exception for any error
     */
    protected MessageConsumer createConsumer(TestContext context, String name)
        throws Exception {

        Destination destination = getDestination(name);
        String subscriber = null;
        if (context.getMessagingBehaviour().getDurable()) {
            subscriber = SessionHelper.getSubscriberName();
        }
        if (log.isDebugEnabled()) {
            log.debug("Creating consumer for destination="
                      + DestinationHelper.getName(destination)
                      + ", subscriber=" + subscriber);
        }
        return SessionHelper.createConsumer(context, destination, subscriber);
    }

    /**
     * Helper to create MessageConsumer instances for each destination returned
     * by {@link #getDestinations}, using the default test context
     *
     * @return a list of message consumers
     * @throws Exception for any error
     */
    protected MessageConsumer[] createConsumers() throws Exception {
        return createConsumers(getContext());
    }

    /**
     * Helper to create MessageConsumer instances for each destination returned
     * by {@link #getDestinations}
     *
     * @param context the test context to use
     * @return a list of message consumers
     * @throws Exception for any error
     */
    protected MessageConsumer[] createConsumers(TestContext context)
        throws Exception {
        String[] destinations = getDestinations();
        MessageConsumer[] consumers = new MessageConsumer[destinations.length];
        for (int i = 0; i < destinations.length; ++i) {
            consumers[i] = createConsumer(context, destinations[i]);
        }
        return consumers;
    }

    /**
     * Helper to acknowledge messages, if needed.
     *
     * @param messages the message to acknowledge
     * @throws Exception for any error
     */
    protected void acknowledge(List<?> messages) throws Exception {
        if (messages != null && !messages.isEmpty()) {
            Message last = (Message) messages.get(messages.size() - 1);
            acknowledge(last);
        }
    }
   
    /**
     * Helper to acknowledge a message, if needed.
     *
     * @param message the message to acknowledge
     * @throws Exception for any error
     */
    protected void acknowledge(Message message) throws Exception {
        if (message != null) {
            MessagingBehaviour behaviour
                = getContext().getMessagingBehaviour();
            ReceiptType type = behaviour.getReceiptType();
            if (!type.equals(ReceiptType.BROWSER)) {
                message.acknowledge();
            }
        }
    }

    /**
     * Helper to close a list of MessageConsumers.
     * The references are removed from the list.
     *
     * @param consumers the consumers to close
     * @throws Exception for any error
     */
    protected void close(MessageConsumer[] consumers) throws Exception {
        for (int i = 0; i < consumers.length; ++i) {
            consumers[i].close();
            consumers[i] = null;
        }
    }


}
