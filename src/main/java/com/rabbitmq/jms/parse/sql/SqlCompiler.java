/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.parse.sql;

import static com.rabbitmq.jms.parse.ParseTreeTraverser.traverse;

import com.rabbitmq.jms.parse.Compiler;

public class SqlCompiler implements Compiler {

    private final String compiledCode;
    private final boolean compileOk;
    private final String errorMessage;

    public SqlCompiler(SqlEvaluator eval) {
        if (eval.evaluatorOk()) {
            SqlParseTree parseTree = eval.typedParseTree();
            SqlCompilerVisitor compilerVisitor = new SqlCompilerVisitor();
            if (this.compileOk = traverse(parseTree, compilerVisitor)) {
                this.compiledCode = compilerVisitor.extractCode() + ".";
                this.errorMessage = null;
            } else {
                this.compiledCode = null;
                this.errorMessage = "Could not compile parsed tree "+ parseTree.formattedTree();
            }
        } else {
           this.compileOk = false;
           this.compiledCode = null;
           this.errorMessage = eval.getErrorMessage();
        }
    }

    @Override
    public String compile() {
        return this.compiledCode;
    }

    @Override
    public boolean compileOk() {
        return this.compileOk;
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }
}
