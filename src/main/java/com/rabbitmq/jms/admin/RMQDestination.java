/* Copyright (c) 2013 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.admin;

import java.io.Serializable;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.Topic;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;

/**
 * Implementation of a {@link Topic} and {@link Queue} {@link Destination}.
 * <p>
 * This implementation is {@link Serializable} so it can be stored in a JNDI naming context. It is also
 * {@link Referenceable} for the same purpose.
 * </p>
 */
public class RMQDestination implements Queue, Topic, Destination, Referenceable, Serializable, TemporaryQueue, TemporaryTopic {

    private static final long serialVersionUID = 596966152753718825L;

    private static final String RABBITMQ_AMQ_TOPIC_EXCHANGE_NAME = "amq.topic";
    private static final String RABBITMQ_AMQ_TOPIC_EXCHANGE_TYPE = "topic";             // standard topic exchange type in RabbitMQ
    private static final String JMS_DURABLE_TOPIC_EXCHANGE_NAME = "jms.durable.topic";  // fixed topic exchange in RabbitMQ for jms traffic
    private static final String JMS_TEMP_TOPIC_EXCHANGE_NAME = "jms.temp.topic";        // fixed topic exchange in RabbitMQ for jms traffic

    private static final String RABBITMQ_UNNAMED_EXCHANGE = "";
    private static final String RABBITMQ_AMQ_DIRECT_EXCHANGE_NAME = "amq.direct";
    private static final String RABBITMQ_AMQ_DIRECT_EXCHANGE_TYPE = "direct";           // standard direct exchange type in RabbitMQ
    private static final String JMS_DURABLE_QUEUE_EXCHANGE_NAME = "jms.durable.queues"; // fixed queue exchange in RabbitMQ for jms traffic
    private static final String JMS_TEMP_QUEUE_EXCHANGE_NAME = "jms.temp.queues";       // fixed queue exchange in RabbitMQ for jms traffic

    // Would like all these to be final, but we need to allow set them
    private String destinationName;
    /** <code>true</code> if maps JMS destination to AMQP resource in RabbitMQ server */
    private boolean amqp;
    private String amqpExchangeName;
    private String amqpRoutingKey;
    private String amqpQueueName;

    private boolean isQueue;
    private boolean isTemporary;

    private transient boolean isDeclared;   // field not serialised and not recovered

    /**
     * Constructor used only for Java serialisation
     */
    public RMQDestination() {
        this.isDeclared = false;    // transient field reset on deserialisation
    }

    /**
     * Creates a destination for RJMS
     * @param destName the name of the topic or queue
     * @param isQueue true if this represent a queue
     * @param isTemporary true if this is a temporary destination
     */
    public RMQDestination(String destName, boolean isQueue, boolean isTemporary) {
        this(destName, false, queueOrTopicExchangeName(isQueue, isTemporary), destName, destName, isQueue, isTemporary);
    }

    private static final String queueOrTopicExchangeName(boolean isQueue, boolean isTemporary) {
        if (isQueue & isTemporary)              return JMS_TEMP_QUEUE_EXCHANGE_NAME;
        else if (isQueue & !isTemporary)        return JMS_DURABLE_QUEUE_EXCHANGE_NAME;
        else if (!isQueue & isTemporary)        return JMS_TEMP_TOPIC_EXCHANGE_NAME;
        else /* if (!isQueue & !isTemporary) */ return JMS_DURABLE_TOPIC_EXCHANGE_NAME;
    }

    private static final String queueOrTopicExchangeType(boolean isQueue) {
        if (isQueue) return RABBITMQ_AMQ_DIRECT_EXCHANGE_TYPE;
        else         return RABBITMQ_AMQ_TOPIC_EXCHANGE_TYPE;
    }

    /**
     * Creates a destination for RJMS mapped onto an AMQP queue/destination.
     * <p>
     * <code>amqpExchangeName</code> and <code>amqpRoutingKey</code> must both be <code>null</code> if either is <code>null</code>, and <code>amqpQueueName</code> may be <code>null</code>, but at
     * least one of these three parameters must be non-<code>null</code>.
     * </p>
     *
     * @param destName the name of the queue destination
     * @param amqpExchangeName - the exchange name for the mapped resource
     * @param amqpRoutingKey - the routing key for the mapped resource
     * @param amqpQueueName - the queue name of the mapped resource
     */
    public RMQDestination(String destName, String amqpExchangeName, String amqpRoutingKey, String amqpQueueName) {
        this(destName, true, amqpExchangeName, amqpRoutingKey, amqpQueueName, true, false);
    }

