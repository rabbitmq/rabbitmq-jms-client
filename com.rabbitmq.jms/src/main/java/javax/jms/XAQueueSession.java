/*
 * @(#)XAQueueSession.java	1.11 02/04/09
 *
 * Copyright 1997-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  SUN PROPRIETARY/CONFIDENTIAL.
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */


package javax.jms;

/** An <CODE>XAQueueSession</CODE> provides a regular <CODE>QueueSession</CODE>,
  * which can be used to
  * create <CODE>QueueReceiver</CODE>, <CODE>QueueSender</CODE>, and 
  *<CODE>QueueBrowser</CODE> objects (optional).
  *
  * <P>The <CODE>XAQueueSession</CODE> interface is optional. JMS providers 
  * are not required to support this interface. This interface is for 
  * use by JMS providers to support transactional environments. 
  * Client programs are strongly encouraged to use the transactional support
  * available in their environment, rather than use these XA
  * interfaces directly. 
 *
  * @version     1.1 - February 2, 2002
  * @author      Mark Hapner
  * @author      Rich Burridge
  * @author      Kate Stout
  *
  * @see         javax.jms.XASession
  */

public interface XAQueueSession extends XASession {

    /** Gets the queue session associated with this <CODE>XAQueueSession</CODE>.
      *  
      * @return the queue session object
      *  
      * @exception JMSException if an internal error occurs.
      */ 
 
    QueueSession
    getQueueSession() throws JMSException;
}
