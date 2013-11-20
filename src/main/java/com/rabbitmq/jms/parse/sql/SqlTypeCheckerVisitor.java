package com.rabbitmq.jms.parse.sql;

import com.rabbitmq.jms.parse.ParseTree;
import com.rabbitmq.jms.parse.Visitor;


/**
 * This visitor relies upon being called in postorder, and uses the children types to determine the parent type (and sets it).
 * TODO: Add state that states the type of some identifiers (e.g. JMSPriority is integer, and JMSType is String) and checks accordingly.
 *
 */
public class SqlTypeCheckerVisitor implements Visitor<SqlTreeNode> {

    @Override
    public boolean visit(ParseTree<SqlTreeNode> subtree) {
        subtree.getNode().setExpType(typeOf(subtree.getNode(), subtree.getChildNodes()));
        return true; // traverse the whole tree
    }

    private static final SqlExpressionType typeOf(SqlTreeNode root, SqlTreeNode[] subnodes) {
        switch (root.nodeType()) {
        case BINARYOP:
            switch (root.nodeValue().type()) {
            case CMP_EQ:
            case CMP_NEQ:
                return equalTypeBool(subnodes[0].getExpType(), subnodes[1].getExpType());
            case CMP_GT:
            case CMP_GTEQ:
            case CMP_LT:
            case CMP_LTEQ:
                return arithTypeBool(subnodes[0].getExpType(), subnodes[1].getExpType());
            case LIKE:
            case NOT_LIKE:
            case IN:
            case NOT_IN:
                return stringTypeBool(subnodes[0].getExpType());
            case OP_DIV:
            case OP_MINUS:
            case OP_MULT:
            case OP_PLUS:
                return arithTypeArith(subnodes[0].getExpType(), subnodes[1].getExpType());
            default:
                break;
            }
            break;
        case CONJUNCTION:
        case DISJUNCTION:
            return boolTypeBool(subnodes[0].getExpType(), subnodes[1].getExpType());
        case LEAF:
            switch (root.nodeValue().type()) {
            case TRUE:
            case FALSE: return SqlExpressionType.BOOL;
            case FLOAT:
            case HEX:
            case INT:   return SqlExpressionType.ARITH;
            case IDENT: return SqlExpressionType.ANY;  //  TODO: depends upon the name
            case LIST:  return SqlExpressionType.LIST;
            case STRING:return SqlExpressionType.STRING;
            default:
                break;
            }
            break;
        case POSTFIXUNARYOP:
            return SqlExpressionType.BOOL;
        case PREFIXUNARYOP:
            switch (root.nodeValue().type()) {
            case NOT:
                return boolTypeBool(subnodes[0].getExpType());
            case OP_MINUS:
            case OP_PLUS:
                return arithTypeArith(subnodes[0].getExpType());
            default:
                break;
            }
            break;
        case TERNARYOP:
            return arithTypeBool(subnodes[0].getExpType(), subnodes[1].getExpType(), subnodes[2].getExpType());
        // Following cases should not affect the result:
        case PATTERN1:
        case PATTERN2:
        case LIST:
            break;
        // Following cases should not appear:
        case COLLAPSE1:
        case COLLAPSE2:
        case JOINLIST:
            throw new RuntimeException("Node type:"+root.nodeType()+" should not occur");
        }
        return SqlExpressionType.NOT_SET;
    }

    private static SqlExpressionType arithTypeBool(SqlExpressionType expType, SqlExpressionType expType2,
                                                    SqlExpressionType expType3) {
        if (SqlExpressionType.isArith(expType) && SqlExpressionType.isArith(expType2) && SqlExpressionType.isArith(expType3)) return SqlExpressionType.BOOL;
        return SqlExpressionType.INVALID;
    }

    private static SqlExpressionType boolTypeBool(SqlExpressionType expType, SqlExpressionType expType2) {
        if (SqlExpressionType.isBool(expType) && SqlExpressionType.isBool(expType2)) return SqlExpressionType.BOOL;
        return SqlExpressionType.INVALID;
    }

    private static SqlExpressionType arithTypeArith(SqlExpressionType expType) {
        if (SqlExpressionType.isArith(expType)) return SqlExpressionType.ARITH;
        return SqlExpressionType.INVALID;
    }

    private static SqlExpressionType arithTypeArith(SqlExpressionType expType, SqlExpressionType expType2) {
        if (SqlExpressionType.isArith(expType) && SqlExpressionType.isArith(expType2)) return SqlExpressionType.ARITH;
        return SqlExpressionType.INVALID;
    }

    private static SqlExpressionType stringTypeBool(SqlExpressionType expType) {
        if (SqlExpressionType.isString(expType)) return SqlExpressionType.BOOL;
        return SqlExpressionType.INVALID;
    }

    private static SqlExpressionType boolTypeBool(SqlExpressionType expType) {
        if (SqlExpressionType.isBool(expType)) return SqlExpressionType.BOOL;
        return SqlExpressionType.INVALID;
    }

    private static SqlExpressionType arithTypeBool(SqlExpressionType expType, SqlExpressionType expType2) {
        if (SqlExpressionType.isArith(expType) && SqlExpressionType.isArith(expType2)) return SqlExpressionType.BOOL;
        return SqlExpressionType.INVALID;
    }

    private static SqlExpressionType equalTypeBool(SqlExpressionType expType, SqlExpressionType expType2) {
        if (SqlExpressionType.isBool(expType) && SqlExpressionType.isBool(expType2)) return SqlExpressionType.BOOL;
        if (SqlExpressionType.isArith(expType) && SqlExpressionType.isArith(expType2)) return SqlExpressionType.BOOL;
        if (SqlExpressionType.isString(expType) && SqlExpressionType.isString(expType2)) return SqlExpressionType.BOOL;
        return SqlExpressionType.INVALID;
    }
}
