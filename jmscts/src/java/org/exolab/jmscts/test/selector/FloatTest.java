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
 * Copyright 2003-2004 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id: FloatTest.java,v 1.7 2004/02/03 21:52:11 tanderson Exp $
 */
package org.exolab.jmscts.test.selector;

import junit.framework.Test;

import java.util.HashMap;

import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests selectors containing floating point literals and objects.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.7 $
 * @see AbstractSelectorTestCase
 * @jmscts.message MapMessage
 */
public class FloatTest extends AbstractSelectorTestCase {

    /**
     * Message properties
     */
    private static final HashMap<String, Number> PROPERTIES = new HashMap<String, Number>();


    /**
     * Create an instance of this class for a specific test case, testing
     * against all delivery types
     *
     * @param name the name of test case
     */
    public FloatTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(FloatTest.class);
    }

    /**
     * Verifies that the selector <code>0.0 = 0.0</code> selects all
     * messages
     *
     * @jmscts.requirement selector.literal.approxnumeric
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testEquals1() throws Exception {
        checkSelector("0.0 = 0.0", true);
    }

    /**
     * Verifies that the selector <code>0.0 = 1.0</code> selects no
     * messages
     *
     * @jmscts.requirement selector.literal.approxnumeric
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testEquals2() throws Exception {
        checkSelector("0.0 = 1.0", false);
    }

    /**
     * Verifies that the selector <code>0.2 = 0.2</code> selects all
     * messages
     *
     * @jmscts.requirement selector.literal.approxnumeric
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testEquals3() throws Exception {
        checkSelector("0.2 = 0.2", true);
    }

    /**
     * Verifies that the selector <code>0.2 = 0.0</code> selects no
     * messages
     *
     * @jmscts.requirement selector.literal.approxnumeric
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testEquals4() throws Exception {
        checkSelector("0.2 = 0.0", false);
    }

    /**
     * Verifies that the selector <code>92d = 92</code> selects all messages
     *
     * @jmscts.requirement selector.literal.approxnumeric
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testEquals5() throws Exception {
        checkSelector("92d = 92", true);
    }

    /**
     * Verifies that the selector <code>93f = 93</code> selects all messages
     *
     * @jmscts.requirement selector.literal.approxnumeric
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testEquals6() throws Exception {
        checkSelector("93f = 93", true);
    }

    /**
     * Verifies that the selector <code>1.0 &lt;&gt; 2.0</code> selects all
     * messages
     *
     * @jmscts.requirement selector.literal.approxnumeric
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testNotEquals1() throws Exception {
        checkSelector("1.0 <> 2.0", true);
    }

    /**
     * Verifies that the selector <code>1.0 &lt;&gt; 1.0</code> selects no
     * messages
     *
     * @jmscts.requirement selector.literal.approxnumeric
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testNotEquals2() throws Exception {
        checkSelector("1.0 <> 1.0", false);
    }

    /**
     * Verifies that the selector <code>1.0 &lt;&gt; 1.0</code> selects all
     * messages
     *
     * @jmscts.requirement selector.literal.approxnumeric
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testNotEquals3() throws Exception {
        checkSelector("1.0 <> 1.1", true);
    }

    /**
     * Verifies that the selector <code>1.0 &lt; 2.0</code> selects all
     * messages
     *
     * @jmscts.requirement selector.literal.approxnumeric
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testLessThan1() throws Exception {
        checkSelector("1.0 < 2.0", true);
    }

    /**
     * Verifies that the selector <code>2.0 &lt; 1.0</code> selects no
     * messages
     *
     * @jmscts.requirement selector.literal.approxnumeric
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testLessThan2() throws Exception {
        checkSelector("2.0 < 1.0", false);
    }

    /**
     * Verifies that the selector <code>2.0 &gt; 1.0</code> selects all
     * messages
     *
     * @jmscts.requirement selector.literal.approxnumeric
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testGreaterThan1() throws Exception {
        checkSelector("2.0 > 1.0", true);
    }

    /**
     * Verifies that the selector <code>1.0 &gt; 2.0</code> selects no
     * messages
     *
     * @jmscts.requirement selector.literal.approxnumeric
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testGreaterThan2() throws Exception {
        checkSelector("1.0 > 2.0", false);
    }

    /**
     * Verifies that the selector <code>1.0 &lt;= 2.0</code> selects all
     * messages
     *
     * @jmscts.requirement selector.literal.approxnumeric
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testLessEquals1() throws Exception {
        checkSelector("1.0 <= 2.0", true);
    }

    /**
     * Verifies that the selector <code>2.0 &lt;= 1.0</code> selects no
     * messages
     *
     * @jmscts.requirement selector.literal.approxnumeric
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testLessEquals2() throws Exception {
        checkSelector("2.0 <= 1.0", false);
    }

    /**
     * Verifies that the selector <code>2.0 &gt;= 1.0</code> selects all
     * messages
     *
     * @jmscts.requirement selector.literal.approxnumeric
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testGreaterEquals1() throws Exception {
        checkSelector("2.0 >= 1.0", true);
    }

    /**
     * Verifies that the selector <code>1.0 &gt;= 2.0</code> selects no
     * messages
     *
     * @jmscts.requirement selector.literal.approxnumeric
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testGreaterEquals2() throws Exception {
        checkSelector("1.0 >= 2.0", false);
    }

    /**
     * Verifies that the selector <code>-1.0 = -1.0</code> selects all messages
     *
     * @jmscts.requirement selector.literal.approxnumeric
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testUnaryMinus1() throws Exception {
        checkSelector("-1.0 = -1.0", true);
    }

    /**
     * Verifies that the selector <code>-1.0 = 1.0</code> selects no messages
     *
     * @jmscts.requirement selector.literal.approxnumeric
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testUnaryMinus2() throws Exception {
        checkSelector("-1.0 = 1.0", false);
    }

    /**
     * Verifies that the selector <code>--1.0 = 1.0</code> selects all messages
     *
     * @jmscts.requirement selector.literal.approxnumeric
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testUnaryMinus3() throws Exception {
        checkSelector("--1.0 = 1.0", true);
    }

    /**
     * Verifies that the selector <code>rate = 0.2</code> selects
     * all messages, when the double property 'rate' is set, with
     * value <code>0.2</code>
     *
     * @jmscts.requirement selector.literal.approxnumeric
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testEqualsProperty() throws Exception {
        checkSelector("rate = 0.2", true, PROPERTIES);
    }

    /**
     * Verifies that the selector <code>rate &lt;&gt; 0.2</code> selects
     * no messages, when the double property 'rate' is set, with
     * value <code>0.2</code>
     *
     * @jmscts.requirement selector.literal.approxnumeric
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testNotEqualsProperty() throws Exception {
        checkSelector("rate <> 0.2", false, PROPERTIES);
    }

    /**
     * Verifies that the selector <code>dummy + 10.0 = 10.0</code> selects
     * no messages, for the unset property 'dummy'
     *
     * @jmscts.requirement selector.expression
     * @jmscts.requirement selector.values.null
     * @throws Exception for any error
     */
    public void testUnsetProperty1() throws Exception {
        checkSelector("dummy + 10.0 = 10.0", false);
    }

    /**
     * Verifies that the selector <code>dummy - 10.0 = -10.0</code> selects
     * no messages, for the unset property 'dummy'
     *
     * @jmscts.requirement selector.expression
     * @jmscts.requirement selector.values.null
     * @throws Exception for any error
     */
    public void testUnsetProperty2() throws Exception {
        checkSelector("dummy - 10.0 = -10.0", false);
    }

    /**
     * Verifies that the selector <code>10.0 + dummy = 10.0</code> selects
     * no messages, for the unset property 'dummy'
     *
     * @jmscts.requirement selector.expression
     * @jmscts.requirement selector.values.null
     * @throws Exception for any error
     */
    public void testUnsetProperty3() throws Exception {
        checkSelector("10.0 + dummy = 10.0", false);
    }

    /**
     * Verifies that the selector <code>10.0 - dummy = 0.0</code> selects
     * no messages, for the unset property 'dummy'
     *
     * @jmscts.requirement selector.expression
     * @jmscts.requirement selector.values.null
     * @throws Exception for any error
     */
    public void testUnsetProperty4() throws Exception {
        checkSelector("10.0 - dummy = 0.0", false);
    }

    /**
     * Verifies that the selector <code>dummy * 10.0 = 0.0</code> selects
     * no messages, for the unset property 'dummy'
     *
     * @jmscts.requirement selector.expression
     * @jmscts.requirement selector.values.null
     * @throws Exception for any error
     */
    public void testUnsetProperty5() throws Exception {
        checkSelector("dummy * 10.0 = 0.0", false);
    }

    /**
     * Verifies that the selector <code>10.0 * dummy = 0.0</code> selects
     * no messages, for the unset property 'dummy'
     *
     * @jmscts.requirement selector.expression
     * @jmscts.requirement selector.values.null
     * @throws Exception for any error
     */
    public void testUnsetProperty6() throws Exception {
        checkSelector("10.0 * dummy = 0.0", false);
    }

    /**
     * Verifies that the selector <code>dummy / 10.0 = 0.0</code> selects
     * no messages, for the unset property 'dummy'
     *
     * @jmscts.requirement selector.expression
     * @jmscts.requirement selector.values.null
     * @throws Exception for any error
     */
    public void testUnsetProperty7() throws Exception {
        checkSelector("dummy / 10.0 = 1.0", false);
    }

    /**
     * Verifies that the selector <code>10.0 / dummy = 0.0</code> selects
     * no messages, for the unset property 'dummy'
     *
     * @jmscts.requirement selector.expression
     * @jmscts.requirement selector.values.null
     * @throws Exception for any error
     */
    public void testUnsetProperty8() throws Exception {
        checkSelector("10.0 / dummy = 1.0", false);
    }

    /**
     * Verifies that selectors can have approximate numeric literals
     * in the range <code>Double.MIN_VALUE..Double.MAX_VALUE</code>,
     * using the selector
     * <code>{min-value}={min-value} and {max-value} = {max-value}</code>
     * where {min-value} and {max-value} are the values of Double.MIN_VALUE and
     * Double.MAX_VALUE respectively. This should select all messages.
     *
     * @jmscts.requirement selector.literal.approxnumeric
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testNumericRange() throws Exception {
        checkSelector(Double.MIN_VALUE + "=" + Double.MIN_VALUE + " and "
                      + Double.MAX_VALUE + "=" + Double.MAX_VALUE, true);
    }

    /**
     * Verifies that the selector <code>10 / zero = 10 / zero</code> selects
     * all messages, when the double property 'zero' is set, with
     * value <code>0.0</code>
     *
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testDivisionByZero() throws Exception {
        checkSelector("10.0 / zero = 10.0 / zero", true, PROPERTIES);
    }

    /**
     * Verifies that the selector <code>floatNaN = floatNaN</code> selects
     * no messages, when the float property 'floatNaN' is set, with
     * value <code>Float.NaN</code> (as NaN != NaN)
     *
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testFloatNaN1() throws Exception {
        checkSelector("floatNaN = floatNaN", false, PROPERTIES);
    }

    /**
     * Verifies that the selector <code>floatNaN &lt;&gt; floatNaN</code>
     * selects all messages, when the float property 'floatNaN' is set, with
     * value <code>Float.NaN</code>
     *
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testFloatNaN2() throws Exception {
        checkSelector("floatNaN <> floatNaN", true, PROPERTIES);
    }

    /**
     * Verifies that the selector <code>doubleNaN = doubleNaN</code>
     * selects no messages, when the double property 'doubleNaN' is set,
     * with value <code>Double.NaN</code> (as NaN != NaN)
     *
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testDoubleNaN1() throws Exception {
        checkSelector("doubleNaN = doubleNaN", false, PROPERTIES);
    }

    /**
     * Verifies that the selector <code>doubleNaN &lt;&gt; doubleNaN</code>
     * selects all messages, when the double property 'doubleNaN' is set,
     * with value <code>Double.NaN</code>
     *
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testDoubleNaN2() throws Exception {
        checkSelector("doubleNaN <> doubleNaN", true, PROPERTIES);
    }

    /**
     * Verifies that the selector <code>1.0</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testInvalid1() throws Exception {
        checkInvalidSelector("1.0");
    }

    /**
     * Verifies that the selector <code>-1.0</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testInvalid2() throws Exception {
        checkInvalidSelector("-1.0");
    }

    /**
     * Verifies that the selector <code>2.0 &lt; '3.0'</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testInvalid3() throws Exception {
        checkInvalidSelector("2.0 < '3.0'");
    }

    /**
     * Verifies that the selector <code>1.0 &lt;&gt; false</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testInvalid4() throws Exception {
        checkInvalidSelector("1.0 <> false");
    }

    /**
     * Verifies that the selector <code>1a.0 = 1a.0</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testInvalid5() throws Exception {
        checkInvalidSelector("1a.0 = 1a.0");
    }

    static {
        final double rate = 0.2;
        PROPERTIES.put("rate", new Double(rate));
        PROPERTIES.put("zero", new Double(0.0));
        PROPERTIES.put("floatNaN", new Float(Float.NaN));
        PROPERTIES.put("doubleNaN", new Double(Double.NaN));
    }

}
