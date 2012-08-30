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
 * $Id: StringTest.java,v 1.5 2004/02/03 21:52:11 tanderson Exp $
 */
package org.exolab.jmscts.test.selector;

import java.util.HashMap;

import junit.framework.Test;

import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests selectors containing string literals and objects
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.5 $
 * @see AbstractSelectorTestCase
 * @jmscts.message TextMessage
 */
public class StringTest extends AbstractSelectorTestCase {

    /**
     * Message properties
     */
    private static final HashMap<String, String> PROPERTIES = new HashMap<String, String>();


    /**
     * Create an instance of this class for a specific test case
     *
     * @param name the name of test case
     */
    public StringTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(StringTest.class);
    }

    /**
     * Verifies that the selector <code>'abc' = 'abc'</code> selects
     * all messages
     *
     * @jmscts.requirement selector.literal.string
     * @jmscts.requirement selector.comparison.string
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testEquals() throws Exception {
        checkSelector("'abc' = 'abc'", true);
    }

    /**
     * Verifies that the selector <code>Country = 'France'</code> selects
     * all messages, when the string property 'Country' is set, with
     * value 'France'
     *
     * @jmscts.requirement selector.literal.string
     * @jmscts.requirement selector.comparison.string
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testEqualsProperty() throws Exception {
        checkSelector("Country = 'France'", true, PROPERTIES);
    }

    /**
     * Verifies that string literals are case sensitive, using the selector
     * <code>'abc' = 'ABC'</code>. This shouldn't select any messages.
     *
     * @jmscts.requirement selector.literal.string
     * @jmscts.requirement selector.comparison.string
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testCaseComparison1() throws Exception {
        checkSelector("'abc' = 'ABC'", false);
    }

    /**
     * Verifies that string literals are case sensitive, using the selector
     * <code>'abc' &lt;&gt; 'ABC'</code>. This should select all messages.
     *
     * @jmscts.requirement selector.literal.string
     * @jmscts.requirement selector.comparison.string
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testCaseComparison2() throws Exception {
        checkSelector("'abc' <> 'ABC'", true);
    }

    /**
     * Verifies that string literals are case sensitive when compared
     * with string properties, using the selector
     * <code>Country = 'france'</code>, and the string property 'Country',
     * with  value 'France'. This should select no messages.
     *
     * @jmscts.requirement selector.literal.string
     * @jmscts.requirement selector.comparison.string
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testCaseComparison3() throws Exception {
        checkSelector("Country = 'france'", false, PROPERTIES);
    }

    /**
     * Verifies that string literals can contain embedded quotes, using
     * the selector <code>'it''s' = 'it''s'</code>. This should select
     * all messages
     *
     * @jmscts.requirement selector.literal.string
     * @jmscts.requirement selector.comparison.string
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testCheckSingleQuotes1() throws Exception {
        checkSelector("'it''s' = 'it''s'", true);
    }

    /**
     * Verifies that the quotes are preserved in string literals with embedded
     * quotes, using the selector <code>'it''s' = 'its'</code>.
     * This should select no messages
     *
     * @jmscts.requirement selector.literal.string
     * @jmscts.requirement selector.comparison.string
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testCheckSingleQuotes2() throws Exception {
        checkSelector("'it''s' = 'its'", false);
    }

    /**
     * Verifies that the selector <code>'abc' &lt; 'abc'</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.comparison.string
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testLessThan1() throws Exception {
        checkInvalidSelector("'abc' < 'abc'");
    }

    /**
     * Verifies that the selector <code>dummy &lt; 'abc'</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.comparison.string
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testLessThan2() throws Exception {
        checkInvalidSelector("dummy < 'abc'");
    }

    /**
     * Verifies that the selector <code>'abc' &lt; dummy</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.comparison.string
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testLessThan3() throws Exception {
        checkInvalidSelector("'abc' < dummy");
    }

    /**
     * Verifies that the selector <code>'abc' &gt; 'abc'</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.comparison.string
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testGreaterThan1() throws Exception {
        checkInvalidSelector("'abc' > 'abc'");
    }

    /**
     * Verifies that the selector <code>dummy &gt; 'abc'</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.comparison.string
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testGreaterThan2() throws Exception {
        checkInvalidSelector("dummy > 'abc'");
    }

    /**
     * Verifies that the selector <code>'abc' &gt; dummy</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.comparison.string
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testGreaterThan3() throws Exception {
        checkInvalidSelector("'abc' < dummy");
    }

    /**
     * Verifies that the selector <code>'abc' &lt;= 'abc'</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.comparison.string
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testLessEquals1() throws Exception {
        checkInvalidSelector("'abc' <= 'abc'");
    }

    /**
     * Verifies that the selector <code>dummy &lt;= 'abc'</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.comparison.string
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testLessEquals2() throws Exception {
        checkInvalidSelector("dummy <= 'abc'");
    }

    /**
     * Verifies that the selector <code>'abc' &lt;= dummy</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.comparison.string
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testLessEquals3() throws Exception {
        checkInvalidSelector("'abc' <= dummy");
    }

    /**
     * Verifies that the selector <code>'abc' &gt;= 'abc'</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.comparison.string
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testGreaterEquals1() throws Exception {
        checkInvalidSelector("'abc' >= 'abc'");
    }

    /**
     * Verifies that the selector <code>dummy &gt;= 'abc'</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.comparison.string
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testGreaterEquals2() throws Exception {
        checkInvalidSelector("dummy >= 'abc'");
    }

    /**
     * Verifies that the selector <code>'abc' &gt;= dummy</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.comparison.string
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testGreaterEquals3() throws Exception {
        checkInvalidSelector("'abc' >= dummy");
    }

    /**
     * Verifies that the selector <code>'abc'</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testInvalid1() throws Exception {
        checkInvalidSelector("'abc'");
    }

    /**
     * Verifies that the selector <code>'abc' = 'abc</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testInvalid2() throws Exception {
        checkInvalidSelector("'abc' = 'abc");
    }

    /**
     * Verifies that the selector <code>'abc' = abc'</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testInvalid3() throws Exception {
        checkInvalidSelector("'abc' = abc'");
    }

    /**
     * Verifies that the selector <code>'abc = 'abc'</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testInvalid4() throws Exception {
        checkInvalidSelector("'abc = 'abc'");
    }

    /**
     * Verifies that the selector <code>'abc'''' = 'abc'</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testInvalid5() throws Exception {
        checkInvalidSelector("'abc'''' = 'abc'");
    }

    /**
     * Verifies that the selector <code>"abc" = "abc"</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testInvalid6() throws Exception {
        checkInvalidSelector("\"abc\" = \"abc\"");
    }

    static {
        PROPERTIES.put("Country", "France");
    }

}
