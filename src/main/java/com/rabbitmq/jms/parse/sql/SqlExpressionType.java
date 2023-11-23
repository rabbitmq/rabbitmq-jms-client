/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.parse.sql;

/**
 * The types of the subexpressions in an SQL (selector) expression.
 *
 */
public enum SqlExpressionType {
    NOT_SET,    // type not determined
    BOOL,       // top level expression is a logical one
    STRING,     // valid in equality comparisons
    ARITH,      // all floats, hex's, integers have arithmetic type
    ANY,        // unknown identifier means we cannot assign a type, though it may be valid when evaluated
    LIST,       // only occurs in IN expressions, and must be a list of strings
    INVALID     // a type error has occurred, and the expression cannot be typed properly (these propagate upward in a parse tree)
;

    static boolean isArith(SqlExpressionType expType) {
        return expType  == ANY || expType  == ARITH;
    }

    static boolean isString(SqlExpressionType expType) {
        return expType  == ANY || expType  == STRING;
    }

    static boolean isBool(SqlExpressionType expType) {
        return expType  == ANY || expType  == BOOL;
    }
}
