/*
 * @(#)ConnectionMetaData.java	1.11 02/04/09
 *
 * Copyright 1997-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  SUN PROPRIETARY/CONFIDENTIAL.
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */


package javax.jms;

import java.util.Enumeration;

/** A <CODE>ConnectionMetaData</CODE> object provides information describing the 
  * <CODE>Connection</CODE> object.
  *
  * @version     1.0 - 13 March 1998
  * @author      Mark Hapner
  * @author      Rich Burridge
  */

public interface ConnectionMetaData {

    /** Gets the JMS API version.
      *
      * @return the JMS API version
      *  
      * @exception JMSException if the JMS provider fails to retrieve the
      *                         metadata due to some internal error.
      */

    String 
    getJMSVersion() throws JMSException;


    /** Gets the JMS major version number.
      *  
      * @return the JMS API major version number
      *  
      * @exception JMSException if the JMS provider fails to retrieve the
      *                         metadata due to some internal error.
      */

    int 
    getJMSMajorVersion() throws JMSException; 
 

    /** Gets the JMS minor version number.
      *  
      * @return the JMS API minor version number
      *  
      * @exception JMSException if the JMS provider fails to retrieve the
      *                         metadata due to some internal error.
      */

    int  
    getJMSMinorVersion() throws JMSException;


    /** Gets the JMS provider name.
      *
      * @return the JMS provider name
      *  
      * @exception JMSException if the JMS provider fails to retrieve the
      *                         metadata due to some internal error.
      */ 

    String 
    getJMSProviderName() throws JMSException;


    /** Gets the JMS provider version.
      *
      * @return the JMS provider version
      *  
      * @exception JMSException if the JMS provider fails to retrieve the
      *                         metadata due to some internal error.
      */ 

    String 
    getProviderVersion() throws JMSException;


    /** Gets the JMS provider major version number.
      *  
      * @return the JMS provider major version number
      *  
      * @exception JMSException if the JMS provider fails to retrieve the
      *                         metadata due to some internal error.
      */

    int
    getProviderMajorVersion() throws JMSException; 

 
    /** Gets the JMS provider minor version number.
      *  
      * @return the JMS provider minor version number
      *  
      * @exception JMSException if the JMS provider fails to retrieve the
      *                         metadata due to some internal error.
      */

    int  
    getProviderMinorVersion() throws JMSException;

 
    /** Gets an enumeration of the JMSX property names.
      *  
      * @return an Enumeration of JMSX property names
      *  
      * @exception JMSException if the JMS provider fails to retrieve the
      *                         metadata due to some internal error.
      */

    Enumeration
    getJMSXPropertyNames() throws JMSException;
}
