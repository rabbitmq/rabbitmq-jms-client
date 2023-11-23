/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.parse.sql;

import java.util.List;

import com.rabbitmq.jms.parse.Visitor;

/**
 * This visitor accumulates a compiled version of the tree (code) in the form of an Erlang term.
 * It visits the tree node (parent) both before and after the subtree nodes (children), so as to
 * bracket the generated code correctly.
 */
class SqlCompilerVisitor implements Visitor<SqlTreeNode> {
    private static final boolean BEFORE = true;
    private static final boolean AFTER = false;

    private final StringBuilder sb = new StringBuilder();

    @Override
    public boolean visitBefore(SqlTreeNode parent, SqlTreeNode[] children) {
        codeOf(BEFORE, this.sb, parent, children);
        return true; // traverse the whole tree
    }

    @Override
    public boolean visitAfter(SqlTreeNode parent, SqlTreeNode[] children) {
        codeOf(AFTER, this.sb, parent, children);
        return true; // traverse the whole tree
    }

    public String extractCode() {
        if (this.sb.length() == 0) return ""; // nothing to extract
        // a leading comma is removed
        return this.sb.substring((this.sb.charAt(0) == ',') ? 1 : 0);
    }

    /**
     * Reset code accumulator -- used in unit tests
     */
    void clearCode() {
        this.sb.setLength(0);
    }

    /**
     * @param before - true when this is called before the children are called (prefix), false when this is called after the children are called (postfix).
     * @param sb - accumulates the code expression
     * @param parent - the parent node
     * @param children - the array of children nodes
     */
    private static final void codeOf(boolean before, StringBuilder sb, SqlTreeNode parent, SqlTreeNode[] children) {
        switch(parent.treeType()) {
        case CONJUNCTION:
            if (before) sb.append(",{").append(atomString(SqlTokenType.AND.opCode()));
            else        sb.append('}');
            break;
        case DISJUNCTION:
            if (before) sb.append(",{").append(atomString(SqlTokenType.OR.opCode()));
            else        sb.append('}');
            break;
        case LEAF:
            switch(parent.value().type()) {
            case FALSE:
            case TRUE:
                if (before) sb.append(',').append(atomString(parent.value().type().opCode()));
                break;
            case FLOAT:
                if (before) sb.append(',').append(parent.value().getFloat());
                break;
            case HEX:
                if (before) sb.append(',').append(parent.value().getHex());
                break;
            case IDENT:
                if (before) sb.append(',').append(identString(parent.value().getIdent()));
                break;
            case INT:
                if (before) sb.append(',').append(parent.value().getLong());
                break;
            case LIST:
                if (before) sb.append(',').append(stringListString(parent.value().getList()));
                break;
            case STRING:
                if (before) sb.append(',').append(binString(parent.value().getString()));
                break;
            default:
                throw new RuntimeException(String.format( "Cannot compile tree type: [%s] with token [%s]."
                                                        , parent.treeType(), parent.value()
                                                        ));
            }
            break;
        case LIST:
            if (before) sb.append(',').append(stringListString(parent.value().getList()));
            break;
        case PATTERN1:
            if (before) ;
            else        sb.append(',').append(atomString("no_escape"));
            break;
        case PATTERN2:
            break;      // only a list of arguments is required
        case POSTFIXUNARYOP:
        case PREFIXUNARYOP:
        case TERNARYOP:
        case BINARYOP:
            switch (parent.value().type()) {
            case BETWEEN:
            case NOT:
            case NOT_BETWEEN:
            case NOT_NULL:
            case NULL:
            case OP_MINUS:
            case OP_PLUS:
            case CMP_EQ:
            case CMP_GT:
            case CMP_GTEQ:
            case CMP_LT:
            case CMP_LTEQ:
            case CMP_NEQ:
            case IN:
            case LIKE:
            case NOT_IN:
            case NOT_LIKE:
            case OP_DIV:
            case OP_MULT:
                if (before) sb.append(",{").append(atomString(parent.value().type().opCode()));
                else        sb.append('}');
                break;
            default:
                throw new RuntimeException(String.format( "Internal error: cannot compile tree type: [%s] with token [%s]."
                                                        , parent.treeType(), parent.value()
                                                        ));
            }
            break;
        default:
            throw new RuntimeException(String.format( "Internal error: cannot compile tree type: [%s]."
                                                    , parent.treeType()
                                                    ));
        }
    }

    private static StringBuilder atomString(String str) {
        return new StringBuilder().append('\'').append(str).append('\'');
    }

    private static <Obj> StringBuilder prepend(Obj obj, StringBuilder sb) {
        return sb.insert(0, obj);
    }

    private static StringBuilder identString(String identName) {
        return prepend('{', atomString("ident")).append(',').append(binString(identName)).append('}');
    }

    /**
     * Produce Erlang list of binary string literals from <b>list</>.
     * @param list - list of strings to convert
     * @return string whose contents is an Erlang expression denoting a list of binary string literals
     */
    private static StringBuilder stringListString(List<String> list) {
        if (list.size() == 0) return new StringBuilder("[]");
        StringBuilder sb = new StringBuilder();
        for(String s : list) {
            sb.append(',').append(binString(s));
        }
        return sb.replace(0, 1, "[").append(']');
    }

    /**
     * Produce Erlang binary string literal from <b>str</b>.  Result is ‘<code>&lt;&lt;"</code><b>str</b><code>"&gt;&gt;</code>’
     * but with ‘<code>"</code>’s and ‘<code>\</code>’s in <b>str</b> backslash escaped.
     * @param str - string to convert to literal
     * @return string whose contents is an Erlang expression denoting a binary string literal
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
