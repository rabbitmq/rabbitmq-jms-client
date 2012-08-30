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
 * $Id: MessagingBehaviour.java,v 1.6 2005/06/16 08:12:04 tanderson Exp $
 */
package org.exolab.jmscts.core;

import javax.jms.DeliveryMode;
import javax.jms.Message;


/**
 * Determines the behaviour for sending and receiving messages
 *
 * @version     $Revision: 1.6 $ $Date: 2005/06/16 08:12:04 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         DeliveryType
 * @see         TestContext
 */
public class MessagingBehaviour {

    /**
     * The delivery mode on send. This can be one of DeliveryMode.PERSISTENT
     * or DeliveryMode.NON_PERSISTENT
     */
    private int _deliveryMode = Message.DEFAULT_DELIVERY_MODE;

    /**
     * If true, destinations are administered, otherwise they are temporary
     */
    private boolean _administered = true;

    /**
     * The type of message receipt to use
     */
    private ReceiptType _receipt = ReceiptType.SYNCHRONOUS;

    /**
     * If true, topic subscribers are durable
     */
    private boolean _durable = false;

    /**
     * The message priority on send
     */
    private int _priority = Message.DEFAULT_PRIORITY;

    /**
     * The time to wait for messages in milliseconds when listening/receiving
     */
    private long _timeout = DEFAULT_TIMEOUT;

    /**
     * The time-to-live for messages, in milliseconds. Zero indicates that
     * messages don't expire
     */
    private long _timeToLive = 0;

    /**
     * The default timeout
     */
    private static final long DEFAULT_TIMEOUT;


    /**
     * Construct a new instance, with default values
     */
    public MessagingBehaviour() {
    }

    /**
     * Construct a new instance, specifying the delivery type.
     *
     * @param type the type of delivery when sending and receiving messages
     */
    public MessagingBehaviour(DeliveryType type) {
        if (type == null) {
            throw new IllegalArgumentException("Argument 'type' is null");
        }
        _deliveryMode = type.getDeliveryMode();
        _administered = type.getAdministered();
        _receipt = type.getReceiptType();
    }

    /**
     * Construct a new instance, copying from an existing behaviour
     *
     * @param behaviour behaviour the behaviour to copy
     */
    public MessagingBehaviour(MessagingBehaviour behaviour) {
        if (behaviour == null) {
            throw new IllegalArgumentException("Argument 'behaviour' is null");
        }
        _deliveryMode = behaviour.getDeliveryMode();
        _administered = behaviour.getAdministered();
        _receipt = behaviour.getReceiptType();
        _durable = behaviour.getDurable();
        _priority = behaviour.getPriority();
        _timeout = behaviour.getTimeout();
        _timeToLive = behaviour.getTimeToLive();
    }

    /**
     * Set the message delivery mode. This can be one of
     * DeliveryMode.PERSISTENT or DeliveryMode.NON_PERSISTENT
     *
     * @param mode the message delivery mode when sending messages
     */
    public void setDeliveryMode(int mode) {
        _deliveryMode = mode;
    }

    /**
     * Get the message delivery mode
     *
     * @return the delivery mode used when sending messages
     */
    public int getDeliveryMode() {
        return _deliveryMode;
    }

    /**
     * Set the destination type (administered/temporary)
     *
     * @param type if true, destinations are administered
     */
    public void setAdministered(boolean type) {
        _administered = type;
    }

    /**
     * Determines if destinations are administered or temporary
     *
     * @return <code>true</code> if destinations are administered,
     * <code>false</code> if they are temporary.
     */
    public boolean getAdministered() {
        return _administered;
    }

    /**
     * Set the message receipt behaviour
     *
     * @param receipt the message receipt behaviour
     */
    public void setReceiptType(ReceiptType receipt) {
        _receipt = receipt;
    }

    /**
     * Returns the message receipt behaviour
     *
     * @return the message receipt behaviour. May be <code>null</code>
     */
    public ReceiptType getReceiptType() {
        return _receipt;
    }

    /**
     * Set the topic subscriber behaviour
     *
     * @param durable if true topic subscribers are durable
     */
    public void setDurable(boolean durable) {
        _durable = durable;
    }

    /**
     * Returns true if topic subscribers are durable.
     *
     * @return true if topic subscribers are durable.
     */
    public boolean getDurable() {
        return _durable;
    }

    /**
     * Set the priority of messages.
     *
     * @param priority the priority set on a message when it is sent
     */
    public void setPriority(int priority) {
        _priority = priority;
    }

    /**
     * Get the message priority used when sending messages
     *
     * @return the priority of messages set when they are sent
     */
    public int getPriority() {
        return _priority;
    }

    /**
     * Set the time to wait when listening or receiving messages, before
     * timing out
     *
     * @param timeout the time to wait, in milliseconds
     */
    public void setTimeout(long timeout) {
        _timeout = timeout;
    }

    /**
     * Get the time to wait when listening or receiving messages
     *
     * @return the time to wait, in milliseconds
     */
    public long getTimeout() {
        return _timeout;
    }

    /**
     * Set the default time to live for messages
     *
     * @param timeToLive the message time to live in milliseconds; zero is
     * unlimited
     */
    public void setTimeToLive(long timeToLive) {
        _timeToLive = timeToLive;
    }

    /**
     * Returns the default time to live for messages
     *
     * @return the default message time to live in milliseconds; zero is
     * unlimited
     */
    public long getTimeToLive() {
        return _timeToLive;
    }

    /**
     * Returns a string representation of the messaging behaviour
     *
     * @return a string representation of this
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer("delivery mode=");
        result.append((_deliveryMode == DeliveryMode.PERSISTENT)
                      ? "PERSISTENT" : "NON_PERSISTENT");
        if (_receipt != null) {
            result.append(", message consumer type=");
            result.append(_receipt);
        }
        result.append(", destination type=");
        result.append((_administered) ? "administered" : "temporary");
        result.append(", timeToLive=" + _timeToLive);
        if (_durable) {
            result.append(", using durable subscriber");
        }
        return result.toString();
    }

    static {
        final int timeout = 2000;
        DEFAULT_TIMEOUT = TestProperties.getLong(
            MessagingBehaviour.class, "timeout", timeout);
    }

}
