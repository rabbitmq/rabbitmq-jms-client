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
 * $Id: SendReceive2Size0KTest.java,v 1.4 2004/02/03 21:52:08 tanderson Exp $
 */
package org.exolab.jmscts.stress;

import junit.framework.Test;

import org.exolab.jmscts.core.MessagePopulator;
import org.exolab.jmscts.core.TestCreator;


/**
 * Performs a stress test using one producer, two consumers and empty
 * messages.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $
 * @jmscts.message all
 * @jmscts.session AUTO_ACKNOWLEDGE
 * @jmscts.session CLIENT_ACKNOWLEDGE
 * @jmscts.session DUPS_OK_ACKNOWLEDGE
 * @jmscts.delivery consumer
 */
public class SendReceive2Size0KTest extends SendReceiveStressTestCase {

    /**
     * The destination used by this test case
     */
    private static final String DESTINATION = "SendReceive2Size0KTest";


    /**
     * Construct a <code>SendReceive2Size0KTest</code> for a specific
     * test case
     *
     * @param name the name of test case
     */
    public SendReceive2Size0KTest(String name) {
        super(name, DESTINATION, 2);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.StressTestRunner}
     */
    public static Test suite() {
        return TestCreator.createSendReceiveTest(SendReceive2Size0KTest.class);
    }

    /**
     * Get the message populator. This implementation returns <code>null</code>
     *
     * @return <code>null</code>
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
        return new String[]{DESTINATION};
    }

    /**
     * Performs a stress test using:
     * <ul>
     *   <li>one producer</li>
     *   <li>two consumers</li>
     *   <li>one connection</li>
     *   <li>one destination</li>
     * </ul>
     *
     * The producer and consumers run concurrently.<br/>
     * For <em>CLIENT_ACKNOWLEDGE</em> sessions, each message is acknowledged.
     *
     * @throws Exception for any error
     */
    public void test() throws Exception {
        runStress();
    }

}
