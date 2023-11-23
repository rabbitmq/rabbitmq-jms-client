/* Copyright (c) 2014-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.parse.sql;

import java.util.List;

/**
 * A selector expression value in a parse tree. This object holds the
 * expression value and the expression type for both the {@link SqlCompiler}
 * and {@link SqlEvaluator} visitors. The type and value are both settable and gettable.
 * <p>
 *
 * </p>
 */
class SqlExpressionValue {

    private SqlExpressionType expType;
    private Object expValue;
    // INVARIANT: the expValue must either be null or be an object of type consistent with expType;
    //      one of:
    //      NOT_SET(null), BOOL(Boolean), ARITH(Float, Double, Integer, Long), STRING(String), LIST(List<?>)
    //      but not INVALID.
    //      ANY can be any of the above, except LIST.

    SqlExpressionValue() {
        this(SqlExpressionType.NOT_SET, null);
    }

    private SqlExpressionValue(SqlExpressionType expType, Object expValue) {
        this.expType = expType;
        this.expValue = filterValType(expType, expValue);
    }

    SqlExpressionType getType() {
        return this.expType;
    }

    void setType(SqlExpressionType expType) {
        this.expType = expType;
        this.expValue = null;
    }

    Object getValue() {
        return this.expValue;
    }

    void setValue(Object val) {
        this.expValue = filterValType(this.expType, val);
    }

    private static final Object filterValType(SqlExpressionType type, Object val) {
        if (val == null) return null;
        switch (type) {
        case ANY:    return filter(val, String.class, Boolean.class, Float.class, Double.class, Integer.class, Long.class);
        case ARITH:  return filter(val, Float.class, Double.class, Integer.class, Long.class);
        case BOOL:   return filter(val, Boolean.class);
        case LIST:   return filter(val, List.class);
        case STRING: return filter(val, String.class);
        default:     return null;
        }
    }

    private static final Object filter(Object val, Class<?> ... clzs) {
        for (Class<?> clz : clzs) if (clz.isInstance(val)) return val;
        return null;
    }

    @Override
    public String toString() {
        return String.format("<%s: %s>", this.expType, this.expValue);
    }
}
