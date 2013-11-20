package com.rabbitmq.jms.parse.sql;

import java.util.ArrayList;

import com.rabbitmq.jms.parse.ParseTree;

public class SqlParseTree implements ParseTree<SqlTreeNode> {

    private final SqlTreeNode node;
    private final SqlParseTree[] children;
    private final SqlTreeNode[] nodes;

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

    String[] formattedTree() {
        if (this.children == null) {
            return sa(this.node.toString());
        } else {
            ArrayList<String> lines = new ArrayList<String>();
            SqlToken tokVal = this.node.nodeValue();
            lines.add(this.node.nodeType().toString() + (tokVal == null ? ":" : ": " + tokVal));
            for (SqlParseTree child: this.children) {
                String[] childLines = child.formattedTree();
                for (String childLine: childLines) {
                    lines.add("    " + childLine);
                }
            }
            return lines.toArray(new String[lines.size()]);
        }
    }
    private static final String[] sa(String...ss) { return ss; }

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
