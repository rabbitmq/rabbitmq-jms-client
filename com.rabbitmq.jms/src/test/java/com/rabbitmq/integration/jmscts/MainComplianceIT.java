package com.rabbitmq.integration.jmscts;

import java.io.File;

import junit.framework.TestCase;

import org.exolab.jmscts.test.ComplianceTestSuite;

/**
 * Runs the jmscts test suite as a single (integration) test (using maven/failsafe).
 */
public class MainComplianceIT extends TestCase {

    public void testAll() throws Exception {
        if (System.getProperty("basedir")==null) {
            System.setProperty("basedir",".");
        }
        System.setProperty("jmscts.home", System.getProperty("basedir"));
        if (System.getProperty("rabbit.jms.terminationTimeout") == null) {
            System.setProperty("rabbit.jms.terminationTimeout", "5000");
        }
        ComplianceTestSuite.main(new String[] { "-filter",
                                                new File(System.getProperty("basedir"), "config/filter.xml").getAbsolutePath() });
    }

}
