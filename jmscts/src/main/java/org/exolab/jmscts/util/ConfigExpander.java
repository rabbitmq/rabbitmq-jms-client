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
 * $Id: ConfigExpander.java,v 1.2 2004/02/03 21:52:13 tanderson Exp $
 */
package org.exolab.jmscts.util;

import java.io.IOException;
import java.io.Reader;

import org.xml.sax.AttributeList;
import org.xml.sax.DocumentHandler;
import org.xml.sax.Locator;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributeListImpl;

import org.apache.log4j.Logger;

import org.exolab.castor.util.Configuration;
import org.exolab.castor.xml.EventProducer;


/**
 * This class expands elements and attributes in XML documents as the document
 * is being parsed. It is designed to be used in conjunction with the Castor
 * unmarshalling framework.
 * <p>
 * To be expanded, values must contain text of the form <i>${property.name}</i>
 * , where <i>property.name</i> is a property returned
 * by  System.getProperty().<br>
 * If no property exists, the value remains unchanged.
 *
 * @version     $Revision: 1.2 $ $Date: 2004/02/03 21:52:13 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         EventProducer
 * @see         org.exolab.castor.xml.Unmarshaller
 */
@SuppressWarnings({ "deprecation" })
public class ConfigExpander implements EventProducer {

    /**
     * The document handler
     */
    private DocumentHandler _handler = null;

    /**
     * The XML document reader
     */
    private Reader _reader = null;

    /**
     * The current element name
     */
    private String _name = null;

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(ConfigExpander.class);


    /**
     * Construct a new instance
     *
     * @param reader the XML document reader
     */
    public ConfigExpander(Reader reader) {
        _reader = reader;
    }

    /**
     * Sets the DocumentHandler to send SAX events to
     *
     * @param handler the handler to forward events to
     */
    @Override
    public void setDocumentHandler(DocumentHandler handler) {
        _handler = handler;
    }

    /**
     * Signals to start producing events
     *
     * @throws SAXException if the document cannot be parsed
     */
    @Override
    public void start() throws SAXException {
        Parser parser = Configuration.getDefaultParser();
        if (parser == null) {
            throw new SAXException("Unable to create parser");
        }

        DocumentHandler handler = new Expander();
        parser.setDocumentHandler(handler);
        try {
            parser.parse(new InputSource(_reader));
        } catch (IOException exception) {
            throw new SAXException(exception.getMessage(), exception);
        }
    }

    /**
     * Helper class to intercept {@link #startElement} calls, expanding
     * any attributes.
     */
    private class Expander implements DocumentHandler {

        /**
         * Receive an object for locating the origin of SAX document events.
         *
         * @param locator an object that can return the location of any SAX
         * document event
         */
        @Override
        public void setDocumentLocator(Locator locator) {
            _handler.setDocumentLocator(locator);
        }

        /**
         * Receive notification of the beginning of a document.
         *
         * @throws SAXException any SAX exception, possibly wrapping another
         * exception.
         */
        @Override
        public void startDocument() throws SAXException {
            _handler.startDocument();
        }

        /**
         * Receive notification of the end of a document.
         * @throws SAXException any SAX exception, possibly wrapping another
         * exception.
         */
        @Override
        public void endDocument() throws SAXException {
            _handler.endDocument();
        }

        /**
         * Receive notification of the beginning of an element.
         *
         * @param name the element name
         * @param list the attributes attached to the element, if any
         * @throws SAXException any SAX exception, possibly wrapping another
         * exception.
         */
        @Override
        public void startElement(String name, AttributeList list)
            throws SAXException {

            AttributeListImpl replaced = new AttributeListImpl();
            for (int i = 0; i < list.getLength(); i++) {
                String value = expand(list.getName(i), list.getValue(i));
                replaced.addAttribute(list.getName(i), list.getType(i), value);
            }
            _name = name;
            _handler.startElement(name, replaced);
        }

        /**
         * Receive notification of the end of an element
         *
         * @param name the element name
         * @throws SAXException any SAX exception, possibly wrapping another
         * exception.
         */
        @Override
        public void endElement(String name) throws SAXException {
            _handler.endElement(name);
            _name = null;
        }

        /**
         * Receive notification of character data
         *
         * @param ch the characters from the XML document
         * @param start the start position in the array
         * @param length the number of characters to read from the array
         * @throws SAXException any SAX exception, possibly wrapping another
         * exception.
         */
        @Override
        public void characters(char[] ch, int start, int length)
            throws SAXException {

            String content = new String(ch, start, length);
            content = expand(_name, content);
            _handler.characters(content.toCharArray(), 0, content.length());
        }

        /**
         * Receive notification of ignorable whitespace in element content.
         *
         * @param ch the characters from the XML document
         * @param start the start position in the array
         * @param length the number of characters to read from the array
         * @throws SAXException any SAX exception, possibly wrapping another
         * exception.
         */
        @Override
        public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
            _handler.ignorableWhitespace(ch, start, length);
        }

        /**
         * Receive notification of a processing instruction.
         *
         * @param target the processing instruction target
         * @param data the processing instruction data, or null if none was
         * supplied
         * @throws SAXException any SAX exception, possibly wrapping another
         * exception.
         */
        @Override
        public void processingInstruction(String target, String data)
            throws SAXException {
            _handler.processingInstruction(target, data);
        }

        /**
         * Expand a  value, if its contains embedded ${<property>} text
         *
         * @param name the attribute or element name
         * @param value the content to expand
         * @return the expanded value if it contained embedded text,
         * and the property refers to a value system property. If not, returns
         * value unchanged.
         */
        private String expand(String name, String value) {
            StringBuffer buffer = new StringBuffer();
            int prev = 0;
            int pos;
            while ((pos = value.indexOf("${", prev)) >= 0) {
                if (pos > 0) {
                    buffer.append(value.substring(prev, pos));
                }
                int index = value.indexOf('}', pos);
                if (index < 0) {
                    // invalid format
                    log.debug(
                        "Cannot expand as format is invalid: "
                        + "name=" + name + ", value=" + value);
                    buffer.append("${");
                    prev = pos + 2;
                } else {
                    String propertyName = value.substring(pos + 2, index);
                    String propertyValue = System.getProperty(propertyName);
                    if (propertyValue != null) {
                        buffer.append(propertyValue);
                    } else {
                        log.debug("Cannot expand " + name + " as property="
                                  + propertyName + " is not defined");
                        buffer.append("${");
                        buffer.append(propertyName);
                        buffer.append("}");
                    }
                    prev = index + 1;
                }
            }
            if (prev < value.length()) {
                buffer.append(value.substring(prev));
            }
            String result = buffer.toString();
            return result;
        }

    }

}
