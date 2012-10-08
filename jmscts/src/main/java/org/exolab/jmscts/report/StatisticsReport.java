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
 * $Id: StatisticsReport.java,v 1.4 2004/02/03 21:52:08 tanderson Exp $
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

import org.exolab.jmscts.core.TestStatistics;
import org.exolab.jmscts.provider.ProviderLoader;


/**
 * This class generates a requirements coverage report in HTML from a
 * {@link TestStatistics} instance
 *
 * @version     $Revision: 1.4 $ $Date: 2004/02/03 21:52:08 $
 * @author      <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @see         TestStatistics
 */
public class StatisticsReport {

    /**
     * The path to locate stylesheets
     */
    private String _path;

    /**
     * The statistics
     */
    private TestStatistics _statistics;

    /**
     * The provider configuration
     */
    private ProviderLoader _provider;

    /**
     * The logger
     */
    private static final Logger log =
        Logger.getLogger(StatisticsReport.class);

    /**
     * The file name of the raw report
     */
    private static final String STATISTICS_XML = "statistics.xml";

    /**
     * The file name of the xdoc report
     */
    private static final String XDOC_XML = "statistics-xdoc.xml";

    /**
     * The file name of the HTML report
     */
    private static final String STATISTICS_HTML = "statistics.html";

    /**
     * The file name of the statistics stylesheet
     */
    private static final String STATS_STYLESHEET =
        "resources/statistics.xsl";


    /**
     * Construct an instance to generate a report from an existing
     * statistics.xml file
     *
     * @param path the path to locate stylesheets
     */
    public StatisticsReport(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Argument 'path' is null");
        }
        _path = path;
    }

    /**
     * Construct an instance with the test suite's statistics of requirements
     *
     * @param path the path to locate stylesheets
     * @param statistics the test suite's statistics of requirements
     * @param provider the provider loader
     */
    public StatisticsReport(String path, TestStatistics statistics,
                            ProviderLoader provider) {
        if (path == null) {
            throw new IllegalArgumentException("Argument 'path' is null");
        }
        if (statistics == null) {
            throw new IllegalArgumentException(
                "Argument 'statistics' is null");
        }
        if (provider == null) {
            throw new IllegalArgumentException("Argument 'provider' is null");
        }
        _path = path;
        _statistics = statistics;
        _provider = provider;
    }

    /**
     * Generate the statistics report
     *
     * @param dir the directory path to write the report to
     * @throws IOException if the specified file is not found or if some other
     * I/O error occurs
     * @throws MarshalException if the statistics report cannot be written
     * @throws ValidationException if the statistics report doesn't comply
     * with the XML Schema definition
     * @throws TransformerException if the transformation fails
     */
    public void report(String dir)
        throws IOException, MarshalException, TransformerException,
               ValidationException {
        if (_statistics != null) {
            Statistics statistics = _statistics.getStatistics();
            statistics.setProvider(_provider.getName());
            String path = dir + "/" + STATISTICS_XML;
            if (log.isDebugEnabled()) {
                log.debug("Writing statistics report to " + path);
            }
            FileWriter stream = new FileWriter(path);
            try {
                statistics.marshal(stream);
            } finally {
                stream.close();
            }
        }
        transform(dir);
    }

    /**
     * Transform the xdoc report to HTML
     *
     * @param dir the base directory
     * @throws IOException for any I/O error
     * @throws TransformerException if transformation fails
     */
    private void transform(String dir)
        throws IOException, TransformerException {

        File input = new File(dir, STATISTICS_XML);
        File xdoc = new File(dir, XDOC_XML);
        File baseDir = new File(_path);
        File stylesheet = new File(baseDir, STATS_STYLESHEET);
        File html = new File(dir, STATISTICS_HTML);
        TransformerHelper.transform(input, xdoc, stylesheet);
        TransformerHelper.transformXDoc2HTML(xdoc, html, baseDir);
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

        StatisticsReport report = new StatisticsReport("./");
        report.report(dir);
    }

}
