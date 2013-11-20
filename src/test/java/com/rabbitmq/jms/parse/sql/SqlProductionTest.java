package com.rabbitmq.jms.parse.sql;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.rabbitmq.jms.util.TimeTracker;

public class SqlProductionTest {

    @Test
    public void expression() throws Exception {
        assertParse(SqlProduction.expression, "nothing IS NULL"
                   , "POSTFIXUNARYOP: IS NULL"
                   , "    LEAF: identifier: nothing"
                   );
        assertParse(SqlProduction.expression, "nothing IS NULL And JMSPrefix > 12-4"
                   , "CONJUNCTION:"
                   , "    POSTFIXUNARYOP: IS NULL"
                   , "        LEAF: identifier: nothing"
                   , "    BINARYOP: >"
                   , "        LEAF: identifier: JMSPrefix"
                   , "        BINARYOP: -"
                   , "            LEAF: integer: 12"
                   , "            LEAF: integer: 4"
                   );
        assertParse(SqlProduction.expression, "nothing IS NulL oR JMSPrefix > 12- 4"
                   , "DISJUNCTION:"
                   , "    POSTFIXUNARYOP: IS NULL"
                   , "        LEAF: identifier: nothing"
                   , "    BINARYOP: >"
                   , "        LEAF: identifier: JMSPrefix"
                   , "        BINARYOP: -"
                   , "            LEAF: integer: 12"
                   , "            LEAF: integer: 4"
                   );
        assertParse(SqlProduction.expression, "nothing IS NulL oR (JMSPrefix >= 12/-4.3)"
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
    public void not_expr() throws Exception {
        assertParse(SqlProduction.not_expr,  "not 2 < 3"
                    , "PREFIXUNARYOP: NOT"
                    , "    BINARYOP: <"
                    , "        LEAF: integer: 2"
                    , "        LEAF: integer: 3"
                   );
    }

    @Test
    public void cmp_expr() throws Exception {
        assertParse(SqlProduction.cmp_expr,
                    "2.5 < 3"
                    , "BINARYOP: <"
                    , "    LEAF: float: 2.5"
                    , "    LEAF: integer: 3"
                   );
        assertParse(SqlProduction.cmp_expr,
                    "2.5 between 2 and 3"
                    , "TERNARYOP: BETWEEN"
                    , "    LEAF: float: 2.5"
                    , "    LEAF: integer: 2"
                    , "    LEAF: integer: 3"
                   );
        assertParse(SqlProduction.cmp_expr,
                    "2.5 not between 2 and 3"
                    , "TERNARYOP: NOT BETWEEN"
                    , "    LEAF: float: 2.5"
                    , "    LEAF: integer: 2"
                    , "    LEAF: integer: 3"
                   );
    }

    @Test
    public void arith_expr() throws Exception {
        assertParse(SqlProduction.arith_expr,
                    "2.5 * 2 - 3"
                    , "BINARYOP: -"
                    , "    BINARYOP: *"
                    , "        LEAF: float: 2.5"
                    , "        LEAF: integer: 2"
                    , "    LEAF: integer: 3"
                   );
    }

    @Test
    public void sign_expr() throws Exception {
        assertParse(SqlProduction.sign_expr,
                    "--+-23.3"
                    , "PREFIXUNARYOP: -"
                    , "    PREFIXUNARYOP: -"
                    , "        PREFIXUNARYOP: +"
                    , "            PREFIXUNARYOP: -"
                    , "                LEAF: float: 23.3"
                   );
    }

    @Test
    public void stringlist() throws Exception {
        assertParse(SqlProduction.stringlist,
                    "( '', 'asd' )"
                    , "LIST: list: [, asd]"
                   );
        assertParse(SqlProduction.stringlist,
                    "( 'a', 'b', 'c' )"
                    , "LIST: list: [a, b, c]"
                   );
    }

    @Test
    public void STRING() throws Exception {
        assertParse(SqlProduction.STRING,
                    "'asd'"
                    , "LEAF: string: 'asd'"
                   );
    }

    @Test
    public void pattern() throws Exception {
        assertParse(SqlProduction.pattern,
                    "'asd'"
                    , "PATTERN1:"
                    , "    LEAF: string: 'asd'"
                   );
        assertParse(SqlProduction.pattern,
                    "'asd' escape '`'"
                    , "PATTERN2:"
                    , "    LEAF: string: 'asd'"
                    , "    LEAF: string: '`'"
                   );
    }

    @Test
    public void slow_expression() throws Exception {
        TimeTracker tt = new TimeTracker(500, TimeUnit.MILLISECONDS);
        assertParse(SqlProduction.expression,
                    "(((-1)))"
                    , "PREFIXUNARYOP: -"
                    , "    LEAF: integer: 1"
                   );
        assertFalse("Took too long", tt.timedOut());
    }

    @Test
    public void simple() throws Exception {
        assertParse(SqlProduction.simple,
                    "(asd + 1)"
                    , "BINARYOP: +"
                    , "    LEAF: identifier: asd"
                    , "    LEAF: integer: 1"
                   );
        assertParse(SqlProduction.simple,
                    "  true "
                    , "LEAF: TRUE"
                   );
        assertParse(SqlProduction.simple,
                    "FalSe"
                    , "LEAF: FALSE"
                   );
        assertParse(SqlProduction.simple,
                    "fred is null"
                    , "POSTFIXUNARYOP: IS NULL"
                    , "    LEAF: identifier: fred"
                   );
        assertParse(SqlProduction.simple,
                    "fred is not null"
                    , "POSTFIXUNARYOP: IS NOT NULL"
                    , "    LEAF: identifier: fred"
                   );
        assertParse(SqlProduction.simple,
                    "fred iN  ('a' ,'b')"
                    , "BINARYOP: IN"
                    , "    LEAF: identifier: fred"
                    , "    LIST: list: [a, b]"
                   );
        assertParse(SqlProduction.simple,
                    "fred nOt  iN  ('a' ,'b')"
                    , "BINARYOP: NOT IN"
                    , "    LEAF: identifier: fred"
                    , "    LIST: list: [a, b]"
                   );
        assertParse(SqlProduction.simple,
                    "fred like  'a*'"
                    , "BINARYOP: LIKE"
                    , "    LEAF: identifier: fred"
                    , "    PATTERN1:"
                    , "        LEAF: string: 'a*'"
                   );
        assertParse(SqlProduction.simple,
                    "fred like  'a*' escape '\\'"
                    , "BINARYOP: LIKE"
                    , "    LEAF: identifier: fred"
                    , "    PATTERN2:"
                    , "        LEAF: string: 'a*'"
                    , "        LEAF: string: '\\'"
                   );
        assertParse(SqlProduction.simple,
                    "fred like  'a' escape ''''"
                    , "BINARYOP: LIKE"
                    , "    LEAF: identifier: fred"
                    , "    PATTERN2:"
                    , "        LEAF: string: 'a'"
                    , "        LEAF: string: ''''"
                   );
    }

    private static final int STACKFRAME_NESTED_PARENS_LIMIT = 350;

    @Test
    public void stackDepth() throws Exception {
        assertParse(SqlProduction.simple,
                    parens(STACKFRAME_NESTED_PARENS_LIMIT,"1")
                   , "LEAF: integer: 1"
                   );
    }

    private String parens(int num, String base) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<num; ++i) sb.append('(');
        sb.append(base);
        for (int i=0; i<num; ++i) sb.append(')');
        return sb.toString();
    }

    private void assertParse(SqlProduction sct, String inStr, String... outStr) {
        SqlTokenStream stream = new SqlTokenStream(inStr);
        assertEquals("Residue not empty", "", stream.getResidue());

        SqlParseTree stn = sct.parse(stream);
        assertNotNull("parse failed", stn);

        assertTrue("Stream not fully used", !stream.moreTokens());

        String[] formatted = stn.formattedTree();
        assertArrayEquals("Parsed tree doesn't match", outStr, formatted);
    }

}
