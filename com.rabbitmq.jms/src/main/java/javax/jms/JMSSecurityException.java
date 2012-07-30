/*
 * @(#)JMSSecurityException.java	1.8 02/04/09
 *
 * Copyright 1997-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  SUN PROPRIETARY/CONFIDENTIAL.
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */


package javax.jms;

/**
 * <P> This exception must be thrown when a provider rejects a user 
 *     name/password submitted by a client. It may also be thrown for any case 
 *     where a security restriction prevents a method from completing.
 *
 * @version     26 August 1998
 * @author      Rahul Sharma
 **/

public class JMSSecurityException extends JMSException {

  /** Constructs a <CODE>JMSSecurityException</CODE> with the specified 
   *  reason and error code.
   *
   *  @param  reason        a description of the exception
   *  @param  errorCode     a string specifying the vendor-specific
   *                        error code
   *                        
   **/
  public 
  JMSSecurityException(String reason, String errorCode) {
    super(reason, errorCode);
  }

  /** Constructs a <CODE>JMSSecurityException</CODE> with the specified 
   *  reason. The error code defaults to null.
   *
   *  @param  reason        a description of the exception
   **/
  public 
  JMSSecurityException(String reason) {
    super(reason);
  }

}
