/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact tma@netspace.net.au.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001-2004 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: AbstractSelectorTestCase.java,v 1.8 2005/06/16 06:40:28 tanderson Exp $
 */
package org.exolab.jmscts.test.selector;

import java.util.List;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.InvalidSelectorException;
import javax.jms.Message;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.MessagingHelper;
import org.exolab.jmscts.core.MessagePopulator;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.PropertyPopulator;
import org.exolab.jmscts.core.SessionHelper;
import org.exolab.jmscts.core.TestContext;


/**
 * This class enables selector test cases to be run for each JMS message,
 * session and synchronous/asynchronous processing type.
 *
 * @version     $Revision: 1.8 $ $Date: 2005/06/16 06:40:28 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         AbstractSendReceiveTestCase
 */
public abstract class AbstractSelectorTestCase
    extends AbstractSendReceiveTestCase {

    /**
     * The destination used by this test case
     */
    private static final String DESTINATION = "SelectorTest";


    /**
     * Construct an instance of this class for a specific test case
     *
     * @param name the name of test case
     */
    public AbstractSelectorTestCase(String name) {
        super(name);
    }

    /**
     * Get the message populator. This implementation always returns null
     *
     * @return null
     */
    @Override
    public MessagePopulator getMessagePopulator() {
        return null;
    }

    /**
     * Returns the list of destination names used by this test case. These
     * are used to pre-create destinations prior to running the test case.
     *
     * @return the list of destinations used by this test case
     */
    @Override
    public String[] getDestinations() {
        return new String[] {DESTINATION};
    }

    /**
     * Verifies that the specified selector correctly selects messages
     *
     * @param selector the selector to test
     * @param selectsAll if <code>true</code>, the selector is expected to
     * select all messages; if <code>false</code>, the selector is expected
     * to select no messages
     * @throws Exception for any error
     */
    protected void checkSelector(String selector, boolean selectsAll)
        throws Exception {
        checkSelector(selector, selectsAll, null);
    }

    /**
     * Verifies that the specified selector correctly selects messages
     *
     * @param selector the selector to test
     * @param selectsAll if <code>true</code>, the selector is expected to
     * select all messages; if <code>false</code>, the selector is expected
     * to select no messages
     * @param properties a map of property names to property values, to be
     * populated on sent messages. May be <code>null</code>
     * @throws Exception for any error
     */
    protected void checkSelector(String selector, boolean selectsAll,
                                 Map<?, ?> properties)
        throws Exception {
        final int count = 5; // the number of messages to send
        TestContext context = getContext();
        Message message = context.getMessage();
        Destination destination = getDestination(DESTINATION);
        MessageReceiver receiver = null;

        try {
            receiver = SessionHelper.createReceiver(
                context, destination, selector, false);
            if (properties != null) {
                PropertyPopulator populator = new PropertyPopulator(
                    properties);
                populator.populate(message);
            }
            MessagingHelper.send(context, message, destination, count);
            int expected = (selectsAll) ? count : 0;
            long timeout = context.getMessagingBehaviour().getTimeout();
            List<?> result = receiver.receive(expected, timeout);
            int received = (result == null) ? 0 : result.size();
            if (received != expected) {
                fail("Expected " + expected + " messages for selector=\""
                     + selector + "\" but got " + received);
            }
            acknowledge(result);
        } catch (InvalidSelectorException exception) {
            fail("InvalidSelectorException thrown for valid selector=\""
                 + selector + "\"");
        } finally {
            if (receiver != null) {
                receiver.remove();
            }
        }
    }

    /**
     * Verifies that attempting to construct a receiver for the supplied
     * selector throws <code>InvalidSelectorException</code>
     *
     * @param selector the selector to test
     * @throws Exception for any error
     */
    protected void checkInvalidSelector(String selector) throws Exception {
        TestContext context = getContext();
        Destination destination = getDestination(DESTINATION);
        MessageReceiver receiver = null;
        try {
            receiver = SessionHelper.createReceiver(
                context, destination, selector, false);
            fail("Expected InvalidSelectorException to be thrown for"
                 + " selector=\"" + selector + "\"");
        } catch (InvalidSelectorException expected) {
            // the expected behaviour
        } catch (Exception exception) {
            String message =
                "Expected InvalidSelectorException to be thrown for selector="
                + "\"" + selector + "\", but threw "
                + exception.getClass().getName() + ", message="
                + exception.getMessage() + " instead";
            fail(message);
        } finally {
            if (receiver != null) {
                receiver.remove();
            }
        }
    }

}
