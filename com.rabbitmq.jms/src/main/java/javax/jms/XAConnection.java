/*
 * @(#)XAConnection.java	1.8 02/04/09
 *
 * Copyright 1997-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  SUN PROPRIETARY/CONFIDENTIAL.
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */


package javax.jms;

/** The <CODE>XAConnection</CODE> interface extends the capability of 
  * <CODE>Connection</CODE> by providing an <CODE>XASession</CODE> (optional).
  *
  *<P>The <CODE>XAConnection</CODE> interface is optional. JMS providers 
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
  * @see         javax.jms.XAQueueConnection
  * @see         javax.jms.XATopicConnection
  */

public interface XAConnection extends Connection{
    
    /** Creates an <CODE>XASession</CODE> object.
      *  
      * @return a newly created <CODE>XASession</CODE>
      *  
      * @exception JMSException if the <CODE>XAConnection</CODE> object 
      *                         fails to create an <CODE>XASession</CODE> due to
      *                         some internal error.
      *
      * @since 1.1
      */ 

    XASession
    createXASession() throws JMSException;

    /** Creates an <CODE>Session</CODE> object.
      *
      * @param transacted       usage undefined
      * @param acknowledgeMode  usage undefined
      *  
      * @return a <CODE>Session</CODE> object
      *  
      * @exception JMSException if the <CODE>XAConnection</CODE> object 
      *                         fails to create an <CODE>Session</CODE> due to
      *                         some internal error.
      *
      * @since 1.1
      */ 
    Session
    createSession(boolean transacted,
                       int acknowledgeMode) throws JMSException;
}