    /**
     * Creates a destination: either a queue or a topic; either mapped to a real AMQP resource or not.
     * <p>
     * If this is a mapped AMQP resource then if either <code>amqpExchangeName</code> or <code>amqpRoutingKey</code> is
     * <code>null</code> then the other must be <code>null</code> too, and at least one of <code>amqpExchangeName</code>,
     * <code>amqpRoutingKey</code> and <code>amqpQueueName</code> must be non-<code>null</code>.
     * </p>
     *
     * @param destName - the name of the topic or the queue
     * @param amqp - <code>true</code> if this is bound to an AMQP resource, <code>false</code> if it is a RJMS resource
     * @param exchangeName - the RabbitMQ exchange name we will publish to and bind to (which may be an amqp resource
     *            exchange)
     * @param routingKey - the routing key used for this destination (if it is a topic)
     * @param isQueue - <code>true</code> if this is a queue, <code>false</code> if this is a topic
     * @param isTemporary true if this is a temporary destination
     */
    private RMQDestination(String destName, boolean amqp, String exchangeName, String routingKey, String queueName, boolean isQueue, boolean isTemporary) {
        this.destinationName = destName;

        if (amqp) {
            if ( (exchangeName==null) != (routingKey==null)
              || (exchangeName==null && routingKey==null && queueName==null)
               ) {
                throw new IllegalArgumentException(
                   String.format("Invalid AMQP resource settings (exchangeName=[%s], routingKey=[%s], queueName=[%s])."
                                                                , exchangeName,      routingKey,      queueName));
            }
        }
        this.amqp = amqp;
        this.amqpExchangeName = exchangeName;
        this.amqpRoutingKey = routingKey;
        this.amqpQueueName = queueName;
        this.isQueue = isQueue;
        this.isTemporary = isTemporary;

        this.isDeclared = false;
    }

    public boolean amqpWritable() {
        return (this.amqp && null != this.amqpExchangeName && null != this.amqpRoutingKey);
    }

    public boolean amqpReadable() {
        return (this.amqp && null != this.amqpQueueName);
    }

    /**
     * @return <code>true</code> if this is an AMQP mapped resource, <code>false</code> otherwise
     */
    public boolean isAmqp() {
        return this.amqp;
    }
    /** For JNDI binding and Spring beans */
    public void setAmqp(boolean amqp) {
        if (this.isDeclared())
            throw new IllegalStateException();
        this.amqp = amqp;
        this.isQueue = true;
        this.isTemporary = false;
    }
    public String getAmqpQueueName() {
        return this.amqpQueueName;
    }
    /** For JNDI binding and Spring beans */
    public void setAmqpQueueName(String amqpQueueName) {
        if (this.isDeclared())
            throw new IllegalStateException();
        this.amqpQueueName = amqpQueueName;
    }
    public String getAmqpExchangeName() {
        return this.amqpExchangeName;
    }
    /** For JNDI binding and Spring beans */
    public void setAmqpExchangeName(String amqpExchangeName) {
        if (this.isDeclared())
            throw new IllegalStateException();
        this.amqpExchangeName = amqpExchangeName;
    }
    public String getDestinationName() {
        return this.destinationName;
    }
    /** For JNDI binding and Spring beans */
    public void setDestinationName(String destinationName) {
        if (isDeclared())
            throw new IllegalStateException();
        this.destinationName = destinationName;
    }
    public String getAmqpRoutingKey() {
        return this.amqpRoutingKey;
    }
    /** For JNDI binding and Spring beans */
    public void setAmqpRoutingKey(String routingKey) {
        if (isDeclared())
            throw new IllegalStateException();
        this.amqpRoutingKey = routingKey;
    }

    /** Internal use only */
    public String amqpExchangeType() {
        return queueOrTopicExchangeType(this.isQueue);
    }

    /** Internal use only */
    public boolean noNeedToDeclareExchange() {
        return RABBITMQ_AMQ_TOPIC_EXCHANGE_NAME .equals(this.amqpExchangeName)
            || RABBITMQ_AMQ_DIRECT_EXCHANGE_NAME.equals(this.amqpExchangeName)
            || RABBITMQ_UNNAMED_EXCHANGE.equals(this.amqpExchangeName);
    }

    /**
     * @return true if this is a queue, false if it is a topic
     */
    public boolean isQueue() {
        return this.isQueue;
    }

    /**
     * Set to true if this is a queue, false if this is a topic - should only be
     * used when binding into JNDI
     *
     * @param isQueue <code>true</code> if this is a queue, <code>false</code> otherwise
     * @throws IllegalStateException if the queue has already been declared
     *             {@link RMQDestination#isDeclared()} return true
     */
    public void setQueue(boolean isQueue) {
        if (isDeclared())
            throw new IllegalStateException();
        this.isQueue = isQueue;
    }

