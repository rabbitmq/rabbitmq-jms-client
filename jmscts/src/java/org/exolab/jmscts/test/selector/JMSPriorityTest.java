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
 * $Id: JMSPriorityTest.java,v 1.4 2004/02/03 21:52:11 tanderson Exp $
 */
package org.exolab.jmscts.test.selector;

import junit.framework.Test;

import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests selectors containing <code>JMSPriority</code>
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $
 * @see AbstractSelectorTestCase
 * @jmscts.message TextMessage
 */
public class JMSPriorityTest extends AbstractSelectorTestCase {

    /**
     * Create an instance of this class for a specific test case
     *
     * @param name the name of test case
     */
    public JMSPriorityTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(JMSPriorityTest.class);
    }

    /**
     * Verifies that messages can be selected on JMSPriority, using the
     * selector <code>JMSPriority &lt;&gt; 0</code>. This should select
     * all messages
     *
     * @jmscts.requirement selector.identifier.header
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testJMSPriority() throws Exception {
        checkSelector("JMSPriority <> 0", true);
    }

    /**
     * Verifies that JMS identifier names are case sensitive, using the
     * selector <code>jmspriority between 1 and 9</code>.
     * This shouldn't select any messages.
     *
     * @jmscts.requirement selector.identifier.case
     * @jmscts.requirement selector.expression
     * @throws Exception for any error
     */
    public void testJMSPriorityCase() throws Exception {
        checkSelector("jmspriority between 1 and 9", false);
    }

    /**
     * Verifies that the selector <code>JMSPriority &gt;= '0'</code>
     * throws InvalidSelectorException
     *
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testInvalid1() throws Exception {
        checkInvalidSelector("JMSPriority >= '0'");
    }

    /**
     * Verifies that the selector <code>'0' &lt;= JMSPriority</code>
     * throws InvalidSelectorException
     *
     * @jmscts.requirement selector.datetime
     * @jmscts.requirement selector.validation
     * @throws Exception for any error
     */
    public void testInvalid2() throws Exception {
        checkInvalidSelector("'0' <= JMSPriority");
    }

}
