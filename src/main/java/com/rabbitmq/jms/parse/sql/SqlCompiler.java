package com.rabbitmq.jms.parse.sql;

import static com.rabbitmq.jms.parse.ParseTreeTraverser.traverse;

import java.util.Map;

import com.rabbitmq.jms.parse.Compiler;

public class SqlCompiler implements Compiler {

    private final String compiledCode;
    private final boolean compileOk;
    private final String errorMessage;

    public SqlCompiler(SqlParser parser, Map<String, SqlExpressionType> identTypes) {
        if (parser.parseOk()) {
            SqlParseTree parseTree = parser.parse();
            if (canBeBool(SqlTypeChecker.deriveExpressionType(parseTree, identTypes))) {
                SqlCompilerVisitor compilerVisitor = new SqlCompilerVisitor();
                if (this.compileOk = traverse(parseTree, compilerVisitor)) {
                    this.compiledCode = compilerVisitor.extractCode();
                    this.errorMessage = null;
                } else {
                    this.compiledCode = null;
                    this.errorMessage = "Could not compile parsed tree "+ parseTree.formattedTree();
                }
            } else {
                this.compileOk = false;
                this.compiledCode = null;
                this.errorMessage = "Type error in expression";
            }
        } else {
           this.compileOk = false;
           this.compiledCode = null;
           this.errorMessage = parser.getErrorMessage();
        }
    }

    private static boolean canBeBool(SqlExpressionType set) {
        return (set == SqlExpressionType.BOOL || set == SqlExpressionType.ANY);
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
