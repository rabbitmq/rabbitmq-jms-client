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
 * $Id: TestProperties.java,v 1.2 2004/01/31 13:44:24 tanderson Exp $
 */
package org.exolab.jmscts.core;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


/**
 * Helper class to provide access to properties defined in a
 * <em>jmscts.properties</em> property file.
 * This is read from the ${jmscts.home}/config directory.
 *
 * @version     $Revision: 1.2 $ $Date: 2004/01/31 13:44:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public final class TestProperties {

    /**
     * The properties
     */
    private static final Properties PROPS;

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(TestProperties.class.getName());


    /**
     * Prevent construction of utility class
     */
    private TestProperties() {
    }

    /**
     * Returns the value of a property. The property name is the
     * concatentation of prefix.getName() + "." + name
     *
     * @param prefix the property name prefix
     * @param name the property name suffix
     * @param defaultValue the value to use if no property exists
     * @return the value of the property, or <code>defaultValue</code> if
     * no property exists
     */
    public static String getString(Class<?> prefix, String name,
                                   String defaultValue) {
        return getString(concat(prefix, name), defaultValue);
    }

    /**
     * Returns the value of a property.
     *
     * @param name the property name
     * @param defaultValue the value to use if no property exists
     * @return the value of the property, or <code>defaultValue</code> if
     * no property exists
     */
    public static String getString(String name, String defaultValue) {
        return PROPS.getProperty(name, defaultValue);
    }

    /**
     * Returns the value of a property. The property name is the
     * concatentation of prefix.getName() + "." + name
     *
     * @param prefix the property name prefix
     * @param name the property name suffix
     * @param defaultValue the value to use if no property exists
     * @return the value of the property, or <code>defaultValue</code> if
     * no property exists
     */
    public static int getInt(Class<?> prefix, String name, int defaultValue) {
        return getInt(concat(prefix, name), defaultValue);
    }

    /**
     * Returns the value of a property.
     *
     * @param name the property name
     * @param defaultValue the value to use if no property exists
     * @return the value of the property, or <code>defaultValue</code> if
     * no property exists
     */
    public static int getInt(String name, int defaultValue) {
        int result = defaultValue;
        String value = getString(name, Integer.toString(defaultValue));
        try {
            result = Integer.parseInt(value);
        } catch (NumberFormatException ignore) {
            log.warn("Error parsing integer property=" + name
                     + ", using default value");
        }
        return result;
    }

    /**
     * Returns the value of a property. The property name is the
     * concatentation of prefix.getName() + "." + name
     *
     * @param prefix the property name prefix
     * @param name the property name suffix
     * @param defaultValue the value to use if no property exists
     * @return the value of the property, or <code>defaultValue</code> if
     * no property exists
     */
    public static long getLong(Class<?> prefix, String name, long defaultValue) {
        return getLong(concat(prefix, name), defaultValue);
    }

    /**
     * Returns the value of a property.
     *
     * @param name the property name
     * @param defaultValue the value to use if no property exists
     * @return the value of the property, or <code>defaultValue</code> if
     * no property exists
     */
    public static long getLong(String name, long defaultValue) {
        long result = defaultValue;
        String value = getString(name, Long.toString(defaultValue));
        try {
            result = Long.parseLong(value);
        } catch (NumberFormatException ignore) {
            log.warn("Error parsing long property=" + name
                     + ", using default value");
        }
        return result;
    }

    /**
     * Helper to concatenate the class prefix and name to form a property name
     *
     * @param prefix the property name prefix
     * @param name the property name suffix
     * @return the property name
     */
    private static String concat(Class<?> prefix, String name) {
        return prefix.getName() + "." + name;
    }

    static {
        PROPS = new Properties();
        String home = System.getProperty("jmscts.home");
        if (home == null) {
            log.warn("jmscts.home not set - not loading properties");
        } else {
            String path = home + "/config/jmscts.properties";
            try {
                File file = new File(path);
                if (file.exists()) {
                    FileInputStream stream = new FileInputStream(file);
                    PROPS.load(stream);
                } else {
                    log.warn("jmscts.properties not found: path=" + path);
                }
            } catch (IOException exception) {
                log.error("Failed to read " + path, exception);
            }
        }
    }

}
