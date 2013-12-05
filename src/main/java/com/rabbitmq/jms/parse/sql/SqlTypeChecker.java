package com.rabbitmq.jms.parse.sql;

import static com.rabbitmq.jms.parse.ParseTreeTraverser.traversePostOrder;

import java.util.Collections;
import java.util.Map;

import com.rabbitmq.jms.parse.ParseTree;

public abstract class SqlTypeChecker { // prevent instantiation   directly
    private SqlTypeChecker() {}        // prevent instantiation indirectly

    public static SqlExpressionType deriveExpressionType(ParseTree<SqlTreeNode> tree, Map<String, SqlExpressionType> identifierType) {
        traversePostOrder(tree, new SqlTypeSetterVisitor(identifierType));
        return tree.getNode().getExpType();
    }

    public static SqlExpressionType deriveExpressionType(ParseTree<SqlTreeNode> tree) {
        return deriveExpressionType(tree, Collections.<String, SqlExpressionType> emptyMap());
    }

}
