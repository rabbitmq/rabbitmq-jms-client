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
 * $Id: CoverageReport.java,v 1.6 2004/02/03 21:52:08 tanderson Exp $
 */
package org.exolab.jmscts.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import org.exolab.jmscts.core.TestCoverage;
import org.exolab.jmscts.provider.ProviderLoader;


/**
 * This class generates a requirements coverage report in HTML from a
 * {@link TestCoverage} instance
 *
 * @version     $Revision: 1.6 $ $Date: 2004/02/03 21:52:08 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         TestCoverage
 */
public class CoverageReport {

    /**
     * The path to locate stylesheets
     */
    private String _path;

    /**
     * The test suite's coverage of requirements
     */
    private TestCoverage _coverage;

    /**
     * The provider configuration
     */
    private ProviderLoader _provider;

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(CoverageReport.class);

    /**
     * The file name of the raw report
     */
    private static final String COVERAGE_XML = "coverage.xml";

    /**
     * The file name of the coverage stylesheet
     */
    private static final String COVERAGE_STYLESHEET = "resources/coverage.xsl";

    /**
     * The file name of the xdoc report
     */
    private static final String COVERAGE_XDOC = "coverage.xml";


    /**
     * Construct an instance to generate a report from an existing coverage.xml
     * file
     *
     * @param path the path to locate stylesheets
     */
    public CoverageReport(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Argument 'path' is null");
        }
        _path = path;
    }

    /**
     * Construct an instance with the test suite's coverage of requirements
     *
     * @param path the path to locate stylesheets
     * @param coverage the test suite's coverage of requirements
     * @param provider the provider loader
     */
    public CoverageReport(String path, TestCoverage coverage,
                          ProviderLoader provider) {
        if (path == null) {
            throw new IllegalArgumentException("Argument 'path' is null");
        }
        if (coverage == null) {
            throw new IllegalArgumentException("Argument 'coverage' is null");
        }
        if (provider == null) {
            throw new IllegalArgumentException("Argument 'provider' is null");
        }
        _path = path;
        _coverage = coverage;
        _provider = provider;
    }

    /**
     * Generate the coverage report
     *
     * @param dir the directory path to write the report to
     * @throws IOException if the specified file is not found or if some other
     * I/O error occurs
     * @throws MarshalException if the coverage report cannot be written
     * @throws ValidationException if the coverage report doesn't comply
     * with the XML Schema definition
     * @throws TransformerException if the transformation fails
     */
    public void report(String dir)
        throws IOException, MarshalException, TransformerException,
               ValidationException {

        if (_coverage != null) {
            RequirementCoverage coverage = _coverage.getCoverage();
            coverage.setProvider(_provider.getName());
            String path = dir + "/" + COVERAGE_XML;
            if (log.isDebugEnabled()) {
                log.debug("Writing coverage report to " + path);
            }
            FileWriter stream = new FileWriter(path);
            try {
                coverage.marshal(stream);
            } finally {
                stream.close();
            }
        }
        transform(dir);
    }

    /**
     * Transforms all xdocs to HTML
     *
     * @param dir the base directory
     * @throws IOException if an I/O error occurs
     * @throws TransformerException if the transformation fails
     */
    private void transform(String dir)
        throws IOException, TransformerException {

        File input = new File(dir, COVERAGE_XML);
        File xdocs = new File(dir, "xdocs");
        if (!xdocs.exists() && !xdocs.mkdir()) {
            throw new IOException("Directory " + xdocs
                                  + " could not be created");
        }

        File html = new File(dir, "html");
        if (!html.exists() && !html.mkdir()) {
            throw new IOException("Directory " + html
                                  + " could not be created");
        }

        File coverageXdoc = new File(xdocs, COVERAGE_XDOC);
        File stylesheet = new File(_path, COVERAGE_STYLESHEET);

        TransformerHelper.transform(input, coverageXdoc, stylesheet);
        TransformerHelper.transformAllXDoc2HTML(xdocs, html, new File(_path));
    }

    /**
     * Main line
     *
     * @param args the command line arguments
     * @throws Exception for any error
     */
    public static void main(String[] args) throws Exception {
        final String path = "path";

        Options options = new Options();
        options.addOption(path, true, "the output path");

        CommandLineParser parser = new GnuParser();
        CommandLine commands = parser.parse(options, args);

        String dir = commands.getOptionValue(path);
        if (dir == null) {
            throw new Exception("-" + path + " <path> parameter not set");
        }

        CoverageReport report = new CoverageReport("./");
        report.report(dir);
    }

}
