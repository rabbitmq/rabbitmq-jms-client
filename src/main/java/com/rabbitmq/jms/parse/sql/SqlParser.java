/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.parse.sql;

import com.rabbitmq.jms.parse.Parser;
import com.rabbitmq.jms.parse.TokenStream;

/**
 * This uses {@link SqlProduction} as a naïve parser for the grammar defined in that type.
 * <p>
 * This class is <i>not thread-safe during construction</i> since it then modifies the passed {@link TokenStream} which is (potentially) shared.
 * </p>
 */
public class SqlParser implements Parser<SqlTreeNode> {

    private final boolean parseOk;
    private final String errorMessage;
    private final SqlParseTree parseTree;

    public SqlParser(SqlTokenStream tokenStream) {
        if ("".equals(tokenStream.getResidue())) {
            this.parseTree = SqlProduction.ROOT.parse(tokenStream);
            if (tokenStream.moreTokens()) {
                this.parseOk = false;
                this.errorMessage =
                        String.format("Terminated before end of stream; next token is: '%s' at index: %s."
                                      , tokenStream.readToken(), tokenStream.position());
            } else if (this.parseTree == null) {
                this.parseOk = false;
                this.errorMessage = "No parse tree produced.";
            } else {
                this.parseOk = true;
                this.errorMessage = null;
            }
        } else {
            this.parseOk = false;
            this.errorMessage = "Unrecognised syntax after: '" + tokenStream.getResidue() + "'";
            this.parseTree = null;
        }

    }

    @Override
    public SqlParseTree parse() {
        return this.parseTree;
    }

    @Override
    public boolean parseOk() {
        return this.parseOk;
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }
}
