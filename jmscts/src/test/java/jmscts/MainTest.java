package jmscts;

import java.io.File;

import junit.framework.TestCase;

import org.exolab.jmscts.test.ComplianceTestSuite;

public class MainTest extends TestCase {

    public void testAll() throws Exception {
        System.setProperty("jmscts.home", System.getProperty("basedir"));
        if (System.getProperty("rabbit.jms.terminationTimeout") == null) {
            System.setProperty("rabbit.jms.terminationTimeout", "5000");
        }
        ComplianceTestSuite.main(new String[] { "-filter",
                                                new File(System.getProperty("basedir"), "config/filter.xml").getAbsolutePath() });
    }

}
