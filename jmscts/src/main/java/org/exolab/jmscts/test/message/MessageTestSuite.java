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
 * $Id: MessageTestSuite.java,v 1.4 2004/02/03 00:59:11 tanderson Exp $
 */
package org.exolab.jmscts.test.message;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.exolab.jmscts.test.message.bytes.BytesMessageTest;
import org.exolab.jmscts.test.message.clear.ClearTestSuite;
import org.exolab.jmscts.test.message.close.CloseTest;
import org.exolab.jmscts.test.message.copy.CopyTestSuite;
import org.exolab.jmscts.test.message.foreign.ForeignMessageTest;
import org.exolab.jmscts.test.message.header.HeaderTestSuite;
import org.exolab.jmscts.test.message.map.MapMessageTest;
import org.exolab.jmscts.test.message.object.ObjectMessageTest;
import org.exolab.jmscts.test.message.properties.PropertyTestSuite;
import org.exolab.jmscts.test.message.readwrite.ReadWriteTestSuite;
import org.exolab.jmscts.test.message.stream.StreamMessageTest;


/**
 * This class encapsulates all Message related tests.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $
 */
public final class MessageTestSuite {

    /**
     * Prevent construction of helper class
     */
    private MessageTestSuite() {
    }

    /**
     * Sets up the test suite.
     *
     * @return the test suite
     */
    public static Test suite() {

        TestSuite suite = new TestSuite();

        suite.addTest(BytesMessageTest.suite());
        suite.addTest(MapMessageTest.suite());
        suite.addTest(ObjectMessageTest.suite());
        suite.addTest(StreamMessageTest.suite());
        suite.addTest(PropertyTestSuite.suite());
        suite.addTest(HeaderTestSuite.suite());
        suite.addTest(ReadWriteTestSuite.suite());
        suite.addTest(ClearTestSuite.suite());
        suite.addTest(CopyTestSuite.suite());
        suite.addTest(CloseTest.suite());
        suite.addTest(ForeignMessageTest.suite());
        return suite;
    }

}
