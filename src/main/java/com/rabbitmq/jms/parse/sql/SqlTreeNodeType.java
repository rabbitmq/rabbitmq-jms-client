/**
 *
 */
package com.rabbitmq.jms.parse.sql;

import java.util.ArrayList;
import java.util.List;

/**
 * @author spowell
 *
 */
public enum SqlTreeNodeType {
    LEAF           {@Override SqlParseTree tree(SqlParseTree[] children) { return children[0]; } } ,
    DISJUNCTION    {@Override SqlParseTree tree(SqlParseTree[] children) { return new SqlParseTree(this.treeNode(), children[0], children[2]); } } ,
    CONJUNCTION    {@Override SqlParseTree tree(SqlParseTree[] children) { return new SqlParseTree(this.treeNode(), children[0], children[2]); } } ,
    POSTFIXUNARYOP {@Override SqlParseTree tree(SqlParseTree[] children) { return new SqlParseTree(this.treeNode(children[1].getNode().nodeValue()), children[0]); } } ,
    PREFIXUNARYOP  {@Override SqlParseTree tree(SqlParseTree[] children) { return new SqlParseTree(this.treeNode(children[0].getNode().nodeValue()), children[1]); } },
    BINARYOP       {@Override SqlParseTree tree(SqlParseTree[] children) { return new SqlParseTree(this.treeNode(children[1].getNode().nodeValue()), children[0], children[2]); } },
    TERNARYOP      {@Override SqlParseTree tree(SqlParseTree[] children) { return new SqlParseTree(this.treeNode(children[1].getNode().nodeValue()), children[0], children[2], children[4]); } },
    COLLAPSE1      {@Override SqlParseTree tree(SqlParseTree[] children) { return children[0]; } } ,
    COLLAPSE2      {@Override SqlParseTree tree(SqlParseTree[] children) { return children[1]; } } ,
    PATTERN1       {@Override SqlParseTree tree(SqlParseTree[] children) { return new SqlParseTree(this.treeNode(), children[0]); } } ,
    PATTERN2       {@Override SqlParseTree tree(SqlParseTree[] children) { return new SqlParseTree(this.treeNode(), children[0], children[2]); } } ,
    LIST           {@Override SqlParseTree tree(SqlParseTree[] children) { return new SqlParseTree(this.treeNode(new SqlToken(SqlTokenType.LIST, newList(children[0].getNode().nodeValue().getString())))); } } ,
    ;

    protected SqlTreeNode treeNode() {
        return new SqlTreeNode(this);
    }
    protected SqlTreeNode treeNode(SqlToken value) {
        return new SqlTreeNode(this, value);
    }

    abstract SqlParseTree tree(SqlParseTree[] children);

    SqlParseTree tree(SqlToken value) {
        return new SqlParseTree(this.treeNode(value));
    }

    private final static List<String> newList(String s) { List<String> al = new ArrayList<String>(); al.add(s); return al; }
}
