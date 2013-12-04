package com.rabbitmq.jms.parse.sql;

import static com.rabbitmq.jms.parse.ParseTreeTraverser.traversePostOrder;

import java.util.Collections;
import java.util.Map;

import com.rabbitmq.jms.parse.ParseTree;

public class SqlTypeChecker {

    static SqlExpressionType deriveExpressionType(ParseTree<SqlTreeNode> tree, Map<String, SqlExpressionType> identifierType) {
        traversePostOrder(tree, new SqlTypeSetterVisitor(identifierType));
        return tree.getNode().getExpType();
    }

    static SqlExpressionType deriveExpressionType(ParseTree<SqlTreeNode> tree) {
        return deriveExpressionType(tree, Collections.<String, SqlExpressionType> emptyMap());
    }

}
