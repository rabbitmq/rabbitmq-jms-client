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
 * $Id: SelectorTest.java,v 1.4 2003/05/04 14:12:38 tanderson Exp $
 */
package org.exolab.jmscts.test.selector;

import java.util.List;

import javax.jms.Destination;
import javax.jms.InvalidSelectorException;
import javax.jms.Message;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractSendReceiveTestCase;
import org.exolab.jmscts.core.JMSTestRunner;
import org.exolab.jmscts.core.MessageReceiver;
import org.exolab.jmscts.core.MessageSender;
import org.exolab.jmscts.core.MessageTypes;
import org.exolab.jmscts.core.PropertyPopulator;
import org.exolab.jmscts.core.SessionHelper;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;
import org.exolab.jmscts.test.selector.types.SelectsType;

/**
 * This class tests selector functionality.
 * It covers the following requirements:
 * <ul>
 *   <li>selector.comparison.boolean</li>
 *   <li>selector.comparison.string</li>
 *   <li>selector.identifier.case</li>
 *   <li>selector.identifier.name</li>
 *   <li>selector.literal.approxnumeric</li>
 *   <li>selector.literal.boolean</li>
 *   <li>selector.literal.exactnumeric</li>
 *   <li>selector.literal.string</li>
 *   <li>selector.expression</li>
 *   <li>selector.operator.and</li>
 *   <li>selector.operator.between</li>
 *   <li>selector.operator.in</li>
 *   <li>selector.operator.is</li>
 *   <li>selector.operator.like</li>
 *   <li>selector.operator.not</li>
 *   <li>selector.operator.or</li>
 *   <li>selector.property.conversion</li>
 *   <li>selector.reservedwords</li>
 *   <li>selector.validation</li>
 *   <li>selector.whitespace</li>
 * </ul>
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $
 * @see AbstractSelectorTestCase
 * @see SelectorTestRunner
 */
public class SelectorTest extends AbstractSelectorTestCase {

    /**
     * The destination used by this test case
     */
    private static final String DESTINATION = "SelectorTest";

    /**
     * The list of selector test case documents paths as resources
     */
    private static final String[] RESOURCE_PATHS = {
        "/org/exolab/jmscts/test/selector/and.xml",
        "/org/exolab/jmscts/test/selector/between.xml",
        "/org/exolab/jmscts/test/selector/boolean.xml",
        "/org/exolab/jmscts/test/selector/booleancomparison.xml",
        "/org/exolab/jmscts/test/selector/booleanliteral.xml",
        "/org/exolab/jmscts/test/selector/brackets.xml",
        "/org/exolab/jmscts/test/selector/case.xml",
        "/org/exolab/jmscts/test/selector/conversion.xml",
        "/org/exolab/jmscts/test/selector/datetime.xml",
        "/org/exolab/jmscts/test/selector/expression.xml",
        "/org/exolab/jmscts/test/selector/float.xml",
        "/org/exolab/jmscts/test/selector/floatliteral.xml",
        "/org/exolab/jmscts/test/selector/header.xml",
        "/org/exolab/jmscts/test/selector/identifier.xml",
        "/org/exolab/jmscts/test/selector/in.xml",
        "/org/exolab/jmscts/test/selector/integer.xml",
        "/org/exolab/jmscts/test/selector/integerliteral.xml",
        "/org/exolab/jmscts/test/selector/is.xml",
        "/org/exolab/jmscts/test/selector/jmsprefixed.xml",
        "/org/exolab/jmscts/test/selector/like.xml",
        "/org/exolab/jmscts/test/selector/not.xml",
        "/org/exolab/jmscts/test/selector/or.xml",
        "/org/exolab/jmscts/test/selector/precedence.xml",
        "/org/exolab/jmscts/test/selector/promotion.xml",
        "/org/exolab/jmscts/test/selector/reserved.xml",
        "/org/exolab/jmscts/test/selector/string.xml",
        "/org/exolab/jmscts/test/selector/stringcomparison.xml",
        "/org/exolab/jmscts/test/selector/stringliteral.xml",
        "/org/exolab/jmscts/test/selector/typecomparison.xml",
        "/org/exolab/jmscts/test/selector/whitespace.xml"};

    /**
     * Create an instance of this class for a specific test case, testing 
     * against all delivery types
     * 
     * @param name the name of test case
     */
    public SelectorTest(String name) {
        super(name, RESOURCE_PATHS, MessageTypes.TEXT, null);
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
        return TestCreator.createSessionTest(new SelectorTestRunner(
            SelectorTest.class));
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

    public void testSelector() throws Exception {
        final int count = 5; // the number of messages to send
        TestContext context = getContext();
        Message message = context.getMessage();
        Destination destination = getDestination(DESTINATION);
        MessageReceiver receiver = null;
        MessageSender sender = null;
        Selector selector = getSelector();

        try {
            receiver = SessionHelper.createReceiver(
                context, destination, selector.getExpression(), false);
            if (!selector.getValid()) {
                fail("Expected InvalidSelectorException to be thrown for" +
                     " selector=\"" + selector.getExpression() + "\"");
            }

            sender = SessionHelper.createSender(context, destination);
            if (selector.getPropertyCount() != 0) {
                PropertyPopulator populator = new PropertyPopulator(
                    selector.getProperty());
                populator.populate(message);
            }
            sender.send(message, count);
            int expected = (selector.getSelects() == SelectsType.ALL) ? 
                    count : 0;
            long timeout = context.getMessagingBehaviour().getTimeout();
            List result = receiver.receive(expected, timeout);
            int received = (result == null) ? 0 : result.size();
            if (received != expected) {
                fail("Expected " + expected + " messages for selector=\"" + 
                     selector.getExpression() + "\" but got " + received);
            }
        } catch (InvalidSelectorException exception) {
            if (selector.getValid()) {
                fail("InvalidSelectorException thrown for valid selector=\""
                     + selector.getExpression() + "\"");
            }
        } finally {
            if (sender != null) {
                sender.close();
            }
            if (receiver != null) {
                receiver.remove();
            }
        }
    }

    /**
     * Returns the requirement identifiers covered by a test case
     *
     * @param test the name of the test method
     * @return a list of requirement identifiers covered by the test, or null 
     * if the test doesn't cover any requirements
     */
    public String[] getRequirements(String test) {
        return getSelector().getRequirementId();
    }
   
} //-- SelectorTest
