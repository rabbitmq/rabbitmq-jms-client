package com.rabbitmq.jms.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.util.Abortable;
import com.rabbitmq.jms.util.AbortedException;
import com.rabbitmq.jms.util.EntryExitManager;
import com.rabbitmq.jms.util.RMQJMSException;
import com.rabbitmq.jms.util.RuntimeWrapperException;
import com.rabbitmq.jms.util.Util;

public class RMQMessageConsumer implements MessageConsumer, QueueReceiver, TopicSubscriber {
    /**
     * The destination that this consumer belongs to
     */
    private final RMQDestination destination;
    /**
     * The session that this consumer was created under
     */
    private final RMQSession session;
    /**
     * Unique tag, used when creating AMQP queues for a consumer that thinks it's a topic
     */
    private final String uuidTag;
    /**
     * The async listener that we use to subscribe to Rabbit messages
     */
    private final AtomicReference<MessageListenerConsumer> listenerConsumer = new AtomicReference<MessageListenerConsumer>();
    /**
     * Entry and exit of receive() threads are controlled by this gate.
     * See {@link javax.jms.Connection#start()} and {@link javax.jms.Connection#stop()}
     */
    private final EntryExitManager entryExitManager = new EntryExitManager(true);
    /**
     * We track things that need to be aborted (for a Connection.close()). Typically these are waits.
     */
    private final AbortableHolder abortables = new AbortableHolder();
    /**
     * Is this consumer closed. this value should change to true, but never change back
     */
    private volatile boolean closed = false;
    /**
     * If this consumer is in the process of closing
     */
    private volatile boolean closing = false;
    /**
     * {@link MessageListener}, set by the user*
     */
    private volatile MessageListener messageListener;
    /**
     * Flag to check if we are a durable subscription
     */
    private volatile boolean durable = false;
    /**
     * Flag to check if we have noLocal set
     */
    private volatile boolean noLocal = false;

