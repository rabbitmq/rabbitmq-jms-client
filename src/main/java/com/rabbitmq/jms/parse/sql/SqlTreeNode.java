/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.parse.sql;

/**
 * An {@link SqlParseTree} has a node at each point of the tree.
 * A Node has a type ({@link SqlTreeType}) which determines its degree and a value ({@link SqlToken}) which refines the operation this denotes.
 */
class SqlTreeNode {

    private final SqlTreeType treeType;
    private final SqlToken value;               // null value means children

    /** type and value of expression represented by this node (if known); initially <NOT_SET: null> */
    private SqlExpressionValue expValue = new SqlExpressionValue();

    SqlTreeNode(SqlTreeType treeType, SqlToken value) {
        this.treeType = treeType;
        this.value = value;
    }

    SqlTreeNode(SqlTreeType treeType) {
        this(treeType, null);
    }

    // getter and setter for expression value - used by TypeSetter and Evaluator Visitors
    void setExpValue(SqlExpressionValue expValue) { this.expValue = expValue; }
    SqlExpressionValue getExpValue() { return this.expValue; }

    // getters for node value and type
    SqlToken    value()     { return this.value;    }
    SqlTreeType treeType()  { return this.treeType; }

    @Override
    public String toString() {
        return "SqlTreeNode [treeType=" + treeType + ", value=" + value + ", expValue=" + expValue + "]";
    }
}
