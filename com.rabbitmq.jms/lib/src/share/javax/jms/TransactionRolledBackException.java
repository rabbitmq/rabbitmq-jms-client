/*
 * @(#)TransactionRolledBackException.java	1.7 02/04/09
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
 *     call to <CODE>Session.commit</CODE> results in a rollback of the current 
 *     transaction.
 *
 * @version     26 August 1998
 * @author      Rahul Sharma
 **/

public class TransactionRolledBackException extends JMSException {

  /** Constructs a <CODE>TransactionRolledBackException</CODE> with the 
   *  specified reason and error code.
   *
   *  @param  reason        a description of the exception
   *  @param  errorCode     a string specifying the vendor-specific
   *                        error code
   *                        
   **/
  public 
  TransactionRolledBackException(String reason, String errorCode) {
    super(reason, errorCode);
  }

  /** Constructs a <CODE>TransactionRolledBackException</CODE> with the 
   *  specified reason. The error code defaults to null.
   *
   *  @param  reason        a description of the exception
   **/
  public 
  TransactionRolledBackException(String reason) {
    super(reason);
  }

}
