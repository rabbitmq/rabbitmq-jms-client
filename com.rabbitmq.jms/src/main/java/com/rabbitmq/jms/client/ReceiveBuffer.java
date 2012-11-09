package com.rabbitmq.jms.client;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.jms.util.Abortable;
import com.rabbitmq.jms.util.AbortableHolder;
import com.rabbitmq.jms.util.TimeTracker;

/**
 * Intermediate buffer between RabbitMQ queue and JMS client.
 * <p>
 * The buffer offers a blocking get, with timeout, where the buffer is populated by
 * an asynchronous RabbitMQ {@link Consumer}.  The buffer fills with messages from
 * a RabbitMQ queue, up to a certain size, prompted to do so whenever the buffer is empty.
 * A new Consumer is constructed whenever needed to populate the buffer.
 * </p>
 * <p>
 * The buffer is dedicated to a particular queue, and has a fixed 'fill size' when created.
 * If more messages than the 'fill size' try to get put in the buffer the excess messages
 * are NACKed back to RabbitMQ.
 * </p>
 * <p>
 * The blocking method <code>get()</code> only returns with <code>null</code> when either the buffer is closed,
 * or the timeout expires.
 * </p>
 * <p>
 * When the buffer is <code>close()</code>d, the messages remaining in the buffer are NACKed.
 * </p>
 */
class ReceiveBuffer implements Abortable {

    private final BlockingDeque<GetResponse> buffer = new LinkedBlockingDeque<GetResponse>();
    private final int batchingSize;
    private final RMQMessageConsumer rmqMessageConsumer;
    private final AbortableHolder abortables = new AbortableHolder();

    /**
     * @param batchingSize - the intended limit of messages that can remain in the buffer.
     * @param rmqMessageConsumer - the JMS MessageConsumer we are serving.
     */
    public ReceiveBuffer(int batchingSize, RMQMessageConsumer rmqMessageConsumer) {
        this.batchingSize = batchingSize;
        this.rmqMessageConsumer = rmqMessageConsumer;
    }

    /**
     * Get a message if one arrives in the time available.
     * @param tt - keeps track of the time
     * @return message gotten, or <code>null</code> if timeout or connection closed.
     */
    public GetResponse get(TimeTracker tt) {
        GetResponse resp = this.buffer.poll();
        if (ReceiveConsumer.isEOFMessage(resp)) { // we've aborted
            return null;
        }
        if (null!=resp)
            return resp;
        // Nothing of import on the queue, let's try to get some more.
        // We must do this even if we have timed out, in case we never fetch any.
        ReceiveConsumer rc = this.getSomeMore();
        try {
            resp = this.buffer.poll(tt.remainingNanos(), TimeUnit.NANOSECONDS);
            if (resp==null                          // we timed out
             || ReceiveConsumer.isEOFMessage(resp)) // Consumer ended before we timed out
                return null;
        } catch (InterruptedException e) {
            log("get", e, "interrupted while buffer.poll-ing");
            Thread.currentThread().interrupt();
        } finally {
            this.abortables.remove(rc);
            log("get","about to cancel+wait");
            rc.cancel(); // ensure consumer is cancelled (eventually; may block).
            log("get", "returned from cancel+wait");
        }
        // real messages (or interruptions) drop through to here
        return resp;
    }

    /**
     * Push a message back on the (head of the) buffer.
     * @param resp - the message to put back
     */
    public void push(GetResponse resp) {
        this.buffer.offerFirst(resp);
    }

    private ReceiveConsumer getSomeMore() {
        // set up a Consumer to put messages in the buffer, and die after first message.
        ReceiveConsumer receiveConsumer = new ReceiveConsumer(this.rmqMessageConsumer, this.buffer, this.batchingSize);
        receiveConsumer.register(); // with RabbitMQ server
        return receiveConsumer;
    }

    private static final boolean LOGGING = false;

    private final void log(String s, Exception x, Object ... c) {
        if (LOGGING) {
            log("Exception ("+x+") in "+s, c);
        }
    }

    private final void log(String s, Object ... c) {
        if (LOGGING) {
            StringBuilder sb = new StringBuilder(s).append('(');
            boolean first = true;
            for (Object obj : c) {
                if (first) first = false;
                else sb.append(", ");
                sb.append(String.valueOf(obj));
            }
            log(sb.append(')').toString());
        }
    }

    private final void log(String s) {
        if (LOGGING)
            System.err.println("--->ReceiveBuffer("+String.valueOf(this.rmqMessageConsumer)+"): "+s+" ["+System.nanoTime()+"]");
    }

    @Override
    public void abort() {
        this.nackAllBuffer();
        this.abortables.abort();
    }

    private void nackAllBuffer() {
        log("nackAllBuffer");
        for (GetResponse resp : this.buffer) {
            try {
                this.rmqMessageConsumer.getSession().getChannel().basicNack(resp.getEnvelope().getDeliveryTag(), false, true);
                log("nackAllBuffer", "basicNack", resp.getEnvelope());
            } catch (Exception e) {
                log("nackAllBuffer",e,"basicNack");
                break;
            }
        }
        this.buffer.clear();
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub

    }

    @Override
    public void start() {
        // TODO Auto-generated method stub

    }
}