package com.rabbitmq.jms.parse.sql;

import com.rabbitmq.jms.parse.Parser;
import com.rabbitmq.jms.parse.TokenStream;

/**
 * This uses {@link SqlClauseType} to build a naïve parser for the grammar defined in that type.
 *
 */
public class SqlParser implements Parser<SqlTreeNode> {

    private final TokenStream<SqlToken, Integer> tokenStream;
    private boolean parseOk = false;
    private String terminationMessage = null;

    public SqlParser(TokenStream<SqlToken, Integer> tokenStream) {
        this.tokenStream = tokenStream;
    }

//    @Override
    public SqlParseTree parse() {
        this.reset();
        SqlParseTree parsed = SqlClauseType.ROOT.parse(this.tokenStream);
        if (this.tokenStream.moreTokens()) {
            this.parseOk = false;
            this.terminationMessage = "Terminated before end of stream; "
                    + "next token is: '" + this.tokenStream.readToken()
                    + "' at position: " + this.tokenStream.position() + ".";
            return null;
        } else {
            this.parseOk = true;
        }
        return parsed;
    }

//    @Override
    public boolean parseOk() {
        return this.parseOk;
    }

//    @Override
    public String getTerminationMessage() {
        return this.terminationMessage;
    }

    private final void reset() {
        this.parseOk = false;
        this.tokenStream.reset();
        this.terminationMessage = null;
    }

}
