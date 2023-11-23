/* Copyright (c) 2014-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.parse.sql;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rabbitmq.jms.parse.Visitor;

/**
 * This visitor evaluates an SQL expression in the form of an {@link SqlParseTree} by traversing the
 * nodes of the tree and building expression values of parent nodes from values of the children.
 * It is, in essence, an interpreter. It takes, as instantiation parameter, a context in which
 * the expression is evaluated (a map from identifiers to values).
 * <p>
 * The <code>UNKNOWN</code> value is represented by <code><b>null</b></code>.
 * </p>
 */
class SqlEvaluatorVisitor implements Visitor<SqlTreeNode> {

    private final Map<String, Object> env;

    SqlEvaluatorVisitor(Map<String, Object> env) {
        this.env = env; // identifier values
    }

    @Override
    public boolean visitBefore(SqlTreeNode parent, SqlTreeNode[] children) {
        // nothing happens on before pass
        return true;
    }

    @Override
    public boolean visitAfter(SqlTreeNode parent, SqlTreeNode[] children) {
        Object[] childVals = new Object[children.length];
        for (int i=0; i<children.length; ++i) childVals[i] = children[i].getExpValue().getValue();
        parent.getExpValue().setValue(valueOfParent(env, parent, childVals));
        return true;
    }

    private static final Object valueOfParent(Map<String, Object> env, SqlTreeNode parent, Object[] vals) {
        switch(parent.treeType()) {
        case CONJUNCTION:   return logicalAnd(vals[0], vals[1]);
        case DISJUNCTION:   return logicalOr(vals[0], vals[1]);

        case LEAF:          return leafValue(parent.value(), env);

        case LIST:          return parent.value().getList();

        case PATTERN1:      return pattern(vals[0], null);
        case PATTERN2:      return pattern(vals[0], vals[1]);

        case POSTFIXUNARYOP:
        case PREFIXUNARYOP:
        case TERNARYOP:
        case BINARYOP:      return operationValue(parent.value().type(), vals);

        default:            return null;
        }
    }

    private static final Pattern pattern(Object o1, Object o2) {
        if (!isString(o1)) return null;
        String pattString = Pattern.quote((String) o1);  // enLiteral everything to start with

        boolean noEscape = true;
        char escChar = ' ';       // ignored if noEscape==true
        if (isString(o2) && ((String)o2).length()>0) {
            noEscape=false;
            escChar=((String)o2).charAt(0);
        }

        boolean nextAsis = false;
        StringBuilder sp = new StringBuilder();
        for (char ch : pattString.toCharArray()) {
                 if (nextAsis)                 { sp.append(ch); nextAsis = false; }
            else if (!noEscape && ch==escChar)   nextAsis = true;
            else if (ch=='_')                    sp.append('.');
            else if (ch=='%')                    sp.append(".*");
            else                                 sp.append(ch);
        }
        return Pattern.compile(sp.toString());
    }

    private static final Object operationValue(SqlTokenType op, Object[] vals) {
        switch (op) {
        case NOT_BETWEEN:   return notBetween(vals[0], vals[1], vals[2]);
        case BETWEEN:       return logicalNot(notBetween(vals[0], vals[1], vals[2]));

        case CMP_EQ:        return equals(vals[0], vals[1]);
        case CMP_NEQ:       return logicalNot(equals(vals[0], vals[1]));
        case CMP_GT:        return greaterThan(vals[0], vals[1]);
        case CMP_LTEQ:      return logicalNot(greaterThan(vals[0], vals[1]));
        case CMP_LT:        return greaterThan(vals[1], vals[0]);
        case CMP_GTEQ:      return logicalNot(greaterThan(vals[1], vals[0]));

        case IN:            return in(vals[0], vals[1]);
        case NOT_IN:        return logicalNot(in(vals[0], vals[1]));

        case LIKE:          return like(vals[0], vals[1]);
        case NOT_LIKE:      return logicalNot(like(vals[0], vals[1]));

        case NULL:          return isNull(vals[0]);
        case NOT_NULL:      return logicalNot(isNull(vals[0]));

        case OP_DIV:        return divide(vals[0], vals[1]);
        // OP_MINUS may be unary prefix or binary op:
        case OP_MINUS:      return (vals.length>1 ? subtract(vals[0], vals[1]) : subtract(0L, vals[0]));
        case OP_MULT:       return multiply(vals[0], vals[1]);
        // OP_PLUS may be unary prefix or binary op:
        case OP_PLUS:       return (vals.length>1 ? add(vals[0], vals[1]) : add(vals[0], 0L));

        case NOT:           return logicalNot(vals[0]);

        default:            return null;
        }
    }

    private static final Boolean like(Object o1, Object o2) {
        // assert: o1 is an identifier value -- so may be any type or null;
        if (!isString(o1)) return null;
        if (!isPattern(o2)) return null;
        Matcher matcher = ((Pattern)o2).matcher((String)o1);
        return matcher.matches();
    }

    private static final Object add(Object o1, Object o2) {
        if (isLong(o1)) {
            if (isLong(o2)) return toLong(o1) + toLong(o2);
            else if(isDouble(o2)) return Double.valueOf(o1.toString()) + toDouble(o2);
            else return null;
        } else if (isDouble(o1)) {
            if (isDouble(o2)) return toDouble(o1) + toDouble(o2);
            else if (isLong(o2)) return toDouble(o1) + Double.valueOf(o2.toString());
            else return null;
        }
        return null;
    }

    private static final long toLong(Object o) {
        // isLong(o) is true
        if (o instanceof Long) return (Long) o;
        return ((Integer) o).longValue();
    }

