/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.parse.sql;

import java.util.ArrayList;
import java.util.List;

/**
 * This determines the types of subtree allowed in a {@link SqlParseTree}.
 * The {@link #tree()} method on one of these values constructs a tree from an
 * array of children (trees) that conforms to the type it represents.
 * <p>
 * These are the tree-build actions associated with an alternative of a production
 * in {@link SqlProduction} and must be read along with the definitions in {@link SqlProduction}.
 * </p>
 * @see SqlProduction
 */
enum SqlTreeType {
    LEAF           {@Override SqlParseTree tree(SqlParseTree[] sub) { return sub[0]; } } ,
    DISJUNCTION    {@Override SqlParseTree tree(SqlParseTree[] sub) { return new SqlParseTree(this.treeNode(), sub[0], sub[2]); } } ,
    CONJUNCTION    {@Override SqlParseTree tree(SqlParseTree[] sub) { return new SqlParseTree(this.treeNode(), sub[0], sub[2]); } } ,
    POSTFIXUNARYOP {@Override SqlParseTree tree(SqlParseTree[] sub) { return new SqlParseTree(this.treeNode(sub[1].getNode().value()), sub[0]); } } ,
    PREFIXUNARYOP  {@Override SqlParseTree tree(SqlParseTree[] sub) { return new SqlParseTree(this.treeNode(sub[0].getNode().value()), sub[1]); } },
    BINARYOP       {@Override SqlParseTree tree(SqlParseTree[] sub) { return new SqlParseTree(this.treeNode(sub[1].getNode().value()), sub[0], sub[2]); } },
    TERNARYOP      {@Override SqlParseTree tree(SqlParseTree[] sub) { return new SqlParseTree(this.treeNode(sub[1].getNode().value()), sub[0], sub[2], sub[4]); } },
    PATTERN1       {@Override SqlParseTree tree(SqlParseTree[] sub) { return new SqlParseTree(this.treeNode(), sub[0]); } } ,
    PATTERN2       {@Override SqlParseTree tree(SqlParseTree[] sub) { return new SqlParseTree(this.treeNode(), sub[0], sub[2]); } } ,
    LIST           {@Override SqlParseTree tree(SqlParseTree[] sub) { return new SqlParseTree(this.treeNode(new SqlToken(SqlTokenType.LIST, newList(sub[0].getNode().value().getString())))); } } ,
    // the following node types should never appear in the final tree but are parsing artifacts
    COLLAPSE1      {@Override SqlParseTree tree(SqlParseTree[] sub) { return sub[0]; } } ,
    COLLAPSE2      {@Override SqlParseTree tree(SqlParseTree[] sub) { return sub[1]; } } ,
    JOINLIST       {@Override SqlParseTree tree(SqlParseTree[] sub) { return new SqlParseTree(this.treeNode(LIST, new SqlToken(SqlTokenType.LIST, consList(sub[0].getNode().value().getString(), sub[2].getNode().value().getList())))); } } ,
    ;

    protected SqlTreeNode treeNode() {
        return new SqlTreeNode(this);
    }
    protected SqlTreeNode treeNode(SqlToken value) {
        return new SqlTreeNode(this, value);
    }

    protected SqlTreeNode treeNode(SqlTreeType type, SqlToken value) {
        return new SqlTreeNode(type, value);
    }

    /** Construct a tree of this type from the given array of subtrees */
    abstract SqlParseTree tree(SqlParseTree[] sub);

    private static final List<String> newList(String s)  { List<String> al = new ArrayList<String>(); al.add(s); return al; }
    private static final List<String> consList(String s, List<String> al) { al.add(0,s); return al; }
}
