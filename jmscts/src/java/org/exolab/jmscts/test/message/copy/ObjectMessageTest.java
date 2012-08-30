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
 * $Id: ObjectMessageTest.java,v 1.5 2004/02/03 07:31:02 tanderson Exp $
 */
package org.exolab.jmscts.test.message.copy;

import java.io.Serializable;
import java.util.Arrays;

import javax.jms.ObjectMessage;
import javax.jms.Session;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractMessageTestCase;
import org.exolab.jmscts.core.MessagePopulator;
import org.exolab.jmscts.core.TestContext;
import org.exolab.jmscts.core.TestCreator;
import org.exolab.jmscts.test.message.util.MessageValues;


/**
 * This class tests that <code>ObjectMessage</code> copies content
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.5 $
 * @see javax.jms.ObjectMessage
 * @see AbstractMessageTestCase
 * @jmscts.message ObjectMessage
 */
public class ObjectMessageTest extends AbstractMessageTestCase
    implements MessageValues {

    /**
     * Construct a new <code>ObjectMessageTest</code>
     *
     * @param name the name of test case
     */
    public ObjectMessageTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createMessageTest(ObjectMessageTest.class);
    }

    /**
     * Get the message populator. This implementation always returns null
     *
     * @return null
     */
    @Override
    public MessagePopulator getMessagePopulator() {
        return null;
    }

    /**
     * Verifies that <code>ObjectMessage.setObject()</code> copies byte arrays
     *
     * @jmscts.requirement message.copy
     * @throws Exception for any error
     */
    public void testByteArrayCopy() throws Exception {
        final int size = 10;
        TestContext context = getContext();
        ObjectMessage message = (ObjectMessage) context.getMessage();

        byte[] bytes = new byte[size];
        Arrays.fill(bytes, (byte) 0);
        message.setObject(bytes);

        // verify that the input was copied, by modifying it
        Arrays.fill(bytes, (byte) 1);
        byte[] value = (byte[]) message.getObject();
        assertTrue("ObjectMessage.setObject() did not copy the byte array",
                   !Arrays.equals(value, bytes));

        // verify that the return value is not the same each time
        byte[] value1 = (byte[]) message.getObject();
        byte[] value2 = (byte[]) message.getObject();
        assertTrue("ObjectMessage.getObject() did not copy the byte array",
                   value1 != value2);
    }

    /**
     * Verifies that <code>ObjectMessage.setObject()</code> copies user objects
     *
     * @jmscts.requirement message.copy
     * @throws Exception for any error
     */
    public void testCustomObjectCopy() throws Exception {
        TestContext context = getContext();
        ObjectMessage message = (ObjectMessage) context.getMessage();

        MyObject source = new MyObject();
        source.setContent("ABC");

        message.setObject(source);
        MyObject copy = (MyObject) message.getObject();
        assertTrue("ObjectMessage.setObject() did not copy the object",
                   source != copy);
        // ensure it did a deep copy
        assertTrue("ObjectMessage.setObject() did not perform a deep copy of"
                   + " the object", source.getContent() != copy.getContent());

        source.setContent("DEF");
        copy = (MyObject) message.getObject();
        assertTrue("ObjectMessage.setObject() did not copy the object",
                   !source.getContent().equals(copy.getContent()));
    }

    /**
     * Verifies that <code>ObjectMessage.getObject()</code> returns a
     * different reference to that set, when the object is supplied at
     * construction
     *
     * @jmscts.requirement message.copy
     * @throws Exception for any error
     */
    public void testCopyAtConstruction() throws Exception {
        TestContext context = getContext();
        Session session = context.getSession();

        MyObject source = new MyObject();
        source.setContent("ABC");
        ObjectMessage message = session.createObjectMessage(source);

        MyObject copy = (MyObject) message.getObject();
        assertTrue("ObjectMessage.setObject() did not copy the object",
                   source != copy);
        // ensure it did a deep copy
        assertTrue("ObjectMessage.setObject() did not perform a deep copy of"
                   + " the object", source.getContent() != copy.getContent());
    }

    /**
     * Helper object for testing copying
     */
    public static class MyObject implements Serializable {

        /** TODO */
        private static final long serialVersionUID = 1L;
        /**
         * The content
         */
        private String _content = null;


        /**
         * Set the content
         *
         * @param content the content
         */
        public void setContent(String content) {
            _content = content;
        }

        /**
         * Returns the content
         *
         * @return the content
         */
        public String getContent() {
            return _content;
        }
    }

}
