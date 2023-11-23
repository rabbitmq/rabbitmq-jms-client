/* Copyright (c) 2014-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.parse.sql;

import static com.rabbitmq.jms.parse.ParseTreeTraverser.traverse;

import java.util.Map;

import com.rabbitmq.jms.parse.Evaluator;

/**
 * A boolean evaluator for JMS Sql selector expressions.
 *
 */
public class SqlEvaluator implements Evaluator {

    private final SqlParseTree typedParseTree;
    private final String errorMessage;
    private final boolean evaluatorOk;

    public SqlEvaluator(SqlParser parser, Map<String, SqlExpressionType> identTypes) {
        if (parser.parseOk()) {
            SqlParseTree parseTree = parser.parse();
            if (this.evaluatorOk = canBeBool(SqlTypeChecker.deriveExpressionType(parseTree, identTypes))) {
                this.typedParseTree = parseTree;
                this.errorMessage = null;
            } else {
                this.errorMessage = "Type error in expression";
                this.typedParseTree = null;
            }
        } else {
           this.evaluatorOk = false;
           this.typedParseTree = null;
           this.errorMessage = parser.getErrorMessage();
        }
    }

    private static boolean canBeBool(SqlExpressionType set) {
        return (set == SqlExpressionType.BOOL || set == SqlExpressionType.ANY);
    }

    @Override
    public boolean evaluate(Map<String, Object> env) {
        if (this.evaluatorOk){
            SqlEvaluatorVisitor eVisitor = new SqlEvaluatorVisitor(env);
            if (traverse(this.typedParseTree, eVisitor)) {
                Object val = this.typedParseTree.getNode().getExpValue().getValue();
                if (val != null && val instanceof Boolean)
                    return (Boolean) val;
            }
        }
        return false;
    }

    /**
     * @return the type-checked parse tree used for evaluation; <code>null</code> if {@link #evaluatorOk()} is <code>false</code>.
     */
    public SqlParseTree typedParseTree() {
        return this.typedParseTree;
    }

    @Override
    public boolean evaluatorOk() {
        return this.evaluatorOk;
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }

}
