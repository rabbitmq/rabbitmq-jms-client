package com.rabbitmq.jms.parse.sql;

import java.util.List;

import com.rabbitmq.jms.parse.Visitor;


/**
 * This visitor sets a compiled version of the tree (code) in the parent node in the form of an Erlang term.
 * It assumes that it visits the tree in post-order, so all the children already have their
 * compiled code set.
 */
public class SqlCompilerVisitor implements Visitor<SqlTreeNode> {

    private final StringBuilder sb = new StringBuilder();

    @Override
    public boolean visitBefore(SqlTreeNode parent, SqlTreeNode[] children) {
        codeOf(true, this.sb, parent, children);
        return true; // traverse the whole tree
    }

    @Override
    public boolean visitAfter(SqlTreeNode parent, SqlTreeNode[] children) {
        codeOf(false, this.sb, parent, children);
        return true; // traverse the whole tree
    }

    /**
     * @param before - true when this is called before the children are called (prefix), false when this is called after the children are called (postfix).
     * @param sb - accumulates the code expression
     * @param parent - the parent node
     * @param children - the array of children nodes
     */
    private static final void codeOf(boolean before, StringBuilder sb, SqlTreeNode parent, SqlTreeNode[] children) {
        switch(parent.treeType()) {
        case BINARYOP:
            if (before) sb.append(",{'").append(parent.value().type().description()).append('\'');
            else        sb.append('}');
            break;
        case CONJUNCTION:
            if (before) sb.append(",{'and'");
            else        sb.append('}');
            break;
        case DISJUNCTION:
            if (before) sb.append(",{'or'");
            else        sb.append('}');
            break;
        case LEAF:
            switch(parent.value().type()) {
            case FALSE:
            case TRUE:
                if (before) sb.append(",'").append(parent.value().type().description());
                else        sb.append('\'');
                break;
            case FLOAT:
                if (before) sb.append(',');
                else        sb.append(parent.value().getFloat());
                break;
            case HEX:
                if (before) sb.append(',');
                else        sb.append(parent.value().getHex());
                break;
            case IDENT:
                if (before) sb.append(",{'ident',");
                else        sb.append(binString(parent.value().getIdent())).append('}');
                break;
            case INT:
                if (before) sb.append(',');
                else        sb.append(parent.value().getLong());
                break;
            case LIST:
                if (before) sb.append(",[").append(headString(parent.value().getList()));
                else        sb.append(tailString(parent.value().getList())).append(']');
                break;
            case STRING:
                if (before) sb.append(',');
                else        sb.append(binString(parent.value().getString()));
                break;
            default:
                throw new RuntimeException(String.format( "Tree type: %s has %s token value."
                                                        , parent.treeType(), parent.value()
                                                        ));
            }
            break;
        case LIST:
            if (before) sb.append(",[").append(headString(parent.value().getList()));
            else        sb.append(tailString(parent.value().getList())).append(']');
            break;
        case COLLAPSE1:
        case COLLAPSE2:
        case JOINLIST:
            throw new RuntimeException(String.format( "Tree type: %s should not occur."
                                                    , parent.treeType()
                                                    ));
        case PATTERN1:
        case PATTERN2:
        case POSTFIXUNARYOP:
        case PREFIXUNARYOP:
        case TERNARYOP:
            throw new RuntimeException(String.format( "Tree type: %s is not implemented."
                                                    , parent.treeType()
                                                    ));
        default:
            throw new RuntimeException(String.format( "Tree type: %s is not supported."
                                                    , parent.treeType()
                                                    ));
        }
    }

    private static StringBuilder tailString(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for(int i=1; i<list.size(); ++i) {
            sb.append(',').append(binString(list.get(i)));
        }
        return sb;
    }

    private static StringBuilder headString(List<String> list) {
        return binString(list.get(0));
    }

    /**
     * Produce Erlang binary string literal from <b>str</b>.  Result is ‘<code>&lt;&lt;"</code><b>str</b><code>"&gt;&gt;</code>’
     * but with ‘<code>"</code>’s and ‘<code>\</code>’s in <b>str</b> backslash escaped.
     * @param str - string to convert to literal
     * @return string whose contents is an Erlang binary string literal expression
     */
    private static StringBuilder binString(String str) {
        StringBuilder sb = new StringBuilder("<<\"");
        for(char c : str.toCharArray()) {
            if (c == '"' || c == '\\') sb.append('\\');
            sb.append(c);
        }
        return sb.append("\">>");
    }

}
