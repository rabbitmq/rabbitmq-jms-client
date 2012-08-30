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
 * $Id: AndOperatorTest.java,v 1.4 2004/02/03 21:52:11 tanderson Exp $
 */
package org.exolab.jmscts.test.selector;

import junit.framework.Test;

import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests selectors containing the AND operator.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $
 * @see AbstractSelectorTestCase
 * @jmscts.message Message
 */
public class AndOperatorTest extends AbstractSelectorTestCase {

    /**
     * Create an instance of this class for a specific test case, testing
     * against all delivery types
     *
     * @param name the name of test case
     */
    public AndOperatorTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(AndOperatorTest.class);
    }

    /**
     * Verifies that the selector <code>true and true</code> selects
     * all messages
     *
     * @jmscts.requirement selector.operator.and
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testAnd1() throws Exception {
        checkSelector("true and true", true);
    }

    /**
     * Verifies that the selector <code>true and false</code> selects
     * no messages
     *
     * @jmscts.requirement selector.operator.and
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testAnd2() throws Exception {
        checkSelector("true and false", false);
    }

    /**
     * Verifies that the selector <code>false and true</code> selects
     * no messages
     *
     * @jmscts.requirement selector.operator.and
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testAnd3() throws Exception {
        checkSelector("false and true", false);
    }

    /**
     * Verifies that the selector <code>false and false</code> selects
     * no messages
     *
     * @jmscts.requirement selector.operator.and
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testAnd4() throws Exception {
        checkSelector("false and false", false);
    }

    /**
     * Verifies that the selector <code>true AND true</code> selects
     * all messages
     *
     * @jmscts.requirement selector.operator.and
     * @jmscts.requirement selector.expression
     * @jmscts.requirement selector.reservedwords.case
     * @throws Exception for any error
     */
    public void testAndCase() throws Exception {
        checkSelector("true AND true", true);
    }

    /**
     * Verifies that the selector <code>true and dummy</code> selects
     * no messages, for the unset property 'dummy'
     *
     * @jmscts.requirement selector.operator.and
     * @jmscts.requirement selector.expression
     * @jmscts.requirement selector.values.null
     * @throws Exception for any error
     */
    public void testUnsetProperty1() throws Exception {
        checkSelector("true and dummy", false);
    }

    /**
     * Verifies that the selector <code>false and dummy</code> selects
     * no messages, for the unset property 'dummy'
     *
     * @jmscts.requirement selector.operator.and
     * @jmscts.requirement selector.expression
     * @jmscts.requirement selector.values.null
     * @throws Exception for any error
     */
    public void testUnsetProperty2() throws Exception {
        checkSelector("false and dummy", false);
    }

    /**
     * Verifies that the selector <code>dummy and true</code> selects
     * no messages, for the unset property 'dummy'
     *
     * @jmscts.requirement selector.operator.and
     * @jmscts.requirement selector.expression
     * @jmscts.requirement selector.values.null
     * @throws Exception for any error
     */
    public void testUnsetProperty3() throws Exception {
        checkSelector("dummy and true", false);
    }

    /**
     * Verifies that the selector <code>dummy and false</code> selects
     * no messages, for the unset property 'dummy'
     *
     * @jmscts.requirement selector.operator.and
     * @jmscts.requirement selector.expression
     * @jmscts.requirement selector.values.null
     * @throws Exception for any error
     */
    public void testUnsetProperty4() throws Exception {
        checkSelector("dummy and false", false);
    }

    /**
     * Verifies that the selector <code>dummy and dummy</code> selects
     * no messages, for the unset property 'dummy'
     *
     * @jmscts.requirement selector.operator.and
     * @jmscts.requirement selector.expression
     * @jmscts.requirement selector.values.null
     * @throws Exception for any error
     */
    public void testUnsetProperty5() throws Exception {
        checkSelector("dummy and dummy", false);
    }

    /**
     * Verifies that the selector <code>and</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testInvalidAnd1() throws Exception {
        checkInvalidSelector("and");
    }

    /**
     * Verifies that the selector <code>true and</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testInvalidAnd2() throws Exception {
        checkInvalidSelector("true and");
    }

    /**
     * Verifies that the selector <code>false and</code> throws
     * InvalidSelectorException
     *
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testInvalidAnd3() throws Exception {
        checkInvalidSelector("and true");
    }

}
