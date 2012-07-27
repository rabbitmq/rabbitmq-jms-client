/*
 * @(#)XAQueueConnectionFactory.java	1.14 02/04/09
 *
 * Copyright 1997-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  SUN PROPRIETARY/CONFIDENTIAL.
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */


package javax.jms;

/** An <CODE>XAQueueConnectionFactory</CODE> provides the same create options as
  * a <CODE>QueueConnectionFactory</CODE> (optional).
  *
  * <P>The <CODE>XATopicConnectionFactory</CODE> interface is optional.  JMS providers 
  * are not required to support this interface. This interface is for 
  * use by JMS providers to support transactional environments. 
  * Client programs are strongly encouraged to use the transactional support
  * available in their environment, rather than use these XA
  * interfaces directly. 
  *
  * @version     1.1 - 8 April 2002
  * @author      Mark Hapner
  * @author      Rich Burridge
  * @author      Kate Stout
  *
  * @see         javax.jms.QueueConnectionFactory
  * @see         javax.jms.XAConnectionFactory
  */

public interface XAQueueConnectionFactory 
       extends XAConnectionFactory, QueueConnectionFactory {

    /** Creates an XA queue connection with the default user identity.
      * The connection is created in stopped mode. No messages 
      * will be delivered until the <code>Connection.start</code> method
      * is explicitly called.
      *
      * @return a newly created XA queue connection
      *
      * @exception JMSException if the JMS provider fails to create an XA queue 
      *                         connection due to some internal error.
      * @exception JMSSecurityException  if client authentication fails due to 
      *                         an invalid user name or password.
       */ 

    XAQueueConnection
    createXAQueueConnection() throws JMSException;


    /** Creates an XA queue connection with the specified user identity.
      * The connection is created in stopped mode. No messages 
      * will be delivered until the <code>Connection.start</code> method
      * is explicitly called.
      *  
      * @param userName the caller's user name
      * @param password the caller's password
      *  
      * @return a newly created XA queue connection
      *
      * @exception JMSException if the JMS provider fails to create an XA queue 
      *                         connection due to some internal error.
      * @exception JMSSecurityException  if client authentication fails due to 
      *                         an invalid user name or password.
      */ 

    XAQueueConnection
    createXAQueueConnection(String userName, String password) 
					     throws JMSException;
}
