/*
 * @(#)XATopicSession.java	1.12 02/04/09
 *
 * Copyright 1997-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  SUN PROPRIETARY/CONFIDENTIAL.
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */

package javax.jms;

/** An <CODE>XATopicSession</CODE> provides a regular <CODE>TopicSession</CODE>.
  * which can be used to create <CODE>TopicSubscriber</CODE> and 
  * <CODE>TopicPublisher</CODE> objects (optional).
  *
  * <P>The <CODE>XATopicSession</CODE> interface is optional.  JMS providers 
  * are not required to support this interface. This interface is for 
  * use by JMS providers to support transactional environments. 
  * Client programs are strongly encouraged to use the transactional support
  * available in their environment, rather than using these XA
  * interfaces directly. 
  *
  * @version     1.1 February 2, 2002
  * @author      Mark Hapner
  * @author      Rich Burridge
  * @author      Kate Stout
  *
  * @see         javax.jms.XASession
  * @see         javax.jms.TopicSession
  */

public interface XATopicSession extends XASession {

    /** Gets the topic session associated with this <CODE>XATopicSession</CODE>.
      *   
      * @return the topic session object
      *   
      * @exception JMSException if an internal error occurs.
      */  
  
    TopicSession 
    getTopicSession() throws JMSException;
}
