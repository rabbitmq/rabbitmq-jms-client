package com.rabbitmq.jms.client;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.jms.admin.RMQDestination;
import com.rabbitmq.jms.util.Util;

public class RMQMessageConsumer implements MessageConsumer, QueueReceiver, TopicSubscriber {

    private final RMQDestination destination;
    private final RMQSession session;
    private final String uuidTag;
    private AtomicReference<MessageListenerWrapper> listener = new AtomicReference<MessageListenerWrapper>();

    public RMQMessageConsumer(RMQSession session, RMQDestination destination, String uuidTag) {
        this.session = session;
        this.destination = destination;
        this.uuidTag = uuidTag;
    }

    @Override
    public Queue getQueue() throws JMSException {
        return destination;
    }

    @Override
    public String getMessageSelector() throws JMSException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MessageListener getMessageListener() throws JMSException {
        MessageListenerWrapper wrapper = this.listener.get();
        if (wrapper != null) {
            return wrapper.getMessageListener();
        }
        return null;

    }

    @Override
    public void setMessageListener(MessageListener listener) throws JMSException {
        try {
            MessageListenerWrapper wrapper = listener==null?null:this.wrap(listener);
            MessageListenerWrapper previous = this.listener.getAndSet(wrapper);
            if (previous != null) {
                this.basicCancel(previous.getConsumerTag());
            }
            if (wrapper!=null) {
                String consumerTag = basicConsume(wrapper);
                wrapper.setConsumerTag(consumerTag);
            }
        } catch (IOException x) {
            Util.util().handleException(x);
        }
    }

    @Override
    public Message receive() throws JMSException {
        return receive(Long.MAX_VALUE);
    }

    @Override
    public Message receive(long timeout) throws JMSException {
        Message msg = receiveNoWait();
        if (msg != null) {
            // attempt instant receive first
            return msg;
        }
        if (timeout == 0) {
            timeout = Long.MAX_VALUE;
        }

        try {
            SynchronousConsumer sc = new SynchronousConsumer(this.session.getChannel(), timeout);
            basicConsume(sc);
            GetResponse response = sc.receive();
            return processMessage(response);
        } catch (IOException x) {
            Util.util().handleException(x);
        }
        return null;
    }

    protected String basicConsume(Consumer consumer) throws IOException {
        String name = null;
        if (this.destination.isQueue()) {
            name = this.destination.getName();
        } else {
            name = this.getUUIDTag();
        }
        
        return getSession().getChannel().basicConsume(name, !getSession().getTransactedNoException(), consumer);
    }

    protected void basicCancel(String consumerTag) throws IOException {
        getSession().getChannel().basicCancel(consumerTag);
    }

    @Override
    public Message receiveNoWait() throws JMSException {
        try {
            GetResponse response = null;
            if (this.destination.isQueue()) {
                response = this.getSession().getChannel().basicGet(this.destination.getQueueName(), !this.getSession().getTransacted());
            } else {
                response = this.getSession().getChannel().basicGet(this.getUUIDTag(), !this.getSession().getTransacted());
            }

            return processMessage(response);
        } catch (IOException x) {
            Util.util().handleException(x);
        }
        return null;
    }

    private Message processMessage(GetResponse response) throws JMSException {
        try {
            if (response == null)
                return null;
            this.session.messageReceived(response);
            RMQMessage message = RMQMessage.fromMessage(response.getBody());
            try {
                MessageListener listener = getSession().getMessageListener();
                if (listener!=null) listener.onMessage(message);
            } catch (JMSException x) {
                x.printStackTrace(); //TODO logging implementation
            }
            return message;
        } catch (IOException x) {
            Util.util().handleException(x);
        } catch (ClassNotFoundException x) {
            Util.util().handleException(x);
        } catch (IllegalAccessException x) {
            Util.util().handleException(x);
        } catch (InstantiationException x) {
            Util.util().handleException(x);
        }
        return null;
    }

    @Override
    public void close() throws JMSException {
        setMessageListener(null);
    }

    public RMQDestination getDestination() {
        return this.destination;
    }

    public RMQSession getSession() {
        return this.session;
    }

    public String getUUIDTag() {
        return this.uuidTag;
    }

    @Override
    public Topic getTopic() throws JMSException {
        return this.getDestination();
    }

    @Override
    public boolean getNoLocal() throws JMSException {
        return false;
    }

    protected MessageListenerWrapper wrap(MessageListener listener) throws IOException {
        return new MessageListenerWrapper(listener);
    }
    
    protected class MessageListenerWrapper implements Consumer {
        private MessageListener listener;
        private volatile String consumerTag;
        

        public MessageListenerWrapper(MessageListener listener) {
            this.listener = listener;
        }

        public String getConsumerTag() {
            return consumerTag;
        }

        public void setConsumerTag(String consumerTag) {
            this.consumerTag = consumerTag;
        }

        public MessageListener getMessageListener() {
            return listener;
        }

        @Override
        public void handleConsumeOk(String consumerTag) {
        }

        @Override
        public void handleCancelOk(String consumerTag) {
        }

        @Override
        public void handleCancel(String consumerTag) throws IOException {
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
            if (this.consumerTag==null) this.consumerTag = consumerTag;
            GetResponse response = new GetResponse(envelope, properties, body, 0);
            try {
                Message message = processMessage(response);
                this.listener.onMessage(message);
            } catch (JMSException x) {
                x.printStackTrace(); //TODO logging implementation
                throw new IOException(x);
            }
        }

        @Override
        public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
            // TODO Auto-generated method stub

        }

        @Override
        public void handleRecoverOk(String consumerTag) {
            // TODO Auto-generated method stub

        }

    }

}
