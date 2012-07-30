/*
 * @(#)InvalidSelectorException.java	1.6 02/04/09
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
 * <P> This exception must be thrown when a 
 *     JMS client attempts to give a provider a message selector with 
 *     invalid syntax.
 *
 * @version     26 August 1998
 * @author      Rahul Sharma
 **/

public class InvalidSelectorException extends JMSException {

  /** Constructs an <CODE>InvalidSelectorException</CODE> with the specified 
   *  reason and error code.
   *
   *  @param  reason        a description of the exception
   *  @param  errorCode     a string specifying the vendor-specific
   *                        error code
   *                        
   **/
  public 
  InvalidSelectorException(String reason, String errorCode) {
    super(reason, errorCode);
  }

  /** Constructs an <CODE>InvalidSelectorException</CODE> with the specified 
   *  reason. The error code defaults to null.
   *
   *  @param  reason        a description of the exception
   **/
  public 
  InvalidSelectorException(String reason) {
    super(reason);
  }

}
