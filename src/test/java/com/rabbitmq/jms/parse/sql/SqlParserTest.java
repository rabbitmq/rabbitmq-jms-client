package com.rabbitmq.jms.parse.sql;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SqlParserTest {

    @Test
    public void simpleTree() throws Exception {
        assertParse( "nothing IS NULL"
                   , "POSTFIXUNARYOP: IS NULL"
                   , "    LEAF: identifier: nothing"
                   );
    }

    @Test
    public void moreTrees() throws Exception {
        assertParse( "nothing IS NULL And JMSPrefix > 12-4"
                   , "CONJUNCTION:"
                   , "    POSTFIXUNARYOP: IS NULL"
                   , "        LEAF: identifier: nothing"
                   , "    BINARYOP: >"
                   , "        LEAF: identifier: JMSPrefix"
                   , "        BINARYOP: -"
                   , "            LEAF: integer: 12"
                   , "            LEAF: integer: 4"
                   );
        assertParse( "nothing IS NulL oR JMSPrefix > 12- 4"
                   , "DISJUNCTION:"
                   , "    POSTFIXUNARYOP: IS NULL"
                   , "        LEAF: identifier: nothing"
                   , "    BINARYOP: >"
                   , "        LEAF: identifier: JMSPrefix"
                   , "        BINARYOP: -"
                   , "            LEAF: integer: 12"
                   , "            LEAF: integer: 4"
                   );
        assertParse( "nothing IS NulL oR (JMSPrefix >= 12/-4.3)"
                   , "DISJUNCTION:"
                   , "    POSTFIXUNARYOP: IS NULL"
                   , "        LEAF: identifier: nothing"
                   , "    BINARYOP: >="
                   , "        LEAF: identifier: JMSPrefix"
                   , "        BINARYOP: /"
                   , "            LEAF: integer: 12"
                   , "            PREFIXUNARYOP: -"
                   , "                LEAF: float: 4.3"
                   );
    }

    @Test
    public void clause3() throws Exception {
        assertParse(  "not 2 < 3"
                    , "PREFIXUNARYOP: NOT"
                    , "    BINARYOP: <"
                    , "        LEAF: integer: 2"
                    , "        LEAF: integer: 3"
                   );
    }

    @Test
    public void clause4() throws Exception {
        assertParse(  "2.5 between 2 and 3"
                    , "TERNARYOP: BETWEEN"
                    , "    LEAF: float: 2.5"
                    , "    LEAF: integer: 2"
                    , "    LEAF: integer: 3"
                   );
    }

    private void assertParse(String inStr, String... outStr) {
        SqlTokenStream stream = new SqlTokenStream(inStr);
        assertEquals("Residue not empty", "", stream.getResidue());

        SqlParser sp = new SqlParser(stream);
        SqlParseTree pt = sp.parse();
        assertNotNull("parse failed", pt);

        assertTrue("parse incomplete", sp.parseOk());

        String[] formatted = pt.formattedTree();
        assertArrayEquals("Parsed tree doesn't match", outStr, formatted);
    }

}
