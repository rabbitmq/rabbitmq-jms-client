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
 * $Id: TestCoverageListener.java,v 1.4 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import javax.jms.JMSException;
import javax.naming.NamingException;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestListener;


/**
 * This class implements the {@link TestListener} interface to capture
 * coverage of requirements by test cases
 *
 * @version     $Revision: 1.4 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         TestCoverage
 * @see         JMSTestRunner
 */
class TestCoverageListener implements TestListener {

    /**
     * The test coverage of requirements
     */
    private final TestCoverage _coverage;


    /**
     * Construct a new listener
     *
     * @param coverage the instance to record test coverage of requirements
     */
    public TestCoverageListener(TestCoverage coverage) {
        if (coverage == null) {
            throw new IllegalArgumentException("Argument 'coverage' is null");
        }
        _coverage = coverage;
    }

    /**
     * An error occurred
     *
     * @param test the test
     * @param error the error
     */
    @Override
    public void addError(Test test, Throwable error) {
        Throwable cause = null;
        if (error instanceof JMSException) {
            cause = ((JMSException) error).getLinkedException();
        } else if (error instanceof NamingException) {
            cause = ((NamingException) error).getRootCause();
        }
        _coverage.failed(test, error, cause);
    }

    /**
     * A failure occurred
     *
     * @param test the test
     * @param error the error
     */
    @Override
    public void addFailure(Test test, AssertionFailedError error) {
        if (test instanceof JMSTestCase) {
            _coverage.failed(test, error, null);
        }
    }

    /**
     * A test ended
     *
     * @param test the test
     */
    @Override
    public void endTest(Test test) {
        if (test instanceof JMSTestCase) {
            _coverage.end((JMSTestCase) test);
        }
    }

    /**
     * A test started
     *
     * @param test the test
     */
    @Override
    public void startTest(Test test) {
        if (test instanceof JMSTestCase) {
            _coverage.begin((JMSTestCase) test);
        }
    }

}
