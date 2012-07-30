/*
 * @(#)Queue.java	1.16 02/04/09
 *
 * Copyright 1997-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  SUN PROPRIETARY/CONFIDENTIAL.
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */


package javax.jms;


/** A <CODE>Queue</CODE> object encapsulates a provider-specific queue name. 
  * It is the way a client specifies the identity of a queue to JMS API methods.
  * For those methods that use a <CODE>Destination</CODE> as a parameter, a 
  * <CODE>Queue</CODE> object used as an argument. For example, a queue can
  * be used  to create a <CODE>MessageConsumer</CODE> and a 
  * <CODE>MessageProducer</CODE>  by calling:
  *<UL>
  *<LI> <CODE>Session.CreateConsumer(Destination destination)</CODE>
  *<LI> <CODE>Session.CreateProducer(Destination destination)</CODE>
  *
  *</UL>
  *
  * <P>The actual length of time messages are held by a queue and the 
  * consequences of resource overflow are not defined by the JMS API.
  *
  *
  *
  * @version     1.1 February 2 - 2000
  * @author      Mark Hapner
  * @author      Rich Burridge
  * @author      Kate Stout
  *
  * @see Session#createConsumer(Destination)
  * @see Session#createProducer(Destination)
  * @see Session#createQueue(String)
  * @see QueueSession#createQueue(String)
  */
 
public interface Queue extends Destination { 

    /** Gets the name of this queue.
      *  
      * <P>Clients that depend upon the name are not portable.
      *  
      * @return the queue name
      *  
      * @exception JMSException if the JMS provider implementation of 
      *                         <CODE>Queue</CODE> fails to return the queue
      *                         name due to some internal
      *                         error.
      */ 
 
    String
    getQueueName() throws JMSException;  


    /** Returns a string representation of this object.
      *
      * @return the provider-specific identity values for this queue
      */
 
    String
    toString();
}
