/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.parse.sql;

import java.util.ArrayList;

import com.rabbitmq.jms.parse.ParseTree;

/**
 * Tree used to hold a parsed SQL (selector) expression. The nodes are of type {@link SqlTreeNode}.
 *
 */
class SqlParseTree implements ParseTree<SqlTreeNode> {

    private final SqlTreeNode node;
    private final SqlParseTree[] children;
    private final SqlTreeNode[] nodes;

    /**
     * Tree constructor: takes the node value and the children; both are <b><code>final</code></b> fields of the tree.
     * @param node - the node value of the (root of the) tree.
     * @param children - a series of trees; omitting these arguments will imply a zero-length array of trees.
     */
    public SqlParseTree(SqlTreeNode node, SqlParseTree... children) {
        this.node = node;
        this.children = children;
        this.nodes = genNodes(children);
    }

    @Override
    public SqlTreeNode getNode() {
        return this.node;
    }

    @Override
    public int getNumberOfChildren() {
        return this.children.length;
    }

    @Override
    public SqlParseTree[] getChildren() {
        return this.children;
    }

    /**
     * @return an array of {@link String}s which form a readable representation of the tree.
     */
    String[] formattedTree() {
        ArrayList<String> lines = new ArrayList<String>();
        SqlToken tokVal = this.node.value();
        lines.add(this.node.treeType().toString() + (tokVal == null ? ":" : ": " + tokVal));
        for (SqlParseTree child: this.children) {
            String[] childLines = child.formattedTree();
            for (String childLine: childLines) {
                lines.add("    " + childLine);
            }
        }
        return lines.toArray(new String[lines.size()]);
    }

    @Override
    public SqlTreeNode[] getChildNodes() {
        return this.nodes;
    }

    private static final SqlTreeNode[] genNodes(SqlParseTree[] children) {
        SqlTreeNode[] nodes = new SqlTreeNode[children.length];
        for (int i=0; i<children.length; ++i) {
            nodes[i] = children[i].getNode();
        }
        return nodes;
    }
}
