/*
 * @(#)MessageNotReadableException.java	1.7 02/04/09
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
 * <P> This exception must be thrown when a JMS client attempts to read a 
 *     write-only message.
 *
 * @version     26 August 1998
 * @author      Rahul Sharma
 **/

public class MessageNotReadableException extends JMSException {

  /** Constructs a <CODE>MessageNotReadableException</CODE> with the specified 
   *  reason and error code.
   *
   *  @param  reason        a description of the exception
   *  @param  errorCode     a string specifying the vendor-specific
   *                        error code
   *                        
   **/
  public 
  MessageNotReadableException(String reason, String errorCode) {
    super(reason, errorCode);
  }

  /** Constructs a <CODE>MessageNotReadableException</CODE> with the specified 
   *  reason. The error code defaults to null.
   *
   *  @param  reason        a description of the exception
   **/
  public 
  MessageNotReadableException(String reason) {
    super(reason);
  }

}
