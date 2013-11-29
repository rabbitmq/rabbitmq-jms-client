package com.rabbitmq.jms.parse.sql;


/**
 * An {@link SqlParseTree} has a node at each point of the tree.
 * A Node has a type ({@link SqlTreeType}) which determines its degree and a value ({@link SqlToken}) which refines the operation this denotes.
 */
public class SqlTreeNode {

    private final SqlTreeType treeNodeType;
    private final SqlToken nodeValue;               // null value means children

    private SqlExpressionType expType = SqlExpressionType.NOT_SET;  // type of expression represented by this node (if known)

    SqlTreeNode(SqlTreeType treeNodeType, SqlToken nodeValue) {
        this.treeNodeType = treeNodeType;
        this.nodeValue = nodeValue;
    }

    SqlTreeNode(SqlTreeType treeNodeType) {
        this(treeNodeType, null);
    }

    void setExpType(SqlExpressionType expType) { this.expType = expType; }
    SqlExpressionType getExpType() { return this.expType; }

    SqlToken nodeValue()       { return this.nodeValue; }
    SqlTreeType nodeType() { return this.treeNodeType; }

}
