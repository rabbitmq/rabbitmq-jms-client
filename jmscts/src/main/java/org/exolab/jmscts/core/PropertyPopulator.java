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
 * $Id: PropertyPopulator.java,v 1.3 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import java.util.Iterator;
import java.util.Map;

import javax.jms.Message;


/**
 * A message populator that populates properties using a list of
 * {@link Property} instances
 *
 * @version     $Revision: 1.3 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public class PropertyPopulator implements MessagePopulator {

    /** TODO */
    private static final long serialVersionUID = 1L;

    /**
     * A list of properties to populate messages with.
     */
    private Property[] _list = null;

    /**
     * A map of properties to populate messages with.
     */
    private Map<?, ?> _map = null;


    /**
     * Create an instance of this class, with the list of properties to
     * populate messages with
     *
     * @param properties the list of properties to populate messages with
     */
    public PropertyPopulator(Property[] properties) {
        if (properties == null) {
            throw new IllegalArgumentException(
                "Argument 'properties' is null");
        }
        _list = properties;
    }

    /**
     * Create an instance of this class, with a map of properties to
     * populate messages with
     *
     * @param properties a map of properties to populate messages with
     */
    public PropertyPopulator(Map<?, ?> properties) {
        if (properties == null) {
            throw new IllegalArgumentException(
                "Argument 'properties' is null");
        }
        _map = properties;
    }

    /**
     * Populate the message with the properties passed at construction
     *
     * @param message the message to populate
     * @throws Exception for any error
     */
    @Override
    public void populate(Message message) throws Exception {
        if (_list != null) {
            for (int i = 0; i < _list.length; ++i) {
                Property property = _list[i];
                Object value = PropertyHelper.create(property);
                message.setObjectProperty(property.getName(), value);
            }
        } else {
            Iterator<?> iterator = _map.entrySet().iterator();
            while (iterator.hasNext()) {
                @SuppressWarnings("rawtypes")
                Map.Entry entry = (Map.Entry) iterator.next();
                String name = (String) entry.getKey();
                Object value = entry.getValue();
                message.setObjectProperty(name, value);
            }
        }
    }

}
