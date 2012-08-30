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
 * $Id: BetweenOperatorTest.java,v 1.5 2004/02/03 21:52:11 tanderson Exp $
 */
package org.exolab.jmscts.test.selector;

import java.util.HashMap;

import junit.framework.Test;

import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests selector containing the BETWEEN operator.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.5 $
 * @see AbstractSelectorTestCase
 * @jmscts.message BytesMessage
 */
public class BetweenOperatorTest extends AbstractSelectorTestCase {

    /**
     * Message properties
     */
    private static final HashMap<String, Integer> PROPERTIES = new HashMap<String, Integer>();


    /**
     * Create an instance of this class for a specific test case
     *
     * @param name the name of test case
     */
    public BetweenOperatorTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(BetweenOperatorTest.class);
    }

    /**
     * Verifies that the selector <code>17 between 16 and 18</code> selects
     * all messages
     *
     * @jmscts.requirement selector.operator.between
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testBetween1() throws Exception {
        checkSelector("17 between 16 and 18", true);
    }

    /**
     * Verifies that the selector <code>17 between 18 and 19</code> selects
     * no messages
     *
     * @jmscts.requirement selector.operator.between
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testBetween2() throws Exception {
        checkSelector("17 between 18 and 19", false);
    }

    /**
     * Verifies that the selector <code>17 Between 17 And 17</code> selects
     * all messages
     *
     * @jmscts.requirement selector.operator.between
     * @jmscts.requirement selector.expression
     * @jmscts.requirement selector.reservedwords.case
     * @throws Exception for any error
     */
    public void testBetween3() throws Exception {
        checkSelector("17 Between 17 And 17", true);
    }

    /**
     * Verifies that the selector <code>17 between 4 * 4 and 10 + 8</code>
     * selects all messages
     *
     * @jmscts.requirement selector.operator.between
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testBetween4() throws Exception {
        checkSelector("17 between 4 * 4 and 10 + 8", true);
    }

    /**
     * Verifies that the selector <code>17 between 4 * 5 and 10 + 12</code>
     * selects no messages
     *
     * @jmscts.requirement selector.operator.between
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testBetween5() throws Exception {
        checkSelector("17 between 4 * 5 and 10 + 12", false);
    }

    /**
     * Verifies that the selector <code>two between one and three</code>
     * selects all messages, where one, two and three are integer properties
     * with with corresponding values
     *
     * @jmscts.requirement selector.operator.between
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testBetween6() throws Exception {
        checkSelector("two between one and three", true, PROPERTIES);
    }

    /**
     * Verifies that the selector <code>17 not between 18 and 19</code> selects
     * all messages
     *
     * @jmscts.requirement selector.operator.between
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testNotBetween1() throws Exception {
        checkSelector("17 not between 18 and 19", true);
    }

    /**
     * Verifies that the selector <code>17 not between 16 and 18</code> selects
     * no messages
     *
     * @jmscts.requirement selector.operator.between
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testNotBetween2() throws Exception {
        checkSelector("17 not between 16 and 18", false);
    }

    /**
     * Verifies that the selector <code>17 not between 17 and 17</code> selects
     * no messages
     *
     * @jmscts.requirement selector.operator.between
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testNotBetween3() throws Exception {
        checkSelector("17 NOT between 17 and 17", false);
    }

    /**
     * Verifies that the selector <code>17 Not Between 4 * 5 And 20 / 1</code>
     * selects all messages
     *
     * @jmscts.requirement selector.operator.between
     * @jmscts.requirement selector.expression
     * @jmscts.requirement selector.reservedwords.case
     * @throws Exception for any error
     */
    public void testNotBetween4() throws Exception {
        checkSelector("17 Not Between 4 * 5 And 20 / 1", true);
    }

    /**
     * Verifies that the selector <code>two not between one and three</code>
     * selects all messages, where one, two and three are integer properties
     * with with corresponding values
     *
     * @jmscts.requirement selector.operator.between
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testNotBetween5() throws Exception {
        checkSelector("two not between one and three", false, PROPERTIES);
    }

    /**
     * Verifies that the selector <code>dummy between 1 and 10</code> selects
     * no messages, for the unset property 'dummy'
     *
     * @jmscts.requirement selector.operator.between
     * @jmscts.requirement selector.expression
     * @jmscts.requirement selector.values.null
     * @throws Exception for any error
     */
    public void testUnsetProperty1() throws Exception {
        checkSelector("dummy between 1 and 10", false);
    }

    /**
     * Verifies that the selector <code>1 between dummy and 10</code> selects
     * no messages, for the unset property 'dummy'
     *
     * @jmscts.requirement selector.operator.between
     * @jmscts.requirement selector.expression
     * @jmscts.requirement selector.values.null
     * @throws Exception for any error
     */
    public void testUnsetProperty2() throws Exception {
        checkSelector("1 between dummy and 10", false);
    }

    /**
     * Verifies that the selector <code>1 between 0 and dummy</code> selects
     * no messages, for the unset property 'dummy'
     *
     * @jmscts.requirement selector.operator.between
     * @jmscts.requirement selector.expression
     * @jmscts.requirement selector.values.null
     * @throws Exception for any error
     */
    public void testUnsetProperty3() throws Exception {
        checkSelector("1 between 0 and dummy", false);
    }

    /**
     * Verifies that the selector <code>two between '1' and '3'</code>
     * throws InvalidSelectorException
     *
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testInvalid1() throws Exception {
        checkInvalidSelector("two between '1' and '3'");
    }

    /**
     * Verifies that the selector <code>one between false and true</code>
     * throws InvalidSelectorException
     *
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testInvalid2() throws Exception {
        checkInvalidSelector("one between false and true");
    }

    /**
     * Verifies that the selector <code>b' between 'a' and 'c'</code>
     * throws InvalidSelectorException
     *
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testInvalid3() throws Exception {
        checkInvalidSelector("'b' between 'a' and 'c'");
    }

    /**
     * Verifies that the selector <code>between 1 and 3</code>
     * throws InvalidSelectorException
     *
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testInvalid4() throws Exception {
        checkInvalidSelector("between 1 and 3");
    }

    /**
     * Verifies that the selector <code>not between 1 and 3</code>
     * throws InvalidSelectorException
     *
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testInvalid5() throws Exception {
        checkInvalidSelector("not between 1 and 3");
    }

    /**
     * Verifies that the selector <code>2 between 1, 3</code>
     * throws InvalidSelectorException
     *
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testInvalid6() throws Exception {
        checkInvalidSelector("2 between 1, 3");
    }

    /**
     * Verifies that the selector <code>2 between 1 and</code>
     * throws InvalidSelectorException
     *
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testInvalid7() throws Exception {
        checkInvalidSelector("2 between 1 and");
    }

    /**
     * Verifies that the selector <code>2 between and 3</code>
     * throws InvalidSelectorException
     *
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testInvalid8() throws Exception {
        checkInvalidSelector("2 between and 3");
    }

    /**
     * Verifies that the selector <code>JMSMessageID between 1 and 10</code>
     * throws InvalidSelectorException
     *
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testInvalid9() throws Exception {
        checkInvalidSelector("JMSMessageID between 1 and 10");
    }

    static {
        PROPERTIES.put("one", new Integer(1));
        PROPERTIES.put("two", new Integer(2));
        PROPERTIES.put("three", new Integer(3));
    }

}
