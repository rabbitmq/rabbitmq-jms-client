/*
 * @(#)TransactionInProgressException.java	1.8 02/04/09
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
 * <P> This exception is thrown when an 
 *     operation is invalid because a transaction is in progress. 
 *     For instance, an attempt to call <CODE>Session.commit</CODE> when a 
 *     session is part of a distributed transaction should throw a 
 *     <CODE>TransactionInProgressException</CODE>.
 *
 * @version     26 August 1998
 * @author      Rahul Sharma
 **/

public class TransactionInProgressException extends JMSException {

  /** Constructs a <CODE>TransactionInProgressException</CODE> with the 
   *  specified reason and error code.
   *
   *  @param  reason        a description of the exception
   *  @param  errorCode     a string specifying the vendor-specific
   *                        error code
   *                        
   **/
  public 
  TransactionInProgressException(String reason, String errorCode) {
    super(reason, errorCode);
  }

  /** Constructs a <CODE>TransactionInProgressException</CODE> with the 
   *  specified reason. The error code defaults to null.
   *
   *  @param  reason        a description of the exception
   **/
  public 
  TransactionInProgressException(String reason) {
    super(reason);
  }

}
