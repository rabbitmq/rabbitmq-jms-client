/*
 * @(#)MessageFormatException.java	1.7 02/04/09
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
 * <P> This exception must be thrown when a JMS client 
 *     attempts to use a data type not supported by a message or attempts to 
 *     read data in a message as the wrong type. It must also be thrown when 
 *     equivalent type errors are made with message property values. For 
 *     example, this exception must be thrown if 
 *     <CODE>StreamMessage.writeObject</CODE> is given an unsupported class or 
 *     if <CODE>StreamMessage.readShort</CODE> is used to read a 
 *     <CODE>boolean</CODE> value. Note that the special case of a failure 
 *     caused by an attempt to read improperly formatted <CODE>String</CODE> 
 *     data as numeric values must throw the 
 *     <CODE>java.lang.NumberFormatException</CODE>.
 *
 * @version     26 August 1998
 * @author      Rahul Sharma
 **/

public class MessageFormatException extends JMSException {

  /** Constructs a <CODE>MessageFormatException</CODE> with the specified 
   *  reason and error code.
   *
   *  @param  reason        a description of the exception
   *  @param  errorCode     a string specifying the vendor-specific
   *                        error code
   *                        
   **/
  public 
  MessageFormatException(String reason, String errorCode) {
    super(reason, errorCode);
  }

  /** Constructs a <CODE>MessageFormatException</CODE> with the specified 
   *  reason. The error code defaults to null.
   *
   *  @param  reason        a description of the exception
   **/
  public 
  MessageFormatException(String reason) {
    super(reason);
  }

}