    @Override
    public String getTopicName() throws JMSException {
        return this.destinationName;
    }

    @Override
    public String getQueueName() throws JMSException {
        return this.destinationName;
    }

    @Override
    public Reference getReference() throws NamingException {
        Reference ref = new Reference(this.getClass().getCanonicalName());
        addStringProperty(ref, "destinationName", this.destinationName);
        addBooleanProperty(ref, "amqp", this.amqp);
        addBooleanProperty(ref, "isQueue", this.isQueue);
        addStringProperty(ref, "amqpExchangeName", this.amqpExchangeName);
        addStringProperty(ref, "amqpRoutingKey", this.amqpRoutingKey);
        addStringProperty(ref, "amqpQueueName", this.amqpQueueName);
        return ref;
    }

    /**
     * Adds a String valued property to a Reference (as a RefAddr) if it is non-<code>null</code>.
     * @param ref - the reference to contain the value
     * @param propertyName - the name of the property
     * @param value - the value to store with the property
     */
    private static final void addStringProperty(Reference ref,
                                                String propertyName,
                                                String value) {
        if (value==null || propertyName==null) return;
        RefAddr ra = new StringRefAddr(propertyName, value);
        ref.add(ra);
    }

    /**
     * Adds a boolean valued property to a Reference (as a StringRefAddr) if the value is <code>true</code>
     * (default <code>false</code> on read assumed).
     * @param ref - the reference to contain the value
     * @param propertyName - the name of the property
     * @param value - the value to store with the property
     */
    private static final void addBooleanProperty(Reference ref,
                                                 String propertyName,
                                                 boolean value) {
        if (propertyName==null) return;
        if (value) {
            RefAddr ra = new StringRefAddr(propertyName, String.valueOf(value));
            ref.add(ra);
        }
    }

    /**
     * For internal use only.
     * @return true if we have declared RabbitMQ resources to back this destination
     */
    public boolean isDeclared() {
        return isDeclared;
    }

    /**
     * For internal use only.
     *
     * @param isDeclared - set to true if the queue/topic has been defined in the
     *            RabbitMQ broker
     * @see #isDeclared()
     */
    public void setDeclared(boolean isDeclared) {
        this.isDeclared = isDeclared;
    }

    /**
     * @return <code>true</code> if this is a temporary destination, <code>false</code> otherwise
     */
    public boolean isTemporary() {
        return isTemporary;
    }

    /**
     * This method is for {@link TemporaryQueue}s only — deletion currently occurs automatically on session close.
     * {@inheritDoc}
     */
    @Override
    public void delete() throws JMSException {
        //TODO implement delete by Channel.queueDelete for TemporaryQueues only
        //See RMQSession.close how we call Channel.queueDelete
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (amqp ? 1231 : 1237);
        result = prime * result + ((amqpExchangeName == null) ? 0 : amqpExchangeName.hashCode());
        result = prime * result + ((amqpQueueName == null) ? 0 : amqpQueueName.hashCode());
        result = prime * result + ((amqpRoutingKey == null) ? 0 : amqpRoutingKey.hashCode());
        result = prime * result + ((destinationName == null) ? 0 : destinationName.hashCode());
        result = prime * result + (isQueue ? 1231 : 1237);
        result = prime * result + (isTemporary ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof RMQDestination))
            return false;
        RMQDestination other = (RMQDestination) obj;
        if (amqp != other.amqp)
            return false;
        if (amqpExchangeName == null) {
            if (other.amqpExchangeName != null)
                return false;
        } else if (!amqpExchangeName.equals(other.amqpExchangeName))
            return false;
        if (amqpQueueName == null) {
            if (other.amqpQueueName != null)
                return false;
        } else if (!amqpQueueName.equals(other.amqpQueueName))
            return false;
        if (amqpRoutingKey == null) {
            if (other.amqpRoutingKey != null)
                return false;
        } else if (!amqpRoutingKey.equals(other.amqpRoutingKey))
            return false;
        if (destinationName == null) {
            if (other.destinationName != null)
                return false;
        } else if (!destinationName.equals(other.destinationName))
            return false;
        if (isQueue != other.isQueue)
            return false;
        if (isTemporary != other.isTemporary)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder("RMQDestination{")
          .append("destinationName='").append(destinationName)
          .append(this.isQueue ? "', queue(" : "', topic(")
          .append(this.isTemporary ? "temporary" : "permanent")
          .append(this.amqp ? ", amqp)" : ")")
          .append("', amqpExchangeName='").append(amqpExchangeName)
          .append("', amqpRoutingKey='").append(amqpRoutingKey)
          .append("', amqpQueueName='").append(amqpQueueName)
          .append("'}").toString()
          ;
    }
}
