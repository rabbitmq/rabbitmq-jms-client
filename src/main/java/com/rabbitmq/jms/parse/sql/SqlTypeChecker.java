package com.rabbitmq.jms.parse.sql;

import static com.rabbitmq.jms.parse.ParseTreeTraverser.traversePostOrder;

import com.rabbitmq.jms.parse.ParseTree;

public class SqlTypeChecker {

    static SqlExpressionType deriveExpressionType(ParseTree<SqlTreeNode> tree) {
        traversePostOrder(tree, new SqlTypeCheckerVisitor());
        return tree.getNode().getExpType();
    }

}
