/*
 * @(#)XATopicConnectionFactory.java	1.14 02/04/09
 *
 * Copyright 1997-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  SUN PROPRIETARY/CONFIDENTIAL.
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */


package javax.jms;

/** An <CODE>XATopicConnectionFactory</CODE> provides the same create options as 
  * a <CODE>TopicConnectionFactory</CODE> (optional).
  *
  * <P>The <CODE>XATopicConnectionFactory</CODE> interface is optional.  JMS providers 
  * are not required to support this interface. This interface is for 
  * use by JMS providers to support transactional environments. 
  * Client programs are strongly encouraged to use the transactional support
  * available in their environment, rather than use these XA
  * interfaces directly. 
  *
  * @version     1.1 February 2, 2002
  * @author      Mark Hapner
  * @author      Rich Burridge
  * @author      Kate Stout
  *
  * @see         javax.jms.TopicConnectionFactory
  * @see         javax.jms.XAConnectionFactory
  */

public interface XATopicConnectionFactory 
	extends XAConnectionFactory, TopicConnectionFactory {

    /** Creates an XA topic connection with the default user identity.
      * The connection is created in stopped mode. No messages 
      * will be delivered until the <code>Connection.start</code> method
      * is explicitly called.
      *
      * @return a newly created XA topic connection
      *
      * @exception JMSException if the JMS provider fails to create an XA topic 
      *                         connection due to some internal error.
      * @exception JMSSecurityException  if client authentication fails due to 
      *                         an invalid user name or password.
      */ 

    XATopicConnection
    createXATopicConnection() throws JMSException;


    /** Creates an XA topic connection with the specified user identity.
      * The connection is created in stopped mode. No messages 
      * will be delivered until the <code>Connection.start</code> method
      * is explicitly called.
      *  
      * @param userName the caller's user name
      * @param password the caller's password
      *  
      * @return a newly created XA topic connection
      *
      * @exception JMSException if the JMS provider fails to create an XA topic 
      *                         connection due to some internal error.
      * @exception JMSSecurityException  if client authentication fails due to 
      *                         an invalid user name or password.
      */ 

    XATopicConnection
    createXATopicConnection(String userName, String password) 
					     throws JMSException;
}
