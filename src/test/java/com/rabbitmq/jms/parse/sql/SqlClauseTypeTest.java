package com.rabbitmq.jms.parse.sql;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SqlClauseTypeTest {

    @Test
    public void clauseExpression() throws Exception {
        assertParse(SqlClauseType.expression, "nothing IS NULL"
                   , "POSTFIXUNARYOP: IS NULL"
                   , "    LEAF: identifier: nothing"
                   );
        assertParse(SqlClauseType.expression, "nothing IS NULL And JMSPrefix > 12-4"
                   , "CONJUNCTION:"
                   , "    POSTFIXUNARYOP: IS NULL"
                   , "        LEAF: identifier: nothing"
                   , "    BINARYOP: >"
                   , "        LEAF: identifier: JMSPrefix"
                   , "        BINARYOP: -"
                   , "            LEAF: integer: 12"
                   , "            LEAF: integer: 4"
                   );
        assertParse(SqlClauseType.expression, "nothing IS NulL oR JMSPrefix > 12- 4"
                   , "DISJUNCTION:"
                   , "    POSTFIXUNARYOP: IS NULL"
                   , "        LEAF: identifier: nothing"
                   , "    BINARYOP: >"
                   , "        LEAF: identifier: JMSPrefix"
                   , "        BINARYOP: -"
                   , "            LEAF: integer: 12"
                   , "            LEAF: integer: 4"
                   );
        assertParse(SqlClauseType.expression, "nothing IS NulL oR (JMSPrefix >= 12/-4.3)"
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
    public void clauseNotExpr() throws Exception {
        assertParse(SqlClauseType.not_expr,  "not 2 < 3"
                    , "PREFIXUNARYOP: NOT"
                    , "    BINARYOP: <"
                    , "        LEAF: integer: 2"
                    , "        LEAF: integer: 3"
                   );
    }

    @Test
    public void clauseCmpExpr() throws Exception {
        assertParse(SqlClauseType.cmp_expr,
                    "2.5 between 2 and 3"
                    , "TERNARYOP: BETWEEN"
                    , "    LEAF: float: 2.5"
                    , "    LEAF: integer: 2"
                    , "    LEAF: integer: 3"
                   );
    }

    private void assertParse(SqlClauseType sct, String inStr, String... outStr) {
        SqlTokenStream stream = new SqlTokenStream(inStr);
        assertEquals("Residue not empty", "", stream.getResidue());

        SqlParseTree stn = sct.parse(stream);
        assertNotNull("parse failed", stn);

        assertTrue("Stream not fully used", !stream.moreTokens());

        String[] formatted = stn.formattedTree();
        assertArrayEquals("Parsed tree doesn't match", outStr, formatted);
    }

}
