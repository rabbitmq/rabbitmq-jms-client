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
 * $Id: ProviderLoader.java,v 1.3 2004/02/02 03:50:09 tanderson Exp $
 */
package org.exolab.jmscts.provider;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;


/**
 * Loads a {@link Provider} using its configuration
 *
 * @version     $Revision: 1.3 $ $Date: 2004/02/02 03:50:09 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         Provider
 */
public class ProviderLoader {

    /**
     * The provider name
     */
    private String _name;

    /**
     * The provider classname
     */
    private String _className;

    /**
     * Determines if the provider should be started prior to running tests
     */
    private boolean _start;

    /**
     * Determines if the provider should be stopped after running tests
     */
    private boolean _stop;

    /**
     * The provider classpath
     */
    private Paths _paths;

    /**
     * The provider
     */
    private Provider _provider;


    /**
     * Construct a new <code>ProviderLoader</code>
     */
    public ProviderLoader() {
    }

    /**
     * Sets the provider name
     *
     * @param name the provider name
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * Returns the provider name
     *
     * @return the provider name
     */
    public String getName() {
        return _name;
    }

    /**
     * Sets the provider implementation class name
     *
     * @param className the provider implementation class name
     */
    public void setClassName(String className) {
        _className = className;
    }

    /**
     * Returns the provider implementation class name
     *
     * @return the provider implementation class name
     */
    public String getClassName() {
        return _className;
    }

    /**
     * Sets if the provider should be started prior to running tests
     *
     * @param start if <code>true</code>, start the provider
     */
    public void setStart(boolean start) {
        _start = start;
    }

    /**
     * Determines if the provider should be started prior to running tests
     *
     * @return <code>true</code> if the provider should be started
     */
    public boolean getStart() {
        return _start;
    }

    /**
     * Sets if the provider should be stopped after running tests
     *
     * @param stop if <code>true</code>, stop the provider
     */
    public void setStop(boolean stop) {
        _stop = stop;
    }

    /**
     * Determines if the provider should be stopped after running tests
     *
     * @return <code>true</code> if the provider should be stopped
     */
    public boolean getStop() {
        return _stop;
    }

    /**
     * Sets the classpath for the provider
     *
     * @param paths the classpath
     */
    public void setPaths(Paths paths) {
        _paths = paths;
    }

    /**
     * Returns the classpath for the provider
     *
     * @return the classpath
     */
    public Paths getPaths() {
        return _paths;
    }

    /**
     * Set the provider
     *
     * @param provider the provider
     */
    public void setProvider(Provider provider) {
        _provider = provider;
    }

    /**
     * Returns the provider
     *
     * @return the provider
     */
    public Provider getProvider() {
        return _provider;
    }

    /**
     * Create a new provider. This constructs an instance of the class
     * specified by {@link #getClassName()}, using the classpath specified by
     * {@link #getPaths()}
     *
     * @return the new provider
     * @throws Exception if the provider can't be created
     */
    public Provider createProvider() throws Exception {
        ArrayList<URL> urls = new ArrayList<URL>();
        // Iterator<?> iterator = _paths.getPaths().iterator();
        // while (iterator.hasNext()) {
        // String path = (String) iterator.next();
        // urls.add(getURL(path));
        // }
        URL[] list = urls.toArray(new URL[0]);
        URLClassLoader loader = new URLClassLoader(list);
        Class<?> clazz = loader.loadClass(_className);
        return (Provider) clazz.newInstance();
    }

    /**
     * Converts a path to an URL
     *
     * @param path the path to convert
     * @return the path as a URL
     * @throws IOException if the path cannot be converted
     */
    @SuppressWarnings("deprecation")
    private URL getURL(String path) throws IOException {
        URL url;
        File file = new File(path);
        if (!file.isAbsolute()) {
            file = file.getCanonicalFile();
        }
        if (file.exists() && file.canRead()) {
            url = file.toURL();
        } else {
            url = new URL(path);

            if (url.getProtocol().equals("file")) {
                file = new File(url.getFile());
                if (file.exists() && file.canRead() && file.isDirectory()) {
                    url = file.getCanonicalFile().toURL();
                }
            }
        }

        return url;
    }

}
