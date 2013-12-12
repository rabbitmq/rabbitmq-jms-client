package com.rabbitmq.jms.parse.sql;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class SqlParserTest {

    @Test
    public void simpleTree() throws Exception {
        assertParse( "nothing IS NULL"
                   , "POSTFIXUNARYOP: is_null"
                   , "    LEAF: ident: nothing"
                   );
    }

    @Test
    public void moreTrees() throws Exception {
        assertParse( "nothing IS NULL And JMSPrefix > 12-4"
                   , "CONJUNCTION:"
                   , "    POSTFIXUNARYOP: is_null"
                   , "        LEAF: ident: nothing"
                   , "    BINARYOP: >"
                   , "        LEAF: ident: JMSPrefix"
                   , "        BINARYOP: -"
                   , "            LEAF: integer: 12"
                   , "            LEAF: integer: 4"
                   );
        assertParse( "nothing IS NulL oR JMSPrefix > 12- 4"
                   , "DISJUNCTION:"
                   , "    POSTFIXUNARYOP: is_null"
                   , "        LEAF: ident: nothing"
                   , "    BINARYOP: >"
                   , "        LEAF: ident: JMSPrefix"
                   , "        BINARYOP: -"
                   , "            LEAF: integer: 12"
                   , "            LEAF: integer: 4"
                   );
        assertParse( "nothing IS NulL oR (JMSPrefix >= 12/-4.3)"
                   , "DISJUNCTION:"
                   , "    POSTFIXUNARYOP: is_null"
                   , "        LEAF: ident: nothing"
                   , "    BINARYOP: >="
                   , "        LEAF: ident: JMSPrefix"
                   , "        BINARYOP: /"
                   , "            LEAF: integer: 12"
                   , "            PREFIXUNARYOP: -"
                   , "                LEAF: float: 4.3"
                   );
    }

    @Test
    public void incompleteParse() throws Exception {
        SqlTokenStream tokenStream = new SqlTokenStream("not 2 < 3 (-1)");
        SqlParser sp = new SqlParser(tokenStream);
        sp.parse();

        if ("".equals(tokenStream.getResidue()) && sp.parseOk()) {
            fail("Parse did not fail!");
        } else {
            assertEquals( "Parse did not fail with the right message."
                        , "Terminated before end of stream; next token is: '(' at index: 4."
                        , sp.getErrorMessage()
                        );
        }
    }

    @Test
    public void unopenedQuoteParse() throws Exception {
        SqlTokenStream tokenStream = new SqlTokenStream("'abc' = abc'");
        SqlParser sp = new SqlParser(tokenStream);
        sp.parse();

        if ("".equals(tokenStream.getResidue()) && sp.parseOk()) {
            fail("Parse did not fail!");
        } else {
            assertEquals( "Parse did not fail correctly. Residue not valid."
                        , "'"
                        , tokenStream.getResidue()
                        );
        }
    }

    private void assertParse(String inStr, String... outStr) {
        SqlTokenStream stream = new SqlTokenStream(inStr);
        assertEquals("Residue not empty", "", stream.getResidue());

        SqlParser sp = new SqlParser(stream);
        SqlParseTree pt = sp.parse();

        if (!sp.parseOk()) {
            fail(sp.getErrorMessage());
        }

        String[] formatted = pt.formattedTree();
        assertArrayEquals("Parsed tree doesn't match", outStr, formatted);
    }

}
