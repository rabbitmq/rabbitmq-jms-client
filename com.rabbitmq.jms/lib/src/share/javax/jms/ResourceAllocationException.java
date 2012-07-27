/*
 * @(#)ResourceAllocationException.java	1.6 02/04/09
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
 * <P>This exception is thrown when a provider is unable to allocate the 
 *    resources required by a method. For example, this exception should be 
 *    thrown when a call to 
 *    <CODE>TopicConnectionFactory.createTopicConnection</CODE> fails due to a
 *    lack of JMS provider resources.
 *
 * @version     26 August 1998
 * @author      Rahul Sharma
 **/

public class ResourceAllocationException extends JMSException {

  /** Constructs a <CODE>ResourceAllocationException</CODE> with the specified 
   *  reason and error code.
   *
   *  @param  reason        a description of the exception
   *  @param  errorCode     a string specifying the vendor-specific
   *                        error code
   *                        
   **/
  public 
  ResourceAllocationException(String reason, String errorCode) {
    super(reason, errorCode);
  }

  /** Constructs a <CODE>ResourceAllocationException</CODE> with the specified 
   *  reason. The error code defaults to null.
   *
   *  @param  reason        a description of the exception
   **/
  public 
  ResourceAllocationException(String reason) {
    super(reason);
  }

}
