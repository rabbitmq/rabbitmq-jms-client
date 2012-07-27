/*
 * @(#)TemporaryTopic.java	1.11 02/04/09
 *
 * Copyright 1997-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  SUN PROPRIETARY/CONFIDENTIAL.
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */


package javax.jms;

/** A <CODE>TemporaryTopic</CODE> object is a unique <CODE>Topic</CODE> object 
  * created for the duration of a <CODE>Connection</CODE>. It is a 
  * system-defined topic that can be consumed only by the 
  * <CODE>Connection</CODE> that created it.
  *
  *<P>A <CODE>TemporaryTopic</CODE> object can be created either at the 
  * <CODE>Session</CODE> or <CODE>TopicSession</CODE> level. Creating it at the
  * <CODE>Session</CODE> level allows the <CODE>TemporaryTopic</CODE> to participate
  * in the same transaction with objects from the PTP domain. 
  * If a <CODE>TemporaryTopic</CODE> is  created at the 
  * <CODE>TopicSession</CODE>, it will only
  * be able participate in transactions with objects from the Pub/Sub domain.
  *
  * @version     1.1 - February 2, 2002
  * @author      Mark Hapner
  * @author      Rich Burridge
  * @author      Kate Stout
  *
  * @see Session#createTemporaryTopic()
  * @see TopicSession#createTemporaryTopic()
  */

public interface TemporaryTopic extends Topic {

    /** Deletes this temporary topic. If there are existing subscribers
      * still using it, a <CODE>JMSException</CODE> will be thrown.
      *  
      * @exception JMSException if the JMS provider fails to delete the
      *                         temporary topic due to some internal error.
      */

    void 
    delete() throws JMSException; 
}
