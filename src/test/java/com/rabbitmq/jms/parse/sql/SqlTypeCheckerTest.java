package com.rabbitmq.jms.parse.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SqlTypeCheckerTest {

    @Test
    public void basicBoolType() {
        assertType(SqlProduction.expression, "TRUE", SqlExpressionType.BOOL);
        assertType(SqlProduction.expression, "FALSE", SqlExpressionType.BOOL);
        assertType(SqlProduction.expression, "1=1", SqlExpressionType.BOOL);
        assertType(SqlProduction.expression, "(((1=1)))", SqlExpressionType.BOOL);
    }

    @Test
    public void basicInvalidType() {
        assertType(SqlProduction.expression, "TRUE='a'", SqlExpressionType.INVALID);
        assertType(SqlProduction.expression, "'b'<'a'", SqlExpressionType.INVALID);
        assertType(SqlProduction.expression, "TRUE and ('b'<'a')", SqlExpressionType.INVALID);
        assertType(SqlProduction.expression, "+TRUE", SqlExpressionType.INVALID);
        assertType(SqlProduction.expression, "Not (((-a)))", SqlExpressionType.INVALID);
    }

    @Test
    public void basicIdentType() {
        assertType(SqlProduction.expression, "a", SqlExpressionType.ANY);
        assertType(SqlProduction.expression, "a=b", SqlExpressionType.BOOL);
        assertType(SqlProduction.expression, "a<b", SqlExpressionType.BOOL);
        assertType(SqlProduction.expression, "a+b", SqlExpressionType.ARITH);
        assertType(SqlProduction.expression, "-a", SqlExpressionType.ARITH);
    }

    @Test
    public void listType() {
        assertType(SqlProduction.simple, "a in ('a','b')", SqlExpressionType.BOOL);
    }

    @Test
    public void likeType() {
        assertType(SqlProduction.simple, "a like 'a'", SqlExpressionType.BOOL);
        assertType(SqlProduction.simple, "a like 'a' escape ''''", SqlExpressionType.BOOL);
    }

    private void assertType(SqlProduction sct, String inStr, SqlExpressionType expType) {
        SqlTokenStream stream = new SqlTokenStream(inStr);
        assertEquals("Residue not empty", "", stream.getResidue());

        SqlParseTree stn = sct.parse(stream);
        assertNotNull("parse failed", stn);

        assertTrue("Stream not fully used, next token is: "+ stream.readToken(), !stream.moreTokens());

        assertEquals("Unexpected expression type", expType, SqlTypeChecker.deriveExpressionType(stn));
    }

}
