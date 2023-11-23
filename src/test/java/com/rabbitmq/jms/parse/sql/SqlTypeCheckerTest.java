// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.
//
// Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries.
package com.rabbitmq.jms.parse.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

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
        assertEquals("", stream.getResidue(), "Residue not empty");

        SqlParseTree stn = sct.parse(stream);
        assertNotNull(stn, "parse failed");

        assertTrue(!stream.moreTokens(), "Stream not fully used, next token is: "+ stream.readToken());

        assertEquals(expType, SqlTypeChecker.deriveExpressionType(stn), "Unexpected expression type");
    }

}
