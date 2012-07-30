/*
 * @(#)InvalidClientIDException.java	1.8 02/04/09
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
 *     client attempts to set a connection's client ID to a value that 
 *     is rejected by a provider.
 *
 * @version     26 August 1998
 * @author      Rahul Sharma
 **/

public class InvalidClientIDException extends JMSException {

  /** Constructs an <CODE>InvalidClientIDException</CODE> with the specified 
   *  reason and error code.
   *
   *  @param  reason        a description of the exception
   *  @param  errorCode     a string specifying the vendor-specific
   *                        error code
   *                        
   **/
  public 
  InvalidClientIDException(String reason, String errorCode) {
    super(reason, errorCode);
  }

  /** Constructs an <CODE>InvalidClientIDException</CODE> with the specified 
   *  reason. The error code defaults to null.
   *
   *  @param  reason        a description of the exception
   **/
  public 
  InvalidClientIDException(String reason) {
    super(reason);
  }

}
