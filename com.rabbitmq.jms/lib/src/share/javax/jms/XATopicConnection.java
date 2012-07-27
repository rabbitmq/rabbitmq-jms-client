/*
 * @(#)XATopicConnection.java	1.17 02/04/09
 *
 * Copyright 1997-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  SUN PROPRIETARY/CONFIDENTIAL.
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */


package javax.jms;

/** An <CODE>XATopicConnection</CODE> provides the same create options as 
  * <CODE>TopicConnection</CODE> (optional). The Topic connections created are
  * transactional.
  *
  * <P>The <CODE>XATopicConnection</CODE> interface is optional.  JMS providers 
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
  * @see         javax.jms.XAConnection
  */

public interface XATopicConnection 
	extends XAConnection, TopicConnection {

    /** Creates an <CODE>XATopicSession</CODE> object.
      *  
      * @return a newly created XA topic session
      *  
      * @exception JMSException if the <CODE>XATopicConnection</CODE> object
      *                         fails to create an XA topic session due to some 
      *                         internal error.
      */ 

    XATopicSession
    createXATopicSession() throws JMSException;

    /** Creates an <CODE>XATopicSession</CODE> object.
      *
      * @param transacted usage undefined
      * @param acknowledgeMode usage undefined
      *  
      * @return a newly created XA topic session
      *  
      * @exception JMSException if the <CODE>XATopicConnection</CODE> object
      *                         fails to create an XA topic session due to some 
      *                         internal error.
      */ 

    TopicSession
    createTopicSession(boolean transacted,
                       int acknowledgeMode) throws JMSException;
}