    /**
     * Creates a RMQMessageConsumer object. Internal constructor used by {@link RMQSession}
     * @param session - the session object that created this consume
     * @param destination - the destination for this consumer
     * @param uuidTag - when creating queues to a topic, we need a unique queue name for each consumer. This is the unique name.
     */
    public RMQMessageConsumer(RMQSession session, RMQDestination destination, String uuidTag, boolean paused) {
        this.session = session;
        this.destination = destination;
        this.uuidTag = uuidTag;
        if (!paused) this.entryExitManager.openGate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Queue getQueue() throws JMSException {
        return this.destination;
    }

    /**
     * {@inheritDoc}
     * Note: This implementation always returns null
     */
    @Override
    public String getMessageSelector() throws JMSException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageListener getMessageListener() throws JMSException {
        return this.messageListener;
    }

    /**
     * Remove the listener and dispose of any Rabbit Consumer that may be active and tracked.
     */
    private void removeMessageListener() {
        this.messageListener = null;
        MessageListenerConsumer listenerConsumer = this.listenerConsumer.getAndSet(null);
        if (listenerConsumer!=null) {
            this.abortables.remove(listenerConsumer);
            listenerConsumer.abort();
        }
    }

    /**
     * From the on-line JavaDoc:
     * <blockquote>
     * <p>Sets the message consumer's {@link MessageListener}.</p>
     * <p>
     * Setting the message listener to <code>null</code> is the equivalent of clearing the message listener for the
     * message consumer.
     * </p>
     * <p>
     * The effect of calling {@link #setMessageListener} while messages are being consumed by an existing
     * listener or the consumer is being used to consume messages synchronously is undefined.
     * </p>
     * </blockquote>
     * <p>
     * Notwithstanding, we attempt to clear the previous listener gracefully (by cancelling the Consumer) if there is one.
     * </p>
     * {@inheritDoc}
     */
    @Override
    public void setMessageListener(MessageListener listener) throws JMSException {
        try {
            if (listener != this.messageListener) {
                this.removeMessageListener();
            }
            if (listener != null) {
                MessageListenerConsumer mlConsumer =
                    new MessageListenerConsumer(this, getSession().getChannel(),
                                                TimeUnit.MILLISECONDS.toNanos(this.session.getConnection()
                                                                                          .getTerminationTimeout()));
                if (this.listenerConsumer.compareAndSet(null, mlConsumer)) {
                    this.abortables.add(mlConsumer);
                    if (!this.getSession().getConnection().isStopped()) {
                        mlConsumer.start();
                    }
                } else {
                    mlConsumer.abort();
                    throw new IllegalStateException("MessageListener concurrently set on Consumer " + this);
                }
            }
        } catch (RuntimeWrapperException rwe) {
            throw new RMQJMSException(rwe.getCause());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message receive() throws JMSException {
        return receive(Long.MAX_VALUE);
    }

    /**
     * {@inheritDoc}
     * @param timeout (in milliseconds) {@inheritDoc}
     */
    @Override
    public Message receive(long timeout) throws JMSException {
        if (this.closed || this.closing) throw new IllegalStateException("Consumer is closed or closing.");
        if (timeout == 0) timeout = Long.MAX_VALUE; // The spec identifies 0 as infinite timeout
        long now = System.currentTimeMillis();
        /* Try to receive a message synchronously */
        Message msg;
        try {
            msg = synchronousGet(timeout);
        } catch (AbortedException _) {
            /* If the get has been aborted waiting for the pause we return null, too. */
            return null;
        }

        if (msg != null) return msg; // We received a message - return it

        // Calculate the remaining timeout
        timeout = Math.max(timeout - (System.currentTimeMillis() - now), 0);
        if (timeout==0) return null; // We timed out. A timeout means we return null to the caller

        /* Currently there is no equivalent of receive(timeout) in RabbitMQ's Java API. So we emulate that behaviour by
         * creating a one-shot subscription. We use the object SynchronousConsumer - this object supports both timeout
         * and interrupts. */
        String consumerTag = null;
        SynchronousConsumer sc = null;
        try {
            /* Create the consumer object - in here we specify the timeout too */
            sc = new SynchronousConsumer(this.session.getChannel(), timeout, this.entryExitManager);

            this.abortables.add(sc);

            /* Wait for an message to arrive. This returns null if we timeout. */
            GetResponse response;
            try {
                /* Subscribe the consumer object */
                consumerTag = basicConsume(sc);
                response = sc.receive();
            } catch (AbortedException ae) {
                return null;
            }
            /* Process the result - even if it is null */
            RMQMessage message = (RMQMessage) processMessage(response, isAutoAck());
            /* This is WRONG -- Connection.stop() mustn't return to the caller if we are still processing this. sc must
             * take a completion latch, and close it. */
            if (message!=null) {
                /* If we reached here, means that the Connection.stop method was called before a message arrived. But we
                 * are not allowed to return this message cause we are in a stop/pause state {@link Connection#stop}. */
                timeout = Math.max(timeout - (System.currentTimeMillis() - now), 0);

                try {
                    if (this.entryExitManager.enter(timeout, TimeUnit.MILLISECONDS)) {
                        if (isAutoAck()) {
                            getSession().getChannel().basicAck(message.getRabbitDeliveryTag(), false);
                        }
                    } else {
                        getSession().getChannel().basicNack(message.getRabbitDeliveryTag(), false, true);
                        return null;
                    }
                } catch (AbortedException ax) {
                    // wait was aborted explicitly
                } catch (InterruptedException e) {
                    /* Reset the thread interrupted status */
                    Thread.currentThread().interrupt();
                } finally {
                    this.entryExitManager.exit();
                }

            }
            return message;
        } catch (IOException x) {
            throw new RMQJMSException(x);
        } finally {
            /* Don't attempt to cancel if we are either closing or closed */
            if (consumerTag!=null && sc!=null && this.closing==false && this.closed==false) {
                /* Cancel the subscription, if not already done. */
                sc.cancel(consumerTag);
            }
            this.abortables.remove(sc);
        }
    }

    /**
     * Returns true if messages should be auto acknowledged upon arrival
     * @return true if {@link Session#getAcknowledgeMode()}=={@link Session#DUPS_OK_ACKNOWLEDGE} or
     * {@link Session#getAcknowledgeMode()}=={@link Session#AUTO_ACKNOWLEDGE}
     */
    boolean isAutoAck() {
        int ackMode = getSession().getAcknowledgeModeNoException();
        return (ackMode==Session.DUPS_OK_ACKNOWLEDGE || ackMode==Session.AUTO_ACKNOWLEDGE);
    }

    /**
     * Register a {@link Consumer} with the Rabbit API to receive messages
     *
     * @param consumer the SynchronousConsumer being registered
     * @return the consumer tag created for this consumer
     * @throws IOException
     * @see Channel#basicConsume(String, boolean, String, boolean, boolean, java.util.Map, Consumer)
     */
    public String basicConsume(Consumer consumer) throws IOException {
        String name = null;
        if (this.destination.isQueue()) {
            /*
             * javax.jms.Queue we share a single AMQP queue among all consumers
             * hence the name will the the name of the destination
             */
            name = this.destination.getName();
        } else {
            /*
             * javax.jms.Topic we created a unique AMQP queue for each consumer
             * and that name is unique for this consumer alone
             */
            name = this.getUUIDTag();
        }
        //never ack async messages automatically, only when we can deliver them
        //to the actual consumer so we pass in false as the auto ack mode
        //we must support setMessageListener(null) while messages are arriving
        //and those message we NACK
        return getSession().getChannel().basicConsume(/* the name of the queue */
                                                      name,
                                                      /* autoack is ALWAYS false, otherwise we risk
                                                       * acking messages that are received to the client
                                                       * but the client listener(onMessage) has not yet been invoked */
                                                      false,
                                                      /* the consumer tag, a prefixed unique identifier */
                                                      "jms-consumer-"+Util.generateUUIDTag(), //the consumer tag
                                                      /* we do support the noLocal for subscriptions */
                                                      this.getNoLocalNoException(),
                                                      /* exclusive will always be false
                                                       * exclusive consumer access, meaning only this consumer can access the queue. */
                                                      false,
                                                      /* no custom arguments for the subscriber */
                                                      new HashMap<String,Object>(),
                                                      /* The callback object for handleDelivery() */
                                                      consumer);
    }

    /**
     * Cancels a {@link MessageListenerConsumer} on a channel, and waits for it to be completed (so {@link MessageListener#onMessage} will no longer be called).
     * @param consumer the consumer to be cancelled
     * @throws IOException
     * @see Channel#basicCancel(String)
     */
    private void basicCancel(MessageListenerConsumer consumer) throws IOException {
        String consumerTag = consumer.getConsumerTag();
        if (consumerTag!=null) {
            /*cancel a known subscription*/
            try {
                getSession().getChannel().basicCancel(consumerTag);
            } catch (AlreadyClosedException ace) {
                // TODO check if basicCancel really necessary in this case.
                if (!ace.isInitiatedByApplication()) {
                    throw ace;
                }
            }
            consumer.stop();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message receiveNoWait() throws JMSException {
        if (this.closed || this.closing) throw new IllegalStateException("Consumer is closed or closing.");
        try {
            return synchronousGet(0);
        } catch (AbortedException _) {
            return null;
        }
    }

    /**
     * The spec for {@link Connection#stop()} says:
     * <blockquote>
     * When the connection is stopped, delivery to all the connection's message consumers is inhibited: synchronous
     * receives block, and messages are not delivered to message listeners.
     * {@link Connection#stop()} blocks until receives and/or message listeners in progress have completed.
     * </blockquote>
     * <p>
     * For synchronous gets, we therefore have to potentially block on the way in.
     * </p>
     * @return a received message, or null if no message was received.
     * @throws JMSException
     */
    private Message synchronousGet(long timeout) throws JMSException, AbortedException {
        try {
            if (!this.entryExitManager.enter(timeout, TimeUnit.MILLISECONDS))
                return null; // timed out
            try {
                GetResponse response = null;
                try {
                    if (this.destination.isQueue()) {
                        /* For queue, issue a basic.get on the queue name */
                        response = this.getSession().getChannel().basicGet(this.destination.getQueueName(), this.isAutoAck());
                    } else {
                        /* For topic, issue a basic.get on the unique queue name for the consumer */
                        response = this.getSession().getChannel().basicGet(this.getUUIDTag(), this.isAutoAck());
                    }
                } catch (IOException x) {
                    throw new RMQJMSException(x);
                }
                /* convert the message (and remember tag if we need to) */
                return processMessage(response, this.isAutoAck());
            } finally {
                this.entryExitManager.exit();
            }
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * Converts a {@link GetResponse} to a {@link Message}
     * @param response
     * @return
     * @throws JMSException
     */
    RMQMessage processMessage(GetResponse response, boolean acknowledged) throws JMSException {
        try {
            if (response == null) {
                /*
                 * return null if the response is null
                 */
                return null;
            }
            /* Deserialize the message from the byte[] */
            RMQMessage message = RMQMessage.fromMessage(response.getBody());
            /*
             * Received messages contain a reference to their delivery tag
             * this is used in Message.acknowledge
             */
            message.setRabbitDeliveryTag(response.getEnvelope().getDeliveryTag());
            /*
             * Set a reference to this session
             * this is used in Message.acknowledge
             */
            message.setSession(getSession());
            /*
             * Set the destination this message was received from
             */
            message.setJMSDestination(getDestination());
            /*
             * Initially the message is
             * readOnly properties == true until clearProperties has been called
             * readOnly body == true until clearBody has been called
             */
            message.setReadonly(true);
            /*
             * Set the redelivered flag.
             * we inherit this from the RabbitMQ broker
             */
            message.setJMSRedelivered(response.getEnvelope().isRedeliver());
            if (!acknowledged) {
                /*
                 * If the message has not been acknowledged automatically
                 * let the session know so that it can track unacknowledged messages
                 */
                getSession().unackedMessageReceived(message);
            }
            try {
                /*
                 * If the Session.setMessageListener() has been
                 * set with a listener, we must invoke it at this time
                 */
                MessageListener listener = getSession().getMessageListener();
                if (listener != null) {
                    listener.onMessage(message);
                }
            } catch (JMSException x) {
                /*
                 * We can not propagate this exception
                 * but it should be logged
                 */
                x.printStackTrace(); //TODO logging implementation
            }
            return message;
        } catch (IOException x) {
            throw new RMQJMSException(x);
        } catch (ClassNotFoundException x) {
            throw new RMQJMSException(x);
        } catch (IllegalAccessException x) {
            throw new RMQJMSException(x);
        } catch (InstantiationException x) {
            throw new RMQJMSException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws JMSException {
        try {
            internalClose();
        } finally {
            getSession().consumerClose(this);
        }
    }

    /**
     * @return <code>true</code> if {@link #close()} has been invoked and the call has completed,
     * or <code>false</code> if the {@link #close()} has not been called or is in progress
     */
    public boolean isClosed() {
        return this.closed;
    }

    /**
     * Method called internally or by the Session
     * when system is shutting down
     */
    protected void internalClose() throws JMSException {
        /* let the system know we are in the process of closing */
        this.closing = true;
        /* If we are stopped, we must break that. This will release all threads waiting on the gate and effectively
         * disable the use of the gate */
        this.entryExitManager.finalOpenGate();

        /* cancel any subscription that we have active at this time. */
        setMessageListener(null);

        this.abortables.abort();

        this.closed = true;
        this.closing = false;
    }

    /**
     * Returns the destination this message consumer is registered with
     * @return the destination this message consumer is registered with
     */
    public RMQDestination getDestination() {
        return this.destination;
    }

    /**
     * Returns the session this consumer was created by
     * @return the session this consumer was created by
     */
    public RMQSession getSession() {
        return this.session;
    }

    /**
     * The unique tag that this consumer holds
     * @return unique tag that this consumer holds
     */
    public String getUUIDTag() {
        return this.uuidTag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Topic getTopic() throws JMSException {
        if (this.getDestination().isQueue()) {
            throw new JMSException("Destination is of type Queue, not Topic");
        }
        return this.getDestination();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getNoLocal() throws JMSException {
        return getNoLocalNoException();
    }

    /**
     * @see #getNoLocal()
     * @return true if the noLocal variable was set
     */
    public boolean getNoLocalNoException() {
        return this.noLocal;
    }

    /**
     * Stops this consumer from receiving messages.
     * This is called by the session indirectly when
     * {@link javax.jms.Connection#stop()} is invoked.
     * In this implementation, any async consumers will be cancelled, only to be re-subscribed when <code>resume()</code>d.
     * @throws InterruptedException if the thread is interrupted
     * @see #resume()
     */
    public void pause() throws InterruptedException {
        this.entryExitManager.closeGate();
        this.abortables.stop();
    }

    /**
     * Resubscribes all async listeners
     * and continues to receive messages
     * @see javax.jms.Connection#stop()
     * @throws javax.jms.JMSException if the thread is interrupted
     */
    public void resume() throws JMSException  {
        this.abortables.start();
        this.entryExitManager.openGate();
    }

    /**
     * @return true if durable
     */
    public boolean isDurable() {
        return this.durable;
    }

    /**
     * Set durable status
     * @param durable
     */
    protected void setDurable(boolean durable) {
        this.durable = durable;
    }

    /**
     * Configures the no local for this consumer
     * This is currently only used when subscribing an async consumer
     * @param noLocal
     */
    public void setNoLocal(boolean noLocal) {
        this.noLocal = noLocal;
    }

    /**
     * Bag of {@link Abortable}s which is itself an {@link Abortable}.
     */
    private static class AbortableHolder implements Abortable {
        private final ConcurrentLinkedQueue<Abortable> abortableQueue = new ConcurrentLinkedQueue<Abortable>();
        private final boolean[] flags = new boolean[]{false, false, false}; // to prevent infinite regress

        private enum Action {
            ABORT(0) { void doit(Abortable a) { a.abort(); } },
            START(1) { void doit(Abortable a) { a.start(); } },
            STOP(2)  { void doit(Abortable a) { a.stop();  } };
            private final int ind;
            Action(int ind) { this.ind = ind; }
            int index() { return this.ind; }
            abstract void doit(Abortable a);
            };

        public void add(Abortable a) {
            this.abortableQueue.add(a);
        }

        public void remove(Abortable a) {
            this.abortableQueue.remove(a);
        }

        public void abort() {
            act(Action.ABORT);
        }

        public void start() {
            act(Action.START);
        }

        public void stop() {
            act(Action.STOP);
        }

        private void act(Action action) {
            if (this.flags[action.index()]) return;
            this.flags[action.index()] = true;
            Abortable[] as = this.abortableQueue.toArray(new Abortable[this.abortableQueue.size()]);
            for (Abortable a : as) {
                action.doit(a);
            }
            this.flags[action.index()] = false;
        }
    }
}
