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
 * $Id: TransformerHelper.java,v 1.5 2004/02/02 03:50:24 tanderson Exp $
 */
package org.exolab.jmscts.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;


/**
 * Helper class for performing XML transformations
 *
 * @version     $Revision: 1.5 $ $Date: 2004/02/02 03:50:24 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 */
public final class TransformerHelper {

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(TransformerHelper.class);

    /**
     * Xdoc file extension
     */
    private static final String XDOC_EXT = ".xml";

    /**
     * HTML file extension
     */
    private static final String HTML_EXT = ".html";

    /**
     * The xdoc to html stylesheet
     */
    private static final String XDOC_STYLESHEET = "resources/xdoc2html.xsl";


    /**
     * Prevent construction of utility class
     */
    private TransformerHelper() {
    }

    /**
     * Transforms a single xdoc file to HTML.
     *
     * if the output path is a directory, the output file name will be
     * the same name as the input path, with a .html extension.
     *
     * @param inputPath the path of the file to transform
     * @param outputPath the file or directory to generate HTML
     * @param baseDir the base directory path to locate resources
     * @throws IOException for any I/O error
     * @throws TransformerException if the stylesheet cannot be loaded or
     * applied
     */
    public static void transformXDoc2HTML(File inputPath, File outputPath,
                                          File baseDir)
        throws IOException, TransformerException {
        if (outputPath.isDirectory()) {
            String inputName = inputPath.getName();
            int length = inputName.length() - HTML_EXT.length() + 1;
            String outputName = inputName.substring(0, length) + HTML_EXT;
            outputPath = new File(outputPath, outputName);
        }

        File stylesheetPath = new File(baseDir, XDOC_STYLESHEET);
        if (log.isDebugEnabled()) {
            log.debug("Transforming file " + inputPath
                      + " using stylesheet " + stylesheetPath + " to "
                      + outputPath);
        }

        Templates stylesheet = getStylesheet(stylesheetPath);
        transform(inputPath, outputPath, stylesheet, getProperties(baseDir));
    }

    /**
     * Transforms all xdoc files in a directory to HTML
     *
     * @param inputDir the directory containing the xdocs
     * @param outputDir the directory to generate HTML
     * @param baseDir the base directory path to locate resources
     * @throws IOException for any I/O error
     * @throws TransformerException if the stylesheet cannot be loaded or
     * applied
     */
    public static void transformAllXDoc2HTML(File inputDir, File outputDir,
                                             File baseDir)
        throws IOException, TransformerException {

        File stylesheetPath = new File(baseDir, XDOC_STYLESHEET);
        if (log.isDebugEnabled()) {
            log.debug("Transforming directory " + inputDir
                      + " using stylesheet " + stylesheetPath + " to "
                      + outputDir);
        }

        String[] files = inputDir.list();
        if (files == null) {
            throw new IOException("Failed to list directory: " + inputDir);
        }

        Templates stylesheet = getStylesheet(stylesheetPath);
        HashMap<String, URL> properties = getProperties(baseDir);

        for (int i = 0; i < files.length; ++i) {
            String inputName = files[i];
            if (inputName.endsWith(XDOC_EXT)) {
                File input = new File(inputDir, inputName);
                int length = inputName.length() - HTML_EXT.length() + 1;
                String outputName = inputName.substring(0, length) + HTML_EXT;
                File output = new File(outputDir, outputName);
                transform(input, output, stylesheet, properties);
            }
        }
    }

    /**
     * Transforms a file
     *
     * @param inputPath the path of the file to transform
     * @param outputPath the path to write the transformed file
     * @param stylesheetPath the path to the stylesheet to perform the
     * transformation
     * @throws IOException for any I/O error
     * @throws TransformerException if the stylesheet cannot be loaded or
     * applied
     */
    public static void transform(File inputPath, File outputPath,
                                 File stylesheetPath)
        throws IOException, TransformerException {

        Templates stylesheet = getStylesheet(stylesheetPath);
        transform(inputPath, outputPath, stylesheet, new HashMap<String, URL>());
    }

    /**
     * Transforms a file
     *
     * @param inputPath the path of the file to transform
     * @param outputPath the path to write the transformed file
     * @param stylesheet the stylesheet to perform the transformation
     * @param properties properties to pass to the transformation
     * @throws IOException for any I/O error
     * @throws TransformerException if the stylesheet cannot be loaded or
     * applied
     */
    @SuppressWarnings("rawtypes")
    public static void transform(File inputPath, File outputPath,
                                 Templates stylesheet, HashMap<String, URL> properties)
        throws IOException, TransformerException {

        if (log.isDebugEnabled()) {
            log.debug("Transforming " + inputPath + " -> " + outputPath);
        }

        Transformer transformer = stylesheet.newTransformer();
        Iterator<?> iterator = properties.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            Object value = entry.getValue();
            transformer.setParameter(name, value);
        }

        transformer.transform(
            new StreamSource(inputPath),
            new StreamResult(new FileOutputStream(outputPath)));
    }

    /**
     * Loads a stylesheet
     *
     * @param path the stylesheet path
     * @return the stylesheet
     * @throws TransformerException if the stylesheet cannot be loaded
     */
    public static Templates getStylesheet(File path)
        throws TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance();

        Templates template = factory.newTemplates(new StreamSource(path));
        return template;
    }

    /**
     * Sets up properties for the xdoc2html stylesheet
     *
     * @param baseDir the base directory path to locate resources
     * @throws IOException for any I/O error
     * @return a map of the properties
     */
    @SuppressWarnings("deprecation")
    private static HashMap<String, URL> getProperties(File baseDir) throws IOException {
        File resourcesDir = new File(baseDir, "resources");
        HashMap<String, URL> properties = new HashMap<String, URL>();
        properties.put("style-path", resourcesDir.toURL());
        return properties;
    }

}
