/*
 * @(#)TemporaryQueue.java	1.13 02/04/09
 *
 * Copyright 1997-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  SUN PROPRIETARY/CONFIDENTIAL.
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package javax.jms;

/** A <CODE>TemporaryQueue</CODE> object is a unique <CODE>Queue</CODE> object 
  * created for the duration of a <CODE>Connection</CODE>. It is a 
  * system-defined queue that can be consumed only by the 
  * <CODE>Connection</CODE> that created it.
  *
  *<P>A <CODE>TemporaryQueue</CODE> object can be created at either the 
  * <CODE>Session</CODE> or <CODE>QueueSession</CODE> level. Creating it at the
  * <CODE>Session</CODE> level allows to the <CODE>TemporaryQueue</CODE> to 
  * participate in transactions with objects from the Pub/Sub  domain. 
  * If it is created at the <CODE>QueueSession</CODE>, it will only
  * be able participate in transactions with objects from the PTP domain.
  *
  * @version     1.1 - February 2, 2002
  * @author      Mark Hapner
  * @author      Rich Burridge
  * @author      Kate Stout
  *
  * @see Session#createTemporaryQueue()
  * @see QueueSession#createTemporaryQueue()
  */

public interface TemporaryQueue extends Queue {

    /** Deletes this temporary queue. If there are existing receivers
      * still using it, a <CODE>JMSException</CODE> will be thrown.
      *  
      * @exception JMSException if the JMS provider fails to delete the 
      *                         temporary queue due to some internal error.
      */

    void 
    delete() throws JMSException; 
}
