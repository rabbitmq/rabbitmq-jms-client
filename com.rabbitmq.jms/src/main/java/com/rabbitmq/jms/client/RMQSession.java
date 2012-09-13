package com.rabbitmq.jms.client;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.client.message.RMQBytesMessage;
import com.rabbitmq.jms.client.message.RMQMapMessage;
import com.rabbitmq.jms.client.message.RMQObjectMessage;
import com.rabbitmq.jms.client.message.RMQStreamMessage;
import com.rabbitmq.jms.client.message.RMQTextMessage;
import com.rabbitmq.jms.util.CountUpAndDownLatch;
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
     * the value will enver change
     */
    private final boolean transacted;
    /**
     * The ack mode used when receiving message
     */
    private final int acknowledgeMode;
    /**
     * The RabbitMQ AMQP channel we use under the hood
     */
    private volatile Channel channel;
    /**
     * Set to true if close() has been called and completed
     */
    private volatile boolean closed = false;
    /**
     * We're tracking all our delivery tags for this session
     * and we are only storing the largest one.
     * this is used for nack during rollback of
     * a transacted session
     */
    private volatile AtomicLong lastReceivedTag = new AtomicLong(Long.MIN_VALUE);
    /**
     * The message listener for this session.
     */
    private volatile MessageListener messageListener;
    /**
     * A list of all the producers created by this session
     * When a producer is closed, it will be removed from this list
     */
    private final ArrayList<RMQMessageProducer> producers = new ArrayList<RMQMessageProducer>();
    /**
     * A list of all the consumer created by this session
     * When a consumer is closed, it will be removed from this list
     */
    private final ArrayList<RMQMessageConsumer> consumers = new ArrayList<RMQMessageConsumer>();
    /**
     * A latch that we use when listeners are running, that way we can
     * pause and wait for listeners to complete during close and Connection.stop
     */
    private final CountUpAndDownLatch runningListener = new CountUpAndDownLatch(0);
    /**
     * We are manually numbering our channels, this serves no purpose right now
     */
    private static final AtomicInteger channelNr = new AtomicInteger(0);
    /**
     * We are listing all messages we have received. This tree set is used to meet the
     * requirement of Message.acknowledge, to be able to acknowledge all the messages
     * for this session. we do this using a single ack, and we always use the
     * largest delivery tag to do so
     */
    private volatile TreeSet<Long> receivedMessages = new TreeSet<Long>();

    /**
     * List of all our durable subscriptions so we can track them
     */
    private final ConcurrentHashMap<String, RMQMessageConsumer> subscriptions;

    private ConcurrentLinkedQueue<String> topics = new ConcurrentLinkedQueue<String>();

    /**
     * Creates a session object associated with a connection
     * @param connection the connection that we will send data on
     * @param transacted whether this session is transacted or not
     * @param mode the default ack mode
     * @throws JMSException if we fail to create a {@link Channel} object on the connection
     */
    public RMQSession(RMQConnection connection, boolean transacted, int mode, ConcurrentHashMap<String, RMQMessageConsumer> subscriptions) throws JMSException {
        assert (mode >= 0 && mode <= 3);
        this.connection = connection;
        this.transacted = transacted;
        this.subscriptions = subscriptions;
        this.acknowledgeMode = transacted ? Session.SESSION_TRANSACTED : mode;
        try {
            /*
             * Create the channel that we will use, give it a known ID
             */
            this.channel = connection.getRabbitConnection().createChannel(channelNr.incrementAndGet());
            if (transacted) {
                /*
                 * txSelect is only called once, then the channel stays in transactional mode
                 * until closed
                 */
                this.channel.txSelect();
            }
        } catch (IOException x) {
            Util.util().handleException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BytesMessage createBytesMessage() throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        return new RMQBytesMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MapMessage createMapMessage() throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        return new RMQMapMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message createMessage() throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        return createTextMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectMessage createObjectMessage() throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        return new RMQObjectMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObjectMessage createObjectMessage(Serializable object) throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        ObjectMessage message = createObjectMessage();
        message.setObject(object);
        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamMessage createStreamMessage() throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        return new RMQStreamMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextMessage createTextMessage() throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        return new RMQTextMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextMessage createTextMessage(String text) throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        TextMessage msg = createTextMessage();
        msg.setText(text);
        return msg;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getTransacted() throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
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
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
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
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        Util.util().checkTrue(!this.transacted, "Session is not transacted", IllegalStateException.class);

        try {
            //call commit on the channel
            //this should ack all messages
            this.channel.txCommit();
            /*
             * Reset our NACK tag
             */
            lastReceivedTag.set(Long.MIN_VALUE);
        } catch (Exception x) {
            Util.util().handleException(x);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rollback() throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        Util.util().checkTrue(!this.transacted, "Session is not transacted", IllegalStateException.class);
        try {
            //call rollback
            this.channel.txRollback();
            /* Check to see if we have received a delivery tag */
            if (lastReceivedTag.get()!=Long.MIN_VALUE) {
                /*
                 * We only nack with the very last/largest delivery tag we got
                 * this will automatically nack and requeue all previous messages too
                 */
                channel.basicNack(/* the largest/last received tag*/
                                  lastReceivedTag.get(),
                                  /* true==nack multiple messages, all with tag<last */
                                  true,
                                  /* true==requeue messages that have been nacked */
                                  true);
                /*
                 * Reset the delivery tag
                 */
                lastReceivedTag.set(Long.MIN_VALUE);
            }
            /*
             * commit the NACK/Rejects
             * this I picked up from Spring-AMQP project, that's what they do
             */
            this.channel.txCommit();
        } catch (IOException x) {
            Util.util().handleException(x);
        }
    }

    /**
     * Returns true if {@link #close()} has been called and completed.
     * May return false if close is still in progress
     * @return
     */
    public boolean isClosed() {
        return this.closed;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void close() throws JMSException {
        if (this.closed)
            return;

        try {
            //close all producers created by this session
            for (RMQMessageProducer producer : producers) {
                producer.internalClose();
            }
            producers.clear();

            //close all consumers created by this session
            for (RMQMessageConsumer consumer : consumers) {
                try {
                    consumer.internalClose();
                }catch (JMSException x) {
                    x.printStackTrace(); //TODO logging implementation

                }
            }
            consumers.clear();

            this.setMessageListener(null);

            if (this.getTransacted()) {
                this.rollback();
            }

            String topicQueue = null;
            while ((topicQueue=topics.poll())!=null) {
                try {
                    this.channel.queueDelete(topicQueue);
                    //TODO delete exchanges created for temporary topics
                    //this.channel.exchangeDelete(, true)
                } catch (AlreadyClosedException x) {
                    topics.clear();//nothing we can do but break out
                } catch (IOException iox) {
                    //TODO log warn about not being able to delete a queue
                    //created only for a topic
                }
            }

            //close the channel itself
            try {
                if (channel.isOpen()) {
                    this.channel.close();
                }
            } catch (AlreadyClosedException x) {
                //nothing to do
            } catch (ShutdownSignalException x) {
                //nothing to do
            } catch (IOException x) {
                if (x.getCause() instanceof ShutdownSignalException) {
                    //nothing to do
                }
                Util.util().handleException(x);
            } finally {
            }
        } finally {
            //notify the connection that this session is closed
            //so that it can be removed from the list of sessions
            this.getConnection().sessionClose(this);
            this.closed = true;
        }
    }

    /**
     * {@inheritDoc}
     * TODO we can use basic.recover method call instead of basic.nack(requeue=true)
     */
    @Override
    public void recover() throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        if (getTransactedNoException()) {
            throw new javax.jms.IllegalStateException("Session is transacted.");
        } else {
            synchronized (receivedMessages) {
                /*
                 * First make sure that we have messages to recover
                 */
                if (receivedMessages.size()>0) {
                    /*
                     * get the largest tag
                     */
                    long deliveryTag = receivedMessages.last();
                    try {
                        /*
                         * nack the largest tag, this will
                         * nack and requeue the other messages too
                         */
                        this.channel.basicNack(deliveryTag, true, true);
                    }catch (IOException x) {
                        Util.util().handleException(x);
                    }
                    /*
                     * we must clear our received messages
                     */
                    receivedMessages.clear();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageListener getMessageListener() throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        return this.messageListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMessageListener(MessageListener listener) throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        if (listener==null) {
            this.messageListener = null;
        } else {
            this.messageListener = new MessageListenerWrapper(listener);
        }
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
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        RMQDestination dest = (RMQDestination) destination;
        if (!dest.isDeclared()) {
            if (dest.isQueue()) {
                declareQueue(dest, null, false);
            } else {
                declareTopic(dest);
            }
        }
        RMQMessageProducer producer = new RMQMessageProducer(this, (RMQDestination) destination);
        producers.add(producer);
        return producer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MessageConsumer createConsumer(Destination destination) throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        return createConsumerInternal(destination, null, false);
    }

    /**
     * Creates a consumer for a destination. If this is a topic, we can specify the autoDelete flag
     * @param destination
     * @param uuidTag only used for topics, if null, one is generated as the queue name for this topic
     * @param durableSubscriber true if this is a durable topic subscription
     * @return {@link #createConsumer(Destination)}
     * @throws JMSException if destination is null or we fail to create the destination on the broker
     * @see #createConsumer(Destination)
     */
    public MessageConsumer createConsumerInternal(Destination destination, String uuidTag, boolean durableSubscriber) throws JMSException {
        RMQDestination dest = (RMQDestination) destination;
        String consumerTag = uuidTag != null ? uuidTag : "jms-topic-"+Util.util().generateUUIDTag();

        if (!dest.isDeclared()) {
            if (dest.isQueue()) {
                declareQueue(dest,null, false);
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
                    topics.add(queueName);
                }
                //bind the queue to the exchange and routing key
                this.channel.queueBind(queueName, dest.getExchangeName(), dest.getRoutingKey());
            } catch (IOException x) {
                Util.util().handleException(x);
            }
        }
        RMQMessageConsumer consumer = new RMQMessageConsumer(this, (RMQDestination) destination, consumerTag, getConnection().isStopped());
        consumers.add(consumer);
        return consumer;
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException - method not implemented until we support selectors
     */
    @Override
    public MessageConsumer createConsumer(Destination destination, String messageSelector) throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
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
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
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
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
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
    protected void declareQueue(RMQDestination dest, String queueNameOverride, boolean durableSubscriber) throws JMSException {
        try {
            String queueName = queueNameOverride!=null?queueNameOverride:dest.getQueueName();

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

            HashMap<String,Object> options = new HashMap<String,Object>();

            this.channel.queueDeclare(/* the name of the queue */
                                      queueName,
                                      /* temporary destinations are not durable
                                       * will not survive a server restart
                                       * all JMS queues except temp survive restarts
                                       * only durable topic queues
                                       */
                                      durable,
                                      /* exclusive flag */
                                      exclusive,
                                      /*
                                       * We don't set auto delete to true ever
                                       * that is cause exclusive(rabbit)==true automatically
                                       * get deleted when a Connection is closed
                                       */
                                      false,
                                      /* Queue properties */
                                      options);
            if (dest.isTemporary()) {
                topics.add(dest.getQueueName());
            }
        } catch (Exception x) {
            Util.util().handleException(x);
        }
        dest.setDeclared(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Topic createTopic(String topicName) throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        RMQDestination dest = new RMQDestination(topicName, false, false);
        declareTopic(dest);
        return dest;
    }

    /**
     * Invokes {@link Channel#exchangeDeclare(String, String, boolean, boolean, boolean, java.util.Map)} to define
     * the fanout exchange used to publish topic messages to
     * this method invokes {@link RMQDestination#setDeclared(boolean)} with a true value
     * @param dest the topic used to publish to
     * @throws JMSException
     */
    protected void declareTopic(RMQDestination dest) throws JMSException {
        try {
            this.channel.exchangeDeclare(/* the name of the exchange */
                                         dest.getExchangeName(),
                                         /* the type is always fanout - this will change when selectors come in play*/
                                         "fanout",
                                         /* durable for all except temporary topics */
                                         !dest.isTemporary(),
                                         /* auto delete is true if temporary - TODO how do we delete exchanges used for temporary topics
                                          * this could be autoDelete=dest.isTemporary()
                                          */
                                         false,
                                         /* internal is false JMS will always publish to the exchange*/
                                         false,
                                         /* object parameters */
                                         new HashMap<String,Object>());
        } catch (IOException x) {
            Util.util().handleException(x);
        }
        dest.setDeclared(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name) throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        RMQMessageConsumer previous = subscriptions.get(name);
        if (previous!=null) {
            /*
             * we are changing subscription, or not, if called with the same topic
             */

            if (previous.getDestination().equals(topic)) {
                if (previous.isClosed()) {
                    /*
                     * They called TopicSubscriber.close but didn't unsubscribe
                     * and they are simply resubscribing with a new one
                     */
                } else {
                    throw new JMSException("Subscription with name["+name+"] and topic["+topic+"] already exists");
                }
            } else {
                //change in subscription
                unsubscribe(name);
            }
        }

        /*
         * Create the new subscription
         */
        RMQMessageConsumer result = (RMQMessageConsumer)createConsumerInternal(topic, name, true);
        result.setDurable(true);
        subscriptions.put(name, result);
        return result;
    }

    /**
     * {@inheritDoc}
     * @throws UnsupportedOperationException selectors not yet implemented
     */
    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String name, String messageSelector, boolean noLocal) throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
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
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueBrowser createBrowser(Queue queue, String messageSelector) throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporaryQueue createTemporaryQueue() throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        RMQDestination result = new RMQDestination("jms-temp-queue-"+Util.util().generateUUIDTag(), true, true);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporaryTopic createTemporaryTopic() throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        RMQDestination result = new RMQDestination("jms-temp-topic-"+Util.util().generateUUIDTag(), false, true);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribe(String name) throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        try {
            if (name!=null && subscriptions.remove(name)!=null) {
                //remove the queue from the fanout exchange
                this.channel.queueDelete(name);
            }
        }catch (IOException x) {
            Util.util().handleException(x);
        }
    }

    /**
     * Simply here to satisfy a test
     * @param dest
     * @return
     */
    public QueueReceiver createReceiver(RMQDestination dest) throws JMSException {
        return (QueueReceiver)createReceiver((Queue)dest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueReceiver createReceiver(Queue queue) throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        return (QueueReceiver) this.createConsumer(queue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueReceiver createReceiver(Queue queue, String messageSelector) throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        return (QueueReceiver) this.createConsumer(queue, messageSelector);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueSender createSender(Queue queue) throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        return (QueueSender) this.createProducer(queue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicSubscriber createSubscriber(Topic topic) throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
        return (TopicSubscriber) this.createConsumer(topic);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TopicSubscriber createSubscriber(Topic topic, String messageSelector, boolean noLocal) throws JMSException {
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
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
        Util.util().checkTrue(this.closed, "Session has been closed",IllegalStateException.class);
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

    /**
     * Invoked when the {@link RMQMessageConsumer} has received a message
     * so that we can track the last delivery tag
     * This is used to NACK messages when a transaction is rolled back
     *
     * @param response
     * @see Channel#basicNack(long, boolean, boolean)
     * @see Session#rollback()
     */
    public void messageReceived(GetResponse response) {
        if (!transacted) return; //auto ack
        while (true) {
            long last = lastReceivedTag.get();
            long messTag = response.getEnvelope().getDeliveryTag();
            if (messTag>last) {
                if (lastReceivedTag.compareAndSet(last, messTag)) {
                    break;
                }
            } else {
                break;
            }

        }
    }

    public void consumerClose(RMQMessageConsumer consumer) {
        this.consumers.remove(consumer);
        if (consumer.isDurable()) {
            /*
             * We are closing but haven't unsubscribed, so we can't cancel the subscription
             */

        }
    }

    public void producerClose(RMQMessageProducer producer) {
        this.producers.remove(producer);
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
            } catch (IllegalStateException x) {
                Util.util().handleException(x);
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
                Util.util().handleException(x);
            }
        }
    }


    public void unackedMessageReceived(RMQMessage message) {
        if (!getTransactedNoException()) {
            synchronized (receivedMessages) {
                receivedMessages.add(message.getRabbitDeliveryTag());
            }
        }
    }

    /**
     * Acknowledges a message manually.
     * Invoked when the method
     * {@link javax.jms.Message#acknowledge()}
     * @param message - the message to be acknowledged
     */
    public void acknowledge(RMQMessage message) throws JMSException{
        Util.util().checkTrue(isClosed(), "Session has already been closed.", IllegalStateException.class);
        try {
            //TODO future functionality, allow group ack up until the current tag
            boolean groupAck = false;
            //TODO future functionality, allow ack of single message
            boolean individualAck = false;
            if ((!isAutoAck()) && (!getTransacted())) {
                /*
                 * Per JMS specification Message.acknowledge(), if we ack
                 * the last message in a group, we will ack all the ones prior received
                 * Spec 11.2.21 says
                 * "Note that the acknowledge method of Message acknowledges all messages
                 * received on that message’s session."
                 * However, the JavaDoc states
                 */
                synchronized (receivedMessages) {
                    /*
                     * Make sure that the message is in our unack list
                     * or we can't proceed
                     * if it is not in the list, then the message is either
                     * auto acked or been manually acked previously
                     */
                    if (!receivedMessages.contains(message.getRabbitDeliveryTag())) {
                        //if we acked a non existent tag, it may close the channel
                        return;
                    } else if (individualAck) {
                        /* ACK a single message */
                        getChannel().basicAck(/* we ack the highest tag */
                                              message.getRabbitDeliveryTag(),
                                              /* and we ack everything up to that tag */
                                              false);
                        /* remove the message just acked from our list of un-acked messages */
                        receivedMessages.remove(message.getRabbitDeliveryTag());
                    }else if (groupAck) {
                        /* ack multiple message up until the existing tag */
                        getChannel().basicAck(/* we ack the highest tag */
                                              message.getRabbitDeliveryTag(),
                                              /* and we ack everything up to that tag */
                                              true);
                        /* now delete all the messages that we tagged */
                        long first = receivedMessages.first();
                        while (first<=message.getRabbitDeliveryTag()) {
                            receivedMessages.remove(first);
                            if (receivedMessages.size()>0) {
                                first = receivedMessages.first();
                            } else {
                                break;
                            }
                        }
                    } else if (receivedMessages.contains(message.getRabbitDeliveryTag())) {
                        long deliveryTag = receivedMessages.last();
                        /*
                         * Ack all the messages up to this message
                         */
                        getChannel().basicAck(/* we ack the highest tag */
                                              deliveryTag,
                                              /* and we ack everything up to that tag */
                                              true);
                        receivedMessages.clear();
                    }

                }
            }
        } catch (IOException x) {
            Util.util().handleException(x);
        }
    }

    private final class MessageListenerWrapper implements MessageListener {
        private final MessageListener listener;
        public MessageListenerWrapper(MessageListener listener) {
            this.listener = listener;
        }

        @Override
        public void onMessage(Message message) {
            runningListener.countUp();
            try {
                listener.onMessage(message);
            } finally {
                runningListener.countDown();
            }

        }
    }

}
