package com.rabbitmq.jms.parse.sql;

import java.util.ArrayList;

import com.rabbitmq.jms.parse.ParseTree;

public class SqlParseTree implements ParseTree<SqlTreeNode> {

    private final SqlTreeNode node;
    private final SqlParseTree[] children;

    public SqlParseTree(SqlTreeNode node, SqlParseTree... children) {
        this.node = node;
        this.children = children;
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

    /* (non-Javadoc)
     * This implementation is a no-op and does not traverse the tree.
     * @see sp.parse.ParseTree#visit(java.lang.Object)
     */
    @Override
    public boolean visit(Object obj) {
        return false;
    }

    public String[] formattedTree() {
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
}
