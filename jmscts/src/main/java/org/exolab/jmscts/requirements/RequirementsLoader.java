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
 * $Id: RequirementsLoader.java,v 1.2 2004/02/02 03:51:07 tanderson Exp $
 */
package org.exolab.jmscts.requirements;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;


/**
 * Helper class used to load requirements documents
 *
 * @version     $Revision: 1.2 $ $Date: 2004/02/02 03:51:07 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         Requirements
 */
public final class RequirementsLoader {

    /**
     * The root requirements resource path
     */
    private static final String PATH = "/org/exolab/jmscts/requirements/";

    /**
     * The root requirements document
     */
    private static final String FILE = PATH + "requirements.xml";


    /**
     * Prevent construction of utility class
     */
    private RequirementsLoader() {
    }

    /**
     * Load the requirements from a requirements document
     *
     * @return the requirements
     * @throws FileNotFoundException if the requirements document cannot be
     * found
     * @throws MarshalException if the requirements document cannot be read
     * @throws ValidationException if the requirements document doesn't
     * comply with the XML Schema definition
     */
    public static Requirements load()
        throws FileNotFoundException, MarshalException, ValidationException {
        Requirements result = new Requirements();

        add(result, getResourceAsStream(FILE));
        return result;
    }

    /**
     * Unmarshall a requirements document, adding it to the requirements
     *
     * @param result the requirements
     * @param stream the stream to the requirements document
     * @throws FileNotFoundException if an included requirements document
     * cannot be found
     * @throws MarshalException if the requirements document cannot be read
     * @throws ValidationException if the requirements document doesn't
     * comply with the XML Schema definition
     */
    private static void add(Requirements result, InputStreamReader stream)
        throws FileNotFoundException, MarshalException, ValidationException {

        Document source = Document.unmarshal(stream);

        Include[] includes = source.getInclude();
        if (includes != null) {
            for (int i = 0; i < includes.length; ++i) {
                Include include = includes[i];

                String path = PATH + include.getPath();
                add(result, getResourceAsStream(path));
            }
        }

        Reference[] references = source.getReference();
        if (references != null) {
            for (int i = 0; i < references.length; ++i) {
                result.addReference(references[i]);
            }
        }

        Requirement[] requirements = source.getRequirement();
        if (requirements != null) {
            for (int i = 0; i < requirements.length; ++i) {
                result.addRequirement(requirements[i]);
            }
        }
    }

    /**
    * Helper to return a stream to a resource
    *
    * @param path the resource path
    * @return a stream to the resource
    * @throws FileNotFoundException if the resource can't be found
    */
    private static InputStreamReader getResourceAsStream(String path)
        throws FileNotFoundException {
        InputStream stream =
            RequirementsLoader.class.getResourceAsStream(path);
        if (stream == null) {
            throw new FileNotFoundException("Cannot locate resource: " + path);
        }
        return new InputStreamReader(stream);
    }

}
