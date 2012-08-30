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
 * $Id: JMSXGroupTest.java,v 1.5 2004/02/03 07:31:03 tanderson Exp $
 */
package org.exolab.jmscts.test.message.properties;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageFormatException;

import junit.framework.Test;

import org.exolab.jmscts.core.AbstractMessageTestCase;
import org.exolab.jmscts.core.ClassHelper;
import org.exolab.jmscts.core.TestCreator;


/**
 * This class tests that connections support JMSXGroupID and JMSXGroupSeq
 * properties <br/>
 * NOTE: the specification is not clear on the behaviour of null values
 * for these properties.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.5 $
 * @see AbstractMessageTestCase
 */
public class JMSXGroupTest extends AbstractMessageTestCase {

    /**
     * The JMSXGroupID property name
     */
    private static final String GROUP_ID = "JMSXGroupID";

    /**
     * The JMSXGroupSeq property name
     */
    private static final String GROUP_SEQ = "JMSXGroupSeq";

    /**
     * Invalid values for JMSXGroupID
     */
    private static final Object[] INVALID_GROUP_ID_VALUES = {
        Boolean.TRUE, new Byte(Byte.MIN_VALUE), new Short(Short.MIN_VALUE),
        new Character(Character.MIN_VALUE), new Integer(Integer.MIN_VALUE),
        new Float(Float.MIN_VALUE), new Double(Double.MIN_VALUE)};

    /**
     * Invalid values for JMSGroupSeq
     */
    private static final Object[] INVALID_GROUP_SEQ_VALUES = {
        Boolean.TRUE, new Byte(Byte.MIN_VALUE), new Short(Short.MIN_VALUE),
        new Character(Character.MIN_VALUE), new Float(Float.MIN_VALUE),
        new Double(Double.MIN_VALUE), "abc"};


    /**
     * Construct a new <code>JMSXGroupTest</code>
     *
     * @param name the name of test case
     */
    public JMSXGroupTest(String name) {
        super(name);
    }

    /**
     * Sets up the test suite
     *
     * @return an instance of this class that may be run by
     * {@link org.exolab.jmscts.core.JMSTestRunner}
     */
    public static Test suite() {
        return TestCreator.createMessageTest(JMSXGroupTest.class);
    }

    /**
     * Verifies that the only allowed type for JMSXGroupID is a String.
     *
     * @jmscts.requirement properties.group
     * @throws Exception for any error
     */
    public void testJMSXGroupID() throws Exception {
        Message message = getContext().getMessage();
        for (int i = 0; i < INVALID_GROUP_ID_VALUES.length; ++i) {
            Object value = INVALID_GROUP_ID_VALUES[i];
            checkProperty(message, GROUP_ID, value);
        }
    }

    /**
     * Verifies that the only allowed type for JMSXGroupID is an int.
     *
     * @jmscts.requirement properties.group
     * @throws Exception for any error
     */
    public void testJMSGroupSeq() throws Exception {
        Message message = getContext().getMessage();
        for (int i = 0; i < INVALID_GROUP_SEQ_VALUES.length; ++i) {
            Object value = INVALID_GROUP_SEQ_VALUES[i];
            checkProperty(message, GROUP_SEQ, value);
        }
    }

    /**
     * Verifies that JMSXGroupSeq only handles ints > 0
     * @todo - not sure if this test case is valid in terms of the
     * specification
     *
     * @jmscts.requirement properties.group
     * @throws Exception for any error
     */
    public void testJMSGroupSeqRange() throws Exception {
        Message message = getContext().getMessage();
        checkSequenceValue(message, -1, false);
        checkSequenceValue(message, 0, false);
        checkSequenceValue(message, 1, true);
        checkSequenceValue(message, Integer.MAX_VALUE, true);
    }

    /**
     * Verifies that atttempting to use an invalid property value
     * throws <code>MessageFormatException</code>
     *
     * @param message the message to populate
     * @param name the property name
     * @param value the property value
     * @throws Exception for any error
     */
    private void checkProperty(Message message, String name, Object value)
        throws Exception {

        // javax.jms.Message doesn't provide an interface for populating
        // char primitives. The setObjectProperty method should throw
        // MessageFormatException for Character instances.

        if (!(value instanceof Character)) {
            try {
                PropertyHelper.setPrimitiveProperty(message, name, value);
                fail("Managed to use invalid type="
                     + ClassHelper.getPrimitiveName(value.getClass())
                     + " for property=" + name);
            } catch (MessageFormatException expected) {
                // the expected behaviour
            } catch (Exception exception) {
                fail("Expected MessageFormatException to be thrown when "
                     + "setting property=" + name + " with type="
                     + ClassHelper.getPrimitiveName(value.getClass())
                     + " but got exception=" + exception.getClass().getName()
                     + ", message=" + exception.getMessage());
            }
        }

        try {
            message.setObjectProperty(name, value);
            fail("Managed to use invalid type="
                 + ClassHelper.getPrimitiveName(value.getClass())
                 + " for property=" + name);
        } catch (MessageFormatException expected) {
            // the expected behaviour
        } catch (Exception exception) {
            fail("Expected MessageFormatException to be thrown when "
                 + "setting property=" + name + " with type="
                 + ClassHelper.getPrimitiveName(value.getClass())
                 + " but got exception=" + exception.getClass().getName()
                 + ", message=" + exception.getMessage());
        }
    }

    /**
     * Verifies a value for the JMSXGroupSeq property
     *
     * @param message the message to populate
     * @param value the value to populate
     * @param valid determines if the value is a valid
     * @throws Exception for any error
     */
    private void checkSequenceValue(Message message, int value, boolean valid)
        throws Exception {

        checkSequenceValue(message, value, valid, true);
        checkSequenceValue(message, value, valid, false);
    }

    /**
     * Verifies a value for the JMSXGroupSeq property
     *
     * @param message the message to populate
     * @param value the value to populate
     * @param valid determines if the value is a valid
     * @param primitive if <code>true</code> uses setIntProperty() to populate
     * the value, else uses setObjectProperty()
     * @throws Exception for any error
     */
    private void checkSequenceValue(Message message, int value, boolean valid,
                                    boolean primitive) throws Exception {
        try {
            if (primitive) {
                message.setIntProperty(GROUP_SEQ, value);
            } else {
                message.setObjectProperty(GROUP_SEQ, new Integer(value));
            }
            if (!valid) {
                fail("Managed to use invalid int=" + value + " for property="
                     + GROUP_SEQ);
            }
        } catch (JMSException exception) {
            if (valid) {
                fail("Valid int value=" + value + " for property=" + GROUP_SEQ
                     + " threw exception=" + exception.getClass()
                     + ", message=" + exception.getMessage());
            }
        } catch (Exception exception) {
            if (valid) {
                fail("Valid int value=" + value + " for property="
                     + GROUP_SEQ + " threw exception=" + exception.getClass()
                     + ", message=" + exception.getMessage());
            } else {
                fail("Expected JMSException to be thrown when "
                     + "setting property=" + GROUP_SEQ + " with value=" + value
                     + " but got exception=" + exception.getClass().getName()
                     + ", message=" + exception.getMessage());
            }
        }
    }

}
