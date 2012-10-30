package com.rabbitmq.jms.client;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.client.message.RMQBytesMessage;
import com.rabbitmq.jms.client.message.RMQMapMessage;
import com.rabbitmq.jms.client.message.RMQObjectMessage;
import com.rabbitmq.jms.client.message.RMQStreamMessage;
import com.rabbitmq.jms.client.message.RMQTextMessage;
import com.rabbitmq.jms.util.RMQJMSException;
import com.rabbitmq.jms.util.Util;

/**
 * RabbitMQ implementation of JMS {@link Session}
 */
public class RMQSession implements Session, QueueSession, TopicSession {

    /**
     * The connection that created this session
     */
    private final RMQConnection connection;
    /**
     * Set to true if this session is transacted.
     */
    private final boolean transacted;
    /**
     * The ack mode used when receiving message
     */
    private final int acknowledgeMode;
    /**
     * The RabbitMQ channel we use under the hood
     */
    private final Channel channel;
    /**
     * Set to true if close() has been called and completed
     */
    private volatile boolean closed = false;
    /**
     * The message listener for this session.
     */
    private volatile MessageListener messageListener;
    /**
     * A list of all the producers created by this session.
     * When a producer is closed, it will be removed from this list
     */
    private final ArrayList<RMQMessageProducer> producers = new ArrayList<RMQMessageProducer>();
    /**
     * A list of all the consumer created by this session.
     * When a consumer is closed, it will be removed from this list
     */
    private final ArrayList<RMQMessageConsumer> consumers = new ArrayList<RMQMessageConsumer>();
    /**
     * We keep an ordered set of the message tags (acknowledgement tags) for all messages received and unacknowledged.
     * Each message acknowledgement must ACK all (unacknowledged) messages received up to this point, and
     * we must never acknowledge a message more than once (nor acknowledge a message that doesn't exist).
     */
    private final SortedSet<Long> unackedMessageTags = Collections.synchronizedSortedSet(new TreeSet<Long>());

    /**
     * List of all our durable subscriptions so we can track them
     */
    private final Map<String, RMQMessageConsumer> subscriptions;

    private ConcurrentLinkedQueue<String> topics = new ConcurrentLinkedQueue<String>();

    private final Object closeLock = new Object();

