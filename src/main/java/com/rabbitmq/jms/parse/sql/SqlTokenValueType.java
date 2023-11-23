/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.parse.sql;

/**
 * The types of the values associated with {@link SqlToken}s and sub-expressions.
 * <p>
 * Each token can have an associated {@link String} which is interpreted according to the {@link SqlTokenValueType} term
 * associated with its {@link SqlTokenType}.
 * </p>
 * <p>
 * The NO_VALUE value indicates that there is no String associated with the token (it is a literal term standing for
 * itself, for example the left parenthesis <code>(</code>, or the keyword <code>NOT</code>.
 * </p>
 */
enum SqlTokenValueType {
    NO_VALUE,
    STRING,
    IDENT,
    LONG,
    HEX,
    FLOAT,
    LIST,
    BOOL;
}
