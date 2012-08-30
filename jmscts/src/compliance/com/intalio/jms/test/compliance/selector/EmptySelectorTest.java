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
 *    please contact jima@intalio.com.
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
 * Copyright 2001, 2003 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: EmptySelectorTest.java,v 1.3 2003/05/04 14:12:38 tanderson Exp $
 */
package org.exolab.jmscts.test.selector;

import javax.jms.Destination;
import javax.jms.InvalidSelectorException;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.JMSTestRunner;
import org.exolab.jmscts.core.MessagePopulator;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessageTypes;
import org.exolab.jmscts.core.SessionHelper;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class test selector functionality
 * <p>
 * It covers the following requirements:
 * <ul>
 *   <li>selector.null</li>
 *   <li>selector.empty</li>
 * </ul>
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.3 $
 * @see AbstractSendReceiveTestCase
 */
public class EmptySelectorTest extends AbstractSendReceiveTestCase {

    /**
     * Requirements covered by this test case
     */
    private static final String[][] REQUIREMENTS = {
        {"testNull", "selector.null"},
        {"testEmpty", "selector.empty"}};

    /**
     * The destination used by this test case
     */
    private static final String DESTINATION = "EmptySelectorTest";

    /**
     * Create an instance of this class for a specific test case, testing 
     * against all delivery types
     * 
     * @param name the name of test case
     */
    public EmptySelectorTest(String name) {
        super(name, MessageTypes.TEXT, REQUIREMENTS);
    }

    /**
     * The main line used to execute this test
     */
    public static void main(String[] args) {
        JMSTestRunner test = new JMSTestRunner(suite(), args);
        junit.textui.TestRunner.run(test);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by 
     * {@link JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(EmptySelectorTest.class);
    }

    /**
     * Get the message populator. This implementation always returns null
     *
     * @return null
     */
    public MessagePopulator getMessagePopulator() {
        return null;
    }

    /**
     * Returns the list of destination names used by this test case. These
     * are used to pre-create destinations prior to running the test case.
     *
     * @return the list of destinations used by this test case
     */
    public String[] getDestinations() {
        return new String[] {DESTINATION};
    }

    /**
     * Test that consumer creation operations accept null as a valid selector
     * This covers requirements:
     * <ul>
     *   <li>selector.null</li>
     * </ul>
     * TODO - test QueueBrowser
     * @throws Exception for any error
     */
    public void testNull() throws Exception {
        MessageReceiver receiver = SessionHelper.createReceiver(
            getContext(), getDestination(DESTINATION), null, false);
        receiver.remove();
    }

    /**
     * Test that consumer creation operations accept an empty string as a 
     * valid selector
     * This covers requirements:
     * <ul>
     *   <li>selector.empty</li>
     * </ul>
     * @throws Exception for any error
     */
    public void testEmpty() throws Exception {
        MessageReceiver receiver = SessionHelper.createReceiver(
            getContext(), getDestination(DESTINATION), "", false);
        receiver.remove();
    }
   
} //-- EmptySelectorTest