    /**
     * Creates a session object associated with a connection
     * @param connection the connection that we will send data on
     * @param transacted whether this session is transacted or not
     * @param mode the default ACK mode
     * @param subscriptions the connection's subscriptions, shared with all sessions
     * @throws JMSException if we fail to create a {@link Channel} object on the connection
     */
    public RMQSession(RMQConnection connection, boolean transacted, int mode, Map<String, RMQMessageConsumer> subscriptions) throws JMSException {
        assert (mode >= 0 && mode <= 3);
        this.connection = connection;
        this.transacted = transacted;
        this.subscriptions = subscriptions;
        this.acknowledgeMode = transacted ? Session.SESSION_TRANSACTED : mode;
        try {
            this.channel = connection.getRabbitConnection().createChannel();
            if (transacted) {
                /* Make the channel (RabbitMQ) transactional: this cannot be undone */
                this.channel.txSelect();
            }
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BytesMessage createBytesMessage() throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        return new RMQBytesMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MapMessage createMapMessage() throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        return new RMQMapMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message createMessage() throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        return createTextMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectMessage createObjectMessage() throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        return new RMQObjectMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectMessage createObjectMessage(Serializable object) throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        ObjectMessage message = createObjectMessage();
        message.setObject(object);
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamMessage createStreamMessage() throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        return new RMQStreamMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextMessage createTextMessage() throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        return new RMQTextMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextMessage createTextMessage(String text) throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        TextMessage msg = createTextMessage();
        msg.setText(text);
        return msg;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getTransacted() throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        return getTransactedNoException();
    }

    /**
     * Same as {@link #getTransacted()}
     * but does not declare a JMSException in the throw clause
     * @return true if this session is transacted
     * @see  #getTransacted()
     */
    public boolean getTransactedNoException() {
        return this.transacted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAcknowledgeMode() throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        return getAcknowledgeModeNoException();
    }

    /**
     * Same as {@link RMQSession#getAcknowledgeMode()} but without
     * a declared exception in the throw clause
     * @return the acknowledge mode, one of {@link Session#AUTO_ACKNOWLEDGE},
     * {@link Session#CLIENT_ACKNOWLEDGE}, {@link Session#DUPS_OK_ACKNOWLEDGE} or
     * {@link Session#SESSION_TRANSACTED}
     */
    public int getAcknowledgeModeNoException() {
        return this.acknowledgeMode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        if (!this.transacted) throw new IllegalStateException("Session is not transacted");

        try {
            //call commit on the channel
            //this should ACK all messages
            //TODO: This does not ACK all messages (received) -- correct this?
            this.channel.txCommit();
        } catch (Exception x) {
            throw new RMQJMSException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rollback() throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        if (!this.transacted) throw new IllegalStateException("Session is not transacted");
        try {
            // rollback the RabbitMQ transaction
            this.channel.txRollback();
            // requeue all unacknowledged messages (not automatically done by RabbitMQ)
            this.channel.basicRecover();
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws JMSException {
        this.getConnection().sessionClose(this);
    }

     void internalClose() throws JMSException {
        if (this.closed) return;

        synchronized (this.closeLock) {
            try {
                //close all consumers created by this session
                for (RMQMessageConsumer consumer : this.consumers) {
                    try {
                        consumer.internalClose();
                    } catch (JMSException x) {
                        x.printStackTrace(); //TODO logging implementation
                    }
                }
                this.consumers.clear();

                //close all producers created by this session
                for (RMQMessageProducer producer : this.producers) {
                    producer.internalClose();
                }
                this.producers.clear();

                if (this.getTransacted()) {
                    this.rollback();
                }

                deleteTopicQueues(this.topics, this.channel);

                closeRabbitChannel(this.channel); //close the main channel

            } finally {
                this.closed = true;
            }
        }
    }

    private static void deleteTopicQueues(ConcurrentLinkedQueue<String> topics, Channel channel) {
        String topicQueue = null;
        while ((topicQueue = topics.poll()) != null) {
            try {
                channel.queueDelete(topicQueue);
                // TODO delete exchanges created for temporary topics
                // this.channel.exchangeDelete(, true)
            } catch (AlreadyClosedException x) {
                topics.clear();// nothing we can do but break out
            } catch (IOException iox) {
                // TODO log warn about not being able to delete a queue
                // created only for a topic
            }
        }
    }

    private static void closeRabbitChannel(Channel channel) throws JMSException {
        if (channel==null) return;
        try {
            channel.close();
        } catch (AlreadyClosedException x) {
            // nothing to do
        } catch (ShutdownSignalException x) {
            // nothing to do
        } catch (IOException x) {
            if (!(x.getCause() instanceof ShutdownSignalException)) {
                throw new RMQJMSException(x);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recover() throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        if (getTransactedNoException()) {
            throw new javax.jms.IllegalStateException("Session is transacted.");
        } else {
            synchronized (this.unackedMessageTags) {
                /* If we have messages to recover */
                if (!this.unackedMessageTags.isEmpty()) {
                    try {
                        this.channel.basicRecover(true);
                    }catch (IOException x) {
                        throw new RMQJMSException(x);
                    }
                    this.unackedMessageTags.clear();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageListener getMessageListener() throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        return this.messageListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMessageListener(MessageListener listener) throws JMSException {
        // not implemented
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        //this will not be implemented
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageProducer createProducer(Destination destination) throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        RMQDestination dest = (RMQDestination) destination;
        if (!dest.isDeclared()) {
            if (dest.isQueue()) {
                declareQueue(dest, null, false);
            } else {
                declareTopic(dest);
            }
        }
        RMQMessageProducer producer = new RMQMessageProducer(this, (RMQDestination) destination);
        this.producers.add(producer);
        return producer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageConsumer createConsumer(Destination destination) throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        return createConsumerInternal((RMQDestination) destination, null, false);
    }

    /**
     * Creates a consumer for a destination. If this is a topic, we can specify the autoDelete flag
     * @param dest internal destination object
     * @param uuidTag only used for topics, if null, one is generated as the queue name for this topic
     * @param durableSubscriber true if this is a durable topic subscription
     * @return {@link #createConsumer(Destination)}
     * @throws JMSException if destination is null or we fail to create the destination on the broker
     * @see #createConsumer(Destination)
     */
    private MessageConsumer createConsumerInternal(RMQDestination dest, String uuidTag, boolean durableSubscriber) throws JMSException {
        String consumerTag = uuidTag != null ? uuidTag : "jms-topic-"+Util.generateUUIDTag();

        if (!dest.isDeclared()) {
            if (dest.isQueue()) {
                declareQueue(dest, null, false);
            } else {
                declareTopic(dest);
            }
        }

        if (!dest.isQueue()) {
            String queueName = consumerTag;
            // this is a topic, we need to define a queue, and bind to it
            // the queue name is a unique ID for each consumer
            try {
                //we can set auto delete for a topic queue, since if the consumer disappears he is no longer
                //participating in the topic.
                this.declareQueue(dest, queueName, durableSubscriber);

                if (!durableSubscriber) {
                    //store the name of the queue we created for this consumer
                    //so that we can delete it when we close this session
                    this.topics.add(queueName);
                }
                //bind the queue to the exchange with the correct routing key
                this.channel.queueBind(queueName, dest.getExchangeInfo().name(), dest.getRoutingKey());
            } catch (IOException x) {
                throw new RMQJMSException(x);
            }
        }
        RMQMessageConsumer consumer = new RMQMessageConsumer(this, dest, consumerTag, getConnection().isStopped());
        this.consumers.add(consumer);
        return consumer;
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException - method not implemented until we support selectors
     */
    @Override
    public MessageConsumer createConsumer(Destination destination, String messageSelector) throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        if (messageSelector==null || messageSelector.trim().length()==0) {
            return createConsumer(destination);
        } else {
            // we are not implementing this method yet
            throw new UnsupportedOperationException();
        }
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException - method not implemented until we support selectors
     */
    @Override
    public MessageConsumer createConsumer(Destination destination, String messageSelector, boolean noLocal) throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        if (messageSelector==null || messageSelector.trim().length()==0) {
            RMQMessageConsumer consumer = (RMQMessageConsumer)createConsumer(destination);
            consumer.setNoLocal(noLocal);
            return consumer;
        } else {
            // we are not implementing this method yet
            throw new UnsupportedOperationException();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Queue createQueue(String queueName) throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        RMQDestination dest = new RMQDestination(queueName, true, false);
        declareQueue(dest, null, false);
        return dest;
    }

    /**
     * Invokes {@link Channel#queueDeclare(String, boolean, boolean, boolean, java.util.Map)} to define a queue on the RabbitMQ broker
     * this method invokes {@link RMQDestination#setDeclared(boolean)} with a true value
     * @param dest - the Queue object
     * @param temporary true if the queue is temporary
     * @throws JMSException if an IOException occurs in the {@link Channel#queueDeclare(String, boolean, boolean, boolean, java.util.Map)} call
     */
    private void declareQueue(RMQDestination dest, String queueNameOverride, boolean durableSubscriber) throws JMSException {
        try {
            String queueName = queueNameOverride!=null ? queueNameOverride : dest.getQueueName();

            /*
             * We only want destinations to survive server restarts if
             * 1. They are durable topic subscriptions OR
             * 2. They are permanent queues
             */
            boolean durable = durableSubscriber || (dest.isQueue() & (!dest.isTemporary()));

            /*
             * A queue is exclusive, meaning it can only be accessed by the current connection
             * and will be deleted when the connection is closed if
             * 1. It's a temporary destination OR
             * 2. It's a non durable topic
             */
            boolean exclusive = dest.isTemporary() || ((!dest.isQueue()) && (!durableSubscriber));

            HashMap<String,Object> options = null; //new HashMap<String,Object>();

            this.channel.queueDeclare(
            /* name of the queue */   queueName,
            /* temporary destinations are not durable will not survive a server restart all JMS queues except temp
             * survive restarts only durable topic queues */
                                      durable,
            /* exclusive flag */      exclusive,
            /* We don't set auto delete to true ever that is cause exclusive(rabbit)==true automatically get deleted
             * when a Connection is closed */
                                      false,
            /* Queue properties */    options);
            if (dest.isTemporary()) {
                this.topics.add(dest.getQueueName());
            }
        } catch (Exception x) {
            throw new RMQJMSException(x);
        }
        dest.setDeclared(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Topic createTopic(String topicName) throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        RMQDestination dest = new RMQDestination(topicName, false, false);
        declareTopic(dest);
        return dest;
    }

    /**
     * Declares the topic exchange (and queue) in RabbitMQ.
     * @param dest the topic destination
     * @throws JMSException
     */
    protected void declareTopic(RMQDestination dest) throws JMSException {
        if ("amq.topic".equals(dest.getExchangeInfo().name())) {
            /* built-in exchange -- do not redeclare */
        }
        else
            try {
                this.channel.exchangeDeclare(/* the name of the exchange */
                                             dest.getExchangeInfo().name(),
                                             /* the type of exchange to use */
                                             dest.getExchangeInfo().type(),
                                             /* durable for all except temporary topics */
                                             !dest.isTemporary(),
                                             // TODO: how do we delete exchanges used for temporary topics
                                             /* auto delete is true if temporary -
                                              * this could be autoDelete=dest.isTemporary()
                                              */
                                             false,
                                             /* internal is false JMS will always publish to the exchange*/
                                             false,
                                             /* object parameters */
                                             null);
            } catch (IOException x) {
                throw new RMQJMSException(x);
            }
        dest.setDeclared(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name) throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        RMQDestination topicDest = (RMQDestination) topic;
        RMQMessageConsumer previousConsumer = this.subscriptions.get(name);
        if (previousConsumer!=null) {
            /*
             * we are changing subscription, or not, if called with the same topic
             */

            if (previousConsumer.getDestination().equals(topicDest)) {
                if (previousConsumer.isClosed()) {
                    /*
                     * They called TopicSubscriber.close but didn't unsubscribe
                     * and they are simply resubscribing with a new one
                     */
                } else {
                    throw new JMSException("Subscription with name["+name+"] and topic["+topicDest+"] already exists");
                }
            } else {
                //change in subscription
                unsubscribe(name);
            }
        }

        /*
         * Create the new subscription
         */
        RMQMessageConsumer result = (RMQMessageConsumer)createConsumerInternal(topicDest, name, true);
        result.setDurable(true);
        this.subscriptions.put(name, result);
        return result;
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException selectors not yet implemented
     */
    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name, String messageSelector, boolean noLocal) throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        if (messageSelector==null || messageSelector.trim().length()==0) {
            RMQMessageConsumer consumer = (RMQMessageConsumer) createDurableSubscriber(topic, name);
            consumer.setNoLocal(noLocal);
            return consumer;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * {@inheritDoc}
     * TODO Implement browser support when RabbitMQ broker supports it
     */
    @Override
    public QueueBrowser createBrowser(Queue queue) throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueBrowser createBrowser(Queue queue, String messageSelector) throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporaryQueue createTemporaryQueue() throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        RMQDestination result = new RMQDestination("jms-temp-queue-"+Util.generateUUIDTag(), true, true);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporaryTopic createTemporaryTopic() throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        RMQDestination result = new RMQDestination("jms-temp-topic-"+Util.generateUUIDTag(), false, true);
        return result;
    }

    /**
     * This is only available for topic subscriptions.
     * {@inheritDoc}
     */
    @Override
    public void unsubscribe(String name) throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        try {
            if (name != null && this.subscriptions.remove(name) != null) {
                // remove the queue
                this.channel.queueDelete(name);
            }
        } catch (IOException x) {
            throw new RMQJMSException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueReceiver createReceiver(Queue queue) throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        return (QueueReceiver) this.createConsumer(queue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueReceiver createReceiver(Queue queue, String messageSelector) throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        return (QueueReceiver) this.createConsumer(queue, messageSelector);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueSender createSender(Queue queue) throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        return (QueueSender) this.createProducer(queue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicSubscriber createSubscriber(Topic topic) throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        return (TopicSubscriber) this.createConsumer(topic);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicSubscriber createSubscriber(Topic topic, String messageSelector, boolean noLocal) throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        if (messageSelector==null || messageSelector.trim().length()==0) {
            RMQMessageConsumer consumer = (RMQMessageConsumer)createSubscriber(topic);
            consumer.setNoLocal(noLocal);
            return consumer;
        } else {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicPublisher createPublisher(Topic topic) throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        return (TopicPublisher) this.createProducer(topic);
    }

    /**
     * Returns the connection this session is associated with
     * @return
     */
    public RMQConnection getConnection() {
        return this.connection;
    }

    /**
     * Returns the {@link Channel} this session has created
     * @return
     */
    public Channel getChannel() {
        return this.channel;
    }

    public void consumerClose(RMQMessageConsumer consumer) throws JMSException {
        if (this.consumers.remove(consumer)) {
            //TODO: if (consumer.isDurable()) { don't cancel it? cancel it? -- decide }
            consumer.internalClose();
        }
    }

    public void removeProducer(RMQMessageProducer producer) {
        if (this.producers.remove(producer)) {
            producer.internalClose();
        }
    }

    public boolean isAutoAck() {
        return (getAcknowledgeModeNoException()==Session.AUTO_ACKNOWLEDGE) || (getAcknowledgeModeNoException()==Session.DUPS_OK_ACKNOWLEDGE);
    }

    /**
     * Stops all consumers from receiving messages. This is called by the
     * session indirectly after {@link javax.jms.Connection#stop()} has been
     * invoked. In this implementation, any async consumers will be cancelled,
     * only to be re-subscribed when
     *
     * @throws javax.jms.JMSException if the thread is interrupted
     */
    public void pause() throws JMSException {
        for (RMQMessageConsumer consumer : this.consumers) {
            try {
                consumer.pause();
            } catch (InterruptedException x) {
                throw new RMQJMSException(x);
            }
        }
    }

    /**
     * Resubscribes all async listeners and continues to receive messages
     *
     * @see javax.jms.Connection#stop()
     * @throws javax.jms.JMSException if the thread is interrupted
     */
    public void resume() throws JMSException {
        for (RMQMessageConsumer consumer : this.consumers) {
            try {
                consumer.resume();
            } catch (IllegalStateException x) {
                throw new RMQJMSException(x);
            }
        }
    }

    void unackedMessageReceived(RMQMessage message) {
        if (!getTransactedNoException()) {
            synchronized (this.unackedMessageTags) {
                this.unackedMessageTags.add(message.getRabbitDeliveryTag());
            }
        }
    }

    /**
     * Acknowledges sessions messages.
     * Invoked when the method
     * {@link javax.jms.Message#acknowledge()}
     * @param message - the message to be acknowledged
     */
    void acknowledgeMessage(RMQMessage message) throws JMSException {
        if (this.closed) throw new IllegalStateException("Session is closed");
        boolean groupAck      = false; // TODO: future functionality, allow group ack prior to the current tag
        boolean individualAck = false; // TODO: future functionality, allow ack of single message
        if (!isAutoAck() && !getTransacted() && !this.unackedMessageTags.isEmpty()) {
            long messageTag = message.getRabbitDeliveryTag();
            /*
             * Per JMS specification Message.acknowledge(), if we ack
             * the last message in a group, we will ack all the ones prior received.
             * Spec 11.2.21 says:
             * "Note that the acknowledge method of Message acknowledges all messages
             * received on that message's session."
             */
            synchronized (this.unackedMessageTags) {
                /*
                 * Make sure that the message is in our unack list
                 * or we can't proceed
                 * if it is not in the list, then the message is either
                 * auto acked or been manually acked previously
                 */
                try {
                    if (individualAck) {
                        if (!this.unackedMessageTags.contains(messageTag)) return; // nothing to do
                        /* ACK a single message */
                        getChannel().basicAck(messageTag, // we ack the tag
                                              false);     // and only that tag
                        /* remove the message just acked from our list of un-acked messages */
                        this.unackedMessageTags.remove(messageTag);

                    } else if (groupAck) {
                        SortedSet<Long> previousTags = this.unackedMessageTags.headSet(messageTag+1);
                        if (previousTags.isEmpty()) return; // nothing to do
                        /* ack multiple message up until the existing tag */
                        getChannel().basicAck(previousTags.last(), // we ack the latest one
                                              true);               // and everything prior to that
                        // now remove all the tags <= messageTag
                        previousTags.clear();
                    } else {
                        getChannel().basicAck(this.unackedMessageTags.last(), // we ack the highest tag
                                              true);                          // and everything prior to that
                        this.unackedMessageTags.clear();
                    }
                } catch (IOException x) {
                    throw new RMQJMSException(x);
                }
            }
        }
    }
}