    private static final boolean toBool(Object o) {
        // isBool(o) is true
        return (Boolean) o;
    }

    private static final double toDouble(Object o) {
        // isDouble(o) is true
        if (o instanceof Double) return (Double) o;
        return ((Float) o).doubleValue();
    }

    private static final Object subtract(Object o1, Object o2) {
        if (isLong(o1)) {
            if (isLong(o2)) return toLong(o1) - toLong(o2);
            else if(isDouble(o2)) return Double.valueOf(o1.toString()) - toDouble(o2);
            else return null;
        } else if (isDouble(o1)) {
            if (isDouble(o2)) return toDouble(o1) - toDouble(o2);
            else if (isLong(o2)) return toDouble(o1) - Double.valueOf(o2.toString());
            else return null;
        }
        return null;
    }

    private static final Object multiply(Object o1, Object o2) {
        if (isLong(o1)) {
            if (isLong(o2)) return toLong(o1) * toLong(o2);
            else if(isDouble(o2)) return Double.valueOf(o1.toString()) * toDouble(o2);
            else return null;
        } else if (isDouble(o1)) {
            if (isDouble(o2)) return toDouble(o1) * toDouble(o2);
            else if (isLong(o2)) return toDouble(o1) * Double.valueOf(o2.toString());
            else return null;
        }
        return null;
    }

    private static final Object divide(Object o1, Object o2) {
        if (isLong(o1)) {
            if (isLong(o2)) return toLong(o1) / toLong(o2);
            else if(isDouble(o2)) return Double.valueOf(o1.toString()) / toDouble(o2);
            else return null;
        } else if (isDouble(o1)) {
            if (isDouble(o2)) return toDouble(o1) / toDouble(o2);
            else if (isLong(o2)) return toDouble(o1) / Double.valueOf(o2.toString());
            else return null;
        }
        return null;
    }

    private static final Boolean in(Object o1, Object o2) {
        if (o1==null || o2==null) return null;
        if (!(o1 instanceof String)) return null;
        @SuppressWarnings("unchecked") // assert: this is a type-checked tree being visited
        List<String> list = (List<String>)o2;
        return list.contains((String)o1);
    }

    private static final Boolean greaterThan(Object o1, Object o2) {
        if (isLong(o1)) {
            if (isLong(o2)) return toLong(o1) > toLong(o2);
            else if(isDouble(o2)) return Double.valueOf(o1.toString()) > toDouble(o2);
            else return null;
        } else if (isDouble(o1)) {
            if (isDouble(o2)) return toDouble(o1) > toDouble(o2);
            else if (isLong(o2)) return toDouble(o1) > Double.valueOf(o2.toString());
            else return null;
        }
        return null;
    }

    private static final Boolean equals(Object o1, Object o2) {
        if (o1==null || o2==null) return null;
        if (o1 instanceof String) return o1.equals(o2);
        if (isBool(o1) && isBool(o2)) return toBool(o1) == toBool(o2);
        if (isLong(o1)) {
            if (isLong(o2)) return toLong(o1) == toLong(o2);
            else if (isDouble(o2)) return (double)toLong(o1) == toDouble(o2);
        } else if (isDouble(o1)) {
            if (isLong(o2)) return toDouble(o1) == (double)toLong(o2);
            else if (isDouble(o2)) return toDouble(o1) == toDouble(o2);
        }
        return false;
    }

    private static final boolean isNull(Object o) {
        return o==null;
    }

    private static final Boolean notBetween(Object o1, Object o2, Object o3) {
        return logicalOr(greaterThan(o2, o1), greaterThan(o1, o3));
    }

    private static final Object leafValue(SqlToken value, Map<String, Object> env) {
        switch (value.type()) {
        case TRUE:   return true;
        case FALSE:  return false;
        case FLOAT:  return value.getFloat();
        case HEX:    return value.getHex();
        case INT:    return value.getLong();
        case LIST:   return value.getList();
        case IDENT:  return env==null ? null : env.get(value.getIdent());
        case STRING: return value.getString();
        default:
            return null;
        }
    }

    /**
     * Implements the three-valued logic as in JMS spec
     */
    private static final Boolean logicalAnd(Object o1, Object o2) {
        if (isBool(o1) && !(Boolean) o1) return false;
        if (isBool(o2) && !(Boolean) o2) return false;
        if (isBool(o1) && (Boolean) o1 && isBool(o2) && (Boolean) o2) return true;
        return null;
    }

    /**
     * Implements the three-valued logic as in JMS spec
     */
    private static final Boolean logicalOr(Object o1, Object o2) {
        if (isBool(o1) && (Boolean) o1) return true;
        if (isBool(o2) && (Boolean) o2) return true;
        if (isBool(o1) && !(Boolean) o1 && isBool(o2) && !(Boolean) o2) return false;
        return null;
    }

    /**
     * Implements the three-valued logic as in JMS spec
     */
    private static final Boolean logicalNot(Object o) {
        return (!isBool(o) ? null : !(Boolean) o);
    }

    private static final boolean isLong(Object o) {
        return o!=null && o instanceof Long || o instanceof Integer;
    }

    private static final boolean isDouble(Object o) {
        return o!=null && o instanceof Double || o instanceof Float;
    }

    private static final boolean isString(Object o) {
        return o!=null && o instanceof String;
    }

    private static final boolean isBool(Object o) {
        return o!=null && o instanceof Boolean;
    }

    private static final boolean isPattern(Object o) {
        return o!=null && o instanceof Pattern;
    }
}
