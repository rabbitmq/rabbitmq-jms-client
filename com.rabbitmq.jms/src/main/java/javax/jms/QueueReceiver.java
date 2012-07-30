/*
 * @(#)QueueReceiver.java	1.21 02/04/09
 *
 * Copyright 1997-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  SUN PROPRIETARY/CONFIDENTIAL.
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */


package javax.jms;


/** A client uses a <CODE>QueueReceiver</CODE> object to receive messages that 
  * have been delivered to a queue.
  *
  * <P>Although it is possible to have multiple <CODE>QueueReceiver</CODE>s 
  * for the same queue, the JMS API does not define how messages are 
  * distributed between the <CODE>QueueReceiver</CODE>s.
  *
  * <P>If a <CODE>QueueReceiver</CODE> specifies a message selector, the 
  * messages that are not selected remain on the queue. By definition, a message
  * selector allows a <CODE>QueueReceiver</CODE> to skip messages. This 
  * means that when the skipped messages are eventually read, the total ordering
  * of the reads does not retain the partial order defined by each message 
  * producer. Only <CODE>QueueReceiver</CODE>s without a message selector
  * will read messages in message producer order.
  *
  * <P>Creating a <CODE>MessageConsumer</CODE> provides the same features as
  * creating a <CODE>QueueReceiver</CODE>. A <CODE>MessageConsumer</CODE> object is 
  * recommended for creating new code. The  <CODE>QueueReceiver</CODE> is
  * provided to support existing code.
  *
  * @version     1.1 February 2, 2002
  * @author      Mark Hapner
  * @author      Rich Burridge
  * @author      Kate Stout
  *
  * @see         javax.jms.Session#createConsumer(Destination, String)
  * @see         javax.jms.Session#createConsumer(Destination)
  * @see         javax.jms.QueueSession#createReceiver(Queue, String)
  * @see         javax.jms.QueueSession#createReceiver(Queue)
  * @see         javax.jms.MessageConsumer
  */

public interface QueueReceiver extends MessageConsumer {

    /** Gets the <CODE>Queue</CODE> associated with this queue receiver.
      *  
      * @return this receiver's <CODE>Queue</CODE> 
      *  
      * @exception JMSException if the JMS provider fails to get the queue for
      *                         this queue receiver
      *                         due to some internal error.
      */ 
 
    Queue
    getQueue() throws JMSException;
}
