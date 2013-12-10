package com.rabbitmq.jms.parse.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class SqlTokenStreamTest {

    @Test
    public void testSeries() {
        assertTokenise("1 2.0 2.1e-1 name is null  'hello world''s'  2e2 2.e2 2D 2.0f",
                       "integer: 1"
                     , "float: 2.0"
                     , "float: 0.21"
                     , "identifier: name"
                     , "IS NULL"
                     , "string: 'hello world''s'"
                     , "float: 200.0"
                     , "float: 200.0"
                     , "float: 2.0"
                     , "float: 2.0");
    }

    @Test
    public void testKeywordIdentifier() {
        assertTokenise("nothing not", "identifier: nothing", "NOT");
    }

    @Test
    public void testExpression() {
        assertTokenise("nothing IS NULL", "identifier: nothing", "IS NULL");
    }

    @Test
    public void testKeywordSequenceSpacing() {
        assertTokenise("IS  NOT NULL", "IS NOT NULL");
        assertTokenise("IS\nNOT NULL", "IS NOT NULL");
        assertTokenise("IS\tNOT NULL", "IS NOT NULL");
        assertTokenise("IS\fNOT NULL", "IS NOT NULL");
        assertTokenise("IS\rNOT NULL", "IS NOT NULL");
        assertTokenise("IS\u000BNOT NULL", "IS NOT NULL");
    }

    @Test
    public void testIdentifierError() {
        assertTokeniseFailure(" ~ABC='abc'");
    }

    @Test
    public void testSingleQuoteError() {
        assertTokeniseFailure("abc'");
    }

    @Test
    public void testGeneralSpacing() {
        assertTokenise("\n\t\f\r\u000BIS NULLify \n", "identifier: IS", "identifier: NULLify");
    }

    private void assertTokenise(String inStr, String ...strs) {
        SqlTokenStream stream = new SqlTokenStream(inStr);
        assertEquals("Residue not empty", "", stream.getResidue());
        List<String> listOut = new ArrayList<String>();
        while (stream.moreTokens()) {
            SqlToken t = stream.getNext();
            listOut.add(t.toString());
        }
        assertEquals("Parse failure", resultList(strs), listOut);
    }

    private void assertTokeniseFailure(String inStr, String ...strs) {
        SqlTokenStream stream = new SqlTokenStream(inStr);
        assertNotEquals("Residue empty", "", stream.getResidue());
    }

    private static List<String> resultList(String ... strs) {
        ArrayList<String> res = new ArrayList<String>();
        for (String str : strs) {
            res.add(str);
        }
        return res;
    }
}
