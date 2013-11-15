package com.rabbitmq.jms.parse.sql;


/**
 * These are the nodes of an Sql Parse Tree.
 * @author spowell
 *
 */
public class SqlTreeNode {

    private final SqlTreeNodeType treeNodeType;
    private final SqlToken nodeValue;               // null value means children

    SqlTreeNode(SqlTreeNodeType treeNodeType, SqlToken nodeValue) {
        this.treeNodeType = treeNodeType;
        this.nodeValue = nodeValue;
    }

    SqlTreeNode(SqlTreeNodeType treeNodeType) {
        this(treeNodeType, null);
    }

    SqlToken nodeValue()       { return this.nodeValue; }
    SqlTreeNodeType nodeType() { return this.treeNodeType; }

}
