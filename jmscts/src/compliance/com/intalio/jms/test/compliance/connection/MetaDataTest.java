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
 * $Id: MetaDataTest.java,v 1.3 2003/05/04 14:12:37 tanderson Exp $
 */
package org.exolab.jmscts.test.connection;

import java.util.Enumeration;
import java.util.HashSet;

import javax.jms.Connection;
import javax.jms.ConnectionMetaData;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractConnectionTestCase;
import org.exolab.jmscts.core.JMSTestRunner;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests that connections support JMSXGroupID and JMSXGroupSeq
 * properties
 * <p>
 * It covers the following requirements:
 * <ul>
 *   <li>connection.metadata.properties</li>
 * </ul>
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.3 $
 * @see AbstractConnectionTestCase
 * @see org.exolab.jmscts.core.ConnectionTestRunner
 */
public class MetaDataTest extends AbstractConnectionTestCase {

    /**
     * Requirements covered by this test case
     */
    private static final String[][] REQUIREMENTS = {
        {"testMetaData", "connection.metadata.properties"}};

    private static final String GROUP_ID = "JMSXGroupID";
    private static final String GROUP_SEQ = "JMSXGroupSeq";

    /**
     * Create an instance of this class for a specific test case
     *
     * @param name the name of test case
     */
    public MetaDataTest(String name) {
        super(name, REQUIREMENTS);
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
        return TestCreator.createConnectionTest(MetaDataTest.class);
    }

    /**
     * Test that the connection supports JMSXGroupID and JMSXGroupSeq 
     * properties. 
     * This covers requirements:
     * <ul>
     *   <li>connection.metadata.properties</li>
     * </ul>
     *
     * @throws Exception for any error
     */
    public void testMetaData() throws Exception {
        ConnectionMetaData metaData = 
            getContext().getConnection().getMetaData();
        if (metaData == null) {
            fail("Connection returned null for getMetaData()");
        }

        HashSet names = new HashSet();
        Enumeration iter = metaData.getJMSXPropertyNames();
        while (iter.hasMoreElements()) {
            names.add((String) iter.nextElement());
        }
        if (!names.contains(GROUP_ID)) {
            fail("ConnectionMetaData does not support " + GROUP_ID);
        }
        if (!names.contains(GROUP_SEQ)) {
            fail("ConnectionMetaData does not support " + GROUP_SEQ);
        }
    }
    
} //-- MetaDataTest
