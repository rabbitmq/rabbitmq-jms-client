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
 * $Id: SendReceiveTestInvoker.java,v 1.6 2006/09/19 19:55:12 donh123 Exp $
 */
package org.exolab.jmscts.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.IllegalStateException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import junit.framework.Test;
import junit.framework.TestResult;

import org.apache.log4j.Logger;

import org.exolab.jmscts.provider.Administrator;


/**
 * Helper class to run a test case, for the given message type,
 * delivery mode, messaging mode, and destination type.
 * <p>
 * Prior to running, it creates a new message of
 * the specified type, and creates any destinations required by the test.
 *
 * @version     $Revision: 1.6 $ $Date: 2006/09/19 19:55:12 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         SendReceiveTestCase
 */
public class SendReceiveTestInvoker extends MessageTestInvoker {

    /**
     * The messaging behaviour
     */
    private final MessagingBehaviour _behaviour;

    /**
     * The destinations used by the test
     */
    private Map<String, Destination> _destinations = null;

    /**
     * Seed for generating destination names
     */
    private static int _seed = 0;

    /**
     * The logger
     */
    private static final Logger _log =
        Logger.getLogger(SendReceiveTestInvoker.class);


    /**
     * Construct a new <code>SendReceiveTestInvoker</code>
     *
     * @param test the test to run. Must be an instance of
     * <code>SendReceiveTestCase</code>
     * @param result the result of the test
     * @param context the test context
     * @param filter the test filter. May be <code>null</code>
     * @param messageType the message type to run the test against
     * @param behaviour the messaging behaviour
     */
    public SendReceiveTestInvoker(
        Test test, TestResult result, TestContext context, TestFilter filter,
        Class<?> messageType, MessagingBehaviour behaviour) {
        super(test, result, context, filter, messageType);

        if (!(test instanceof SendReceiveTestCase)) {
            throw new IllegalArgumentException(
                "Argument 'test' must implement SendReceiveTestCase");
        }
        if (behaviour == null) {
            throw new IllegalArgumentException("Argument 'behaviour' is null");
        }

        _behaviour = behaviour;
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

        if (_log.isDebugEnabled()) {
            String type = context.getSessionType().getName();
            String msg = "test=" + test + " using session type=" + type
                + ", message type=" + getMessageType().getName() + ", "
                + _behaviour;
            _log.debug("running " + msg);
        }

        SendReceiveTestCase sendReceive = (SendReceiveTestCase) test;
        TestContext child;
        if (sendReceive.shouldCreateMessage()) {
            // set up the message for the test
            Message message = create();
            child = new TestContext(context, message, _behaviour);
        } else {
            child = new TestContext(context, getMessageType(), _behaviour);
        }
        test.setContext(child);
        String[] names = sendReceive.getDestinations();
        _destinations = createDestinations(child, names);
        sendReceive.setDestinations(_destinations);
    }

    /**
     * Tears down the test. This invokes {@link #tearDown(JMSTest test,
     * TestContext context)}, before destroying any allocated contexts
     *
     * @throws Exception for any error
     */
    @Override
    protected void tearDown() throws Exception {
        try {
            super.tearDown();
        } finally {
            destroyDestinations(_destinations);
        }
    }

    /**
     * Tear down the test
     *
     * @param test the test
     * @param context the test context
     * @throws Exception for any error
     */
    @Override
    protected void tearDown(JMSTest test, TestContext context)
        throws Exception {

        Session session = context.getSession();

        if (_log.isDebugEnabled()) {
            String type = context.getSessionType().getName();
            String msg = "test=" + test + " using session type=" + type
                + ", message type=" + getMessageType().getName() + ", "
                + _behaviour;
            _log.debug("completed " + msg);
        }

        if (!context.isInvalid()) {
            try {
		if (!(session instanceof javax.jms.XASession) && session.getTransacted()) {
                    // flush any messages to help ensure that destroying the
                    // destination doesn't cause too many problems
                    session.commit();
                }
            } catch (IllegalStateException ignore) {
                // session has been closed
            } catch (JMSException ignore) {
                // session has been closed
            }
        }
    }

    /**
     * Create any destinations required by the test case
     *
     * @param context the test context
     * @param names the destination names to create <code>Destination</code>
     * instances for
     * @return a map of Destination instances, keyed on name.
     * @throws Exception for any error
     */
    private Map<String, Destination> createDestinations(TestContext context, String[] names)
        throws Exception {
        HashMap<String, Destination> destinations = new HashMap<String, Destination>();
        Administrator admin = context.getAdministrator();
        if (names != null) {
            Destination destination = null;
            for (int i = 0; i < names.length; ++i) {
                String key = names[i];
                String name = key + (++_seed);
                try {
                    DestinationHelper.destroy(name, admin);
                    destination = DestinationHelper.create(context, name);
                } catch (Exception exception) {
                    _log.error(exception, exception);
                    throw exception;
                }
                destinations.put(key, destination);
            }
        }
        return destinations;
    }

    /**
     * Destroy any destinations set up for the test case
     *
     * @param destinations a map of <code>Destination</code> instances, keyed
     * on name
     * @throws Exception for any error
     */
    protected void destroyDestinations(Map<String, Destination> destinations) throws Exception {
        TestContext context = getContext();
        Administrator admin = context.getAdministrator();

        Iterator<Destination> iter = destinations.values().iterator();
        while (iter.hasNext()) {
            try {
                Destination destination = iter.next();
                if (destination!=null) 
                    DestinationHelper.destroy(destination, admin);
            } catch (Exception exception) {
                _log.error(exception, exception);
                throw exception;
            }
        }
    }

}
