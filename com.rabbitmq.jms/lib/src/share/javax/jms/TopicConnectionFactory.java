/*
 * @(#)TopicConnectionFactory.java	1.15 02/04/09
 *
 * Copyright 1997-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  SUN PROPRIETARY/CONFIDENTIAL.
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */


package javax.jms;

/** A client uses a <CODE>TopicConnectionFactory</CODE> object to create 
  * <CODE>TopicConnection</CODE> objects with a publish/subscribe JMS provider.
  *
  * <P>A<CODE> TopicConnectionFactory</CODE> can be used to create a 
  * <CODE>TopicConnection</CODE>, from which specialized topic-related objects
  * can be  created. A more general, and recommended approach 
  * is to use the <CODE>ConnectionFactory</CODE> object.
  *  
  * <P> The <CODE>TopicConnectionFactory</CODE> object
  * should be used to support existing code.
  *
  * @version    1.1 - February 2, 2002
  * @author      Mark Hapner
  * @author      Rich Burridge
  * @author      Kate Stout
  *
  * @see         javax.jms.ConnectionFactory
  */

public interface TopicConnectionFactory extends ConnectionFactory {

    /** Creates a topic connection with the default user identity.
      * The connection is created in stopped mode. No messages 
      * will be delivered until the <code>Connection.start</code> method
      * is explicitly called.
      *
      * @return a newly created topic connection
      *
      * @exception JMSException if the JMS provider fails to create a topic 
      *                         connection due to some internal error.
      * @exception JMSSecurityException if client authentication fails due to 
      *                                 an invalid user name or password.
      */ 

    TopicConnection
    createTopicConnection() throws JMSException;


    /** Creates a topic connection with the specified user identity.
      * The connection is created in stopped mode. No messages 
      * will be delivered until the <code>Connection.start</code> method
      * is explicitly called.
      *  
      * @param userName the caller's user name
      * @param password the caller's password
      *  
      * @return a newly created topic connection
      *
      * @exception JMSException if the JMS provider fails to create a topic 
      *                         connection due to some internal error.
      * @exception JMSSecurityException if client authentication fails due to 
      *                                 an invalid user name or password.
      */ 

    TopicConnection
    createTopicConnection(String userName, String password) 
					     throws JMSException;
}
