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
 * $Id: AbstractSessionTestCase.java,v 1.5 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import org.apache.log4j.Logger;


/**
 * This class provides a default implementation of the {@link SessionTestCase}
 * interface.
 *
 * @version     $Revision: 1.5 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         SessionTestRunner
 * @see         AckTypes
 */
public abstract class AbstractSessionTestCase
    extends AbstractConnectionTestCase
    implements SessionTestCase {

    /**
     * The logger
     */
    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger(
        AbstractSessionTestCase.class);


    /**
     * Construct an instance of this class for a specific test case, to be
     * run against all message acknowledgement types
     *
     * @param name the name of test case
     */
    public AbstractSessionTestCase(String name) {
        super(name);
    }

    /**
     * Returns the session types to test against.
     * This implementation returns the values of any
     * <code>jmscts.session</code> javadoc tags associated with the test
     * case.
     * The none are specified, returns {@link AckTypes#ALL}
     *
     * @return the session types to test against
     */
    @Override
    public AckTypes getAckTypes() {
        AckTypes result = null;
        String test = getName();
        String[] types = AttributeHelper.getAttributes(
            getClass(), test, "jmscts.session", false);

        if (types.length == 0) {
            result = AckTypes.ALL;
        } else {
            result = AckTypes.fromString(types);
        }
        return result;
    }

}
