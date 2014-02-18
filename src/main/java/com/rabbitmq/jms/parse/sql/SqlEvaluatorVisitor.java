/* Copyright (c) 2014 Pivotal Software, Inc. All rights reserved. */
package com.rabbitmq.jms.parse.sql;

import java.util.Map;

import com.rabbitmq.jms.parse.Visitor;

/**
 * This visitor evaluates an SQL expression in the form of an {@link SqlParseTree} by traversing the
 * nodes of the tree and building expression values of parent nodes from values of the children.
 * It is, in essence, an interpreter. It takes, as instantiation parameter, a context in which
 * the expression is evaluated (a map from identifiers to values).
 */
class SqlEvaluatorVisitor implements Visitor<SqlTreeNode> {

    private final Map<String, Object> env;

    SqlEvaluatorVisitor(Map<String, Object> env) {
        this.env = env; // identifier values
    }

    @Override
    public boolean visitBefore(SqlTreeNode parent, SqlTreeNode[] children) {
        return true;
    }

    @Override
    public boolean visitAfter(SqlTreeNode parent, SqlTreeNode[] children) {
        // TODO Auto-generated method stub
        return false;
    }

}
