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
 * $Id: JMSTestCase.java,v 1.3 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import org.apache.log4j.Logger;

import junit.framework.TestCase;


/**
 * This class is the base class for generic test cases that can be
 * to be run for different connection factories, connections, and sessions.
 *
 * @version     $Revision: 1.3 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         TestContext
 * @see         ProviderTestRunner
 * @see         ConnectionTestRunner
 * @see         SessionTestRunner
 * @see         MessageTestRunner
 * @see         SendReceiveTestRunner
 */
public abstract class JMSTestCase extends TestCase implements JMSTest {

    /**
     * The logger
     */
    @SuppressWarnings("unused")
    private static final Logger log
        = Logger.getLogger(JMSTestCase.class.getName());


    /**
     * The test context
     */
    private TestContext _context = null;

    /**
     * Construct an instance of this class for a specific test case
     *
     * @param name the name of test case
     */
    public JMSTestCase(String name) {
        super(name);
    }

    /**
     * Set the context to test against
     *
     * @param context the test context
     */
    @Override
    public void setContext(TestContext context) {
        _context = context;
    }

    /**
     * Return the context to test against
     *
     * @return the test context
     */
    @Override
    public TestContext getContext() {
        return _context;
    }

    /**
     * Returns if this test can share resources with other test cases.
     * This implementation always returns <code>true</code>
     *
     * @return <code>true</code>
     */
    @Override
    public boolean share() {
        return true;
    }

    /**
     * Returns the requirement identifiers covered by a test case.
     * This implementation returns the values of any
     * <code>jmscts.requirement</code> javadoc tags associated with the test
     * case
     *
     * @param test the name of the test method
     * @return a list of requirement identifiers covered by the test
     */
    public String[] getRequirements(String test) {
        return AttributeHelper.getAttributes(
            getClass(), test, "jmscts.requirement");
    }

}
