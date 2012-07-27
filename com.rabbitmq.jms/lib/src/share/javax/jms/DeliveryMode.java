/*
 * @(#)DeliveryMode.java	1.9 02/04/09
 *
 * Copyright 1997-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  SUN PROPRIETARY/CONFIDENTIAL.
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */


package javax.jms;

/** The delivery modes supported by the JMS API are <CODE>PERSISTENT</CODE> and
  * <CODE>NON_PERSISTENT</CODE>.
  *
  * <P>A client marks a message as persistent if it feels that the 
  * application will have problems if the message is lost in transit.
  * A client marks a message as non-persistent if an occasional
  * lost message is tolerable. Clients use delivery mode to tell a
  * JMS provider how to balance message transport reliability with throughput.
  *
  * <P>Delivery mode covers only the transport of the message to its 
  * destination. Retention of a message at the destination until
  * its receipt is acknowledged is not guaranteed by a <CODE>PERSISTENT</CODE> 
  * delivery mode. Clients should assume that message retention 
  * policies are set administratively. Message retention policy
  * governs the reliability of message delivery from destination
  * to message consumer. For example, if a client's message storage 
  * space is exhausted, some messages may be dropped in accordance with 
  * a site-specific message retention policy.
  *
  * <P>A message is guaranteed to be delivered once and only once
  * by a JMS provider if the delivery mode of the message is 
  * <CODE>PERSISTENT</CODE> 
  * and if the destination has a sufficient message retention policy.
  *
  *
  *
  * @version     1.0 - 7 August 1998
  * @author      Mark Hapner
  * @author      Rich Burridge
  */

public interface DeliveryMode {

    /** This is the lowest-overhead delivery mode because it does not require 
      * that the message be logged to stable storage. The level of JMS provider
      * failure that causes a <CODE>NON_PERSISTENT</CODE> message to be lost is 
      * not defined.
      *
      * <P>A JMS provider must deliver a <CODE>NON_PERSISTENT</CODE> message 
      * with an 
      * at-most-once guarantee. This means that it may lose the message, but it 
      * must not deliver it twice.
      */

    static final int NON_PERSISTENT = 1;

    /** This delivery mode instructs the JMS provider to log the message to stable 
      * storage as part of the client's send operation. Only a hard media 
      * failure should cause a <CODE>PERSISTENT</CODE> message to be lost.
      */

    static final int PERSISTENT = 2;
}
