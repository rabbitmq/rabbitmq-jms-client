package com.rabbitmq.jms.parse.sql;

import com.rabbitmq.jms.parse.Parser;
import com.rabbitmq.jms.parse.TokenStream;

/**
 * This uses {@link SqlProduction} to build a naïve parser for the grammar defined in that type.
 * <p>
 * This class is <i>not thread-safe</i> since it modifies the passed {@link TokenStream} which is (potentially) shared.
 * </p>
 */
public class SqlParser implements Parser<SqlTreeNode> {

    private final TokenStream<SqlToken, Integer> tokenStream;
    private boolean parseOk = false;
    private String terminationMessage = null;

    public SqlParser(TokenStream<SqlToken, Integer> tokenStream) {
        this.tokenStream = tokenStream;
    }

    @Override
    public SqlParseTree parse() {
        this.reset();
        SqlParseTree parsed = SqlProduction.ROOT.parse(this.tokenStream);
        if (this.tokenStream.moreTokens()) {
            this.parseOk = false;
            this.terminationMessage =
                String.format("Terminated before end of stream; next token is: '%s' at index: %s."
                             , this.tokenStream.readToken(), this.tokenStream.position());
            return null;
        } else {
            this.parseOk = true;
        }
        return parsed;
    }

    @Override
    public boolean parseOk() {
        return this.parseOk;
    }

    @Override
    public String getTerminationMessage() {
        return this.terminationMessage;
    }

    private final void reset() {
        this.parseOk = false;
        this.tokenStream.reset();
        this.terminationMessage = null;
    }

}
