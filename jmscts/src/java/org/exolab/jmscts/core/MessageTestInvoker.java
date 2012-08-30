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
 * $Id: MessageTestInvoker.java,v 1.3 2005/06/16 08:10:52 tanderson Exp $
 */
package org.exolab.jmscts.core;

import javax.jms.Connection;
import javax.jms.Session;
import javax.jms.Message;

import junit.framework.Test;
import junit.framework.TestResult;


/**
 * Helper class to run an {@link MessageTestCase}
 *
 * @version     $Revision: 1.3 $ $Date: 2005/06/16 08:10:52 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         MessageTestCase
 */
abstract class MessageTestInvoker extends TestInvoker {

    /**
     * The message type
     */
    private final Class<?> _type;


    /**
     * Construct a new <code>MessageTestInvoker</code>
     *
     * @param test the test to run. Must be an instance of
     * <code>MessageTestCase</code>
     * @param result the result of the test
     * @param context the test context
     * @param filter the test filter. May be <code>null</code>
     * @param type the message type to run the test against
     */
    public MessageTestInvoker(Test test, TestResult result,
                              TestContext context, TestFilter filter,
                              Class<?> type) {
        super(test, result, context, filter);

        if (!(test instanceof MessageTestCase)) {
            throw new IllegalArgumentException(
                "Argument 'test' must implement MessageTestCase");
        }
        if (type == null) {
            throw new IllegalArgumentException("Argument 'type' is null");
        }
        _type = type;
    }

    /**
     * Setup the test
     *
     * @param test the test
     * @param context the test context
     * @throws Exception for any error
     */
    @Override
    protected void setUp(JMSTest test, TestContext context) throws Exception {
        super.setUp(test, context);

        MessageTestCase messageTest = (MessageTestCase) test;

        if (!messageTest.share()) {
            // test case cannot share resources, so a new connection and
            // session have been allocated by the parent
            if (messageTest.startConnection()) {
                context.getConnection().start();
            }
        } else {
            // note that the connection is only stopped for shared
            // connections. This is to avoid conflicts with tests
            // that test connection stop/start behaviour when the
            // connection is created
            Connection connection = context.getConnection();
            if (messageTest.startConnection()) {
                connection.start();
            } else {
                connection.stop();
            }
        }
    }

    /**
     * Create a message to test against, corresponding to the type
     * passed at construction. If the test has an associated
     * <code>MessagePopulator</code>, this will be used to populate the message
     *
     * @return a new message
     * @throws Exception for any error
     */
    protected Message create() throws Exception {
        MessageTestCase test = (MessageTestCase) getTest();
        TestContext context = getContext();
        Session session = context.getSession();
        MessageCreator creator = new MessageCreator(
            session, test.getMessagePopulator());
        return creator.create(_type);
    }

    /**
     * Returns the type of the message to test against
     *
     * @return the type of the message to test against
     */
    protected Class<?> getMessageType() {
        return _type;
    }

}
