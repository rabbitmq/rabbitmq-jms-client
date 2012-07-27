/*
 * @(#)MessageListener.java	1.14 02/04/09
 *
 * Copyright 1997-2002 Sun Microsystems, Inc. All Rights Reserved.
 *
 *  SUN PROPRIETARY/CONFIDENTIAL.
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */


package javax.jms;


/** A <CODE>MessageListener</CODE> object is used to receive asynchronously 
  * delivered messages.
  *
  * <P>Each session must insure that it passes messages serially to the
  * listener. This means that a listener assigned to one or more consumers
  * of the same session can assume that the <CODE>onMessage</CODE> method 
  * is not called with the next message until the session has completed the 
  * last call.
  *
  * @version     1.0 - 13 March 1998
  * @author      Mark Hapner
  * @author      Rich Burridge
  */

public interface MessageListener {

    /** Passes a message to the listener.
      *
      * @param message the message passed to the listener
      */

    void 
    onMessage(Message message);
}
