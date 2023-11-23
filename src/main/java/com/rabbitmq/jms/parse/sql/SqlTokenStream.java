/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.parse.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rabbitmq.jms.parse.Multiples.Pair;
import com.rabbitmq.jms.parse.TokenStream;

/**
 * A simple stream of SQL tokens generated from an input character sequence at instantiation time.
 * <p>
 * Apart from the {@link TokenStream} operations this implementation also supports a {@link #getResidue()}
 * method, which returns the character sequence remaining after lexical scanning. If this is not empty,
 * the tokeniser failed to completely scan the input.
 * </p>
 */
public class SqlTokenStream implements TokenStream<SqlToken, Integer> {

    private static final Pattern JUNK_PATTERN = Pattern.compile("");

    private final List<SqlToken> tokenSequence;
    private final int tokenSequenceSize;
    private final CharSequence residue;

    private int currentPosition = 0;

    /**
     * The constructor builds the whole token stream from the given character sequence by tokenising it.
     * The tokens are defined by the {@link SqlTokenType} <b><code>enum</code></b> type.
     * <p>
     * This never fails with an exception, but may not consume many (or any) characters from <code>cseq</code>
     * and return most of the original sequence in {@link #getResidue()}.
     * </p>
     * @see SqlTokenType
     * @see #tokenize(CharSequence)
     * @param cseq - the sequence of characters (for example a {@link String}) which is tokenized
     */
    public SqlTokenStream(CharSequence cseq) {
        Pair<List<SqlToken>, CharSequence> result = tokenize(cseq);
        this.tokenSequence = result.left();
        this.tokenSequenceSize = result.left().size();
        this.residue = result.right();
    }

    /**
     * After this object is instantiated there may be characters in the initial sequence
     * which were not able to be tokenized.  The characters after which the tokenizer could not
     * determine any more tokens are returned by this method.
     * @return the characters after the first non-tokenisable chars in the intial sequence.
     */
    public CharSequence getResidue() {
        return this.residue;
    }

    @Override
    public boolean moreTokens() {
        return (currentPosition < tokenSequenceSize);
    }

    @Override
    public SqlToken readToken() {
        if (this.moreTokens())
            return this.tokenSequence.get(this.currentPosition);
        else
            return null;
    }

    @Override
    public SqlToken getNext() {
        SqlToken token = readToken();
        incrementPosition();
        return token;
    }

    private void incrementPosition() {
        if (currentPosition == tokenSequenceSize) return;
        ++this.currentPosition;
    }

    @Override
    public Integer position() {
        return this.currentPosition;
    }

    @Override
    public void stepBack() {
        if (currentPosition == 0) return;
        --this.currentPosition;
    }

    @Override
    public void reset(Integer position) {
        if (position < 0) this.currentPosition = 0;
        else if (position < tokenSequenceSize) this.currentPosition = position;
        else this.currentPosition = tokenSequenceSize;
    }

    @Override
    public void reset() {
        this.currentPosition = 0;
    }

    /**
     * <i><b>Strategy for lexical analysis</b></i> (tokenizing):
     * <p>
     * Match each of the token types in turn to the ‘next’ characters in <code>cseq</code> until one of them matches
     * completely. Take that as the token to generate, and step over the characters used. And repeat.
     * </p>
     * <p>
     * If none of the token types match at any point we terminate, with the remaining character sequence and the tokens
     * already built as output.
     * </p>
     * @param cseq - the character sequence to tokenise
     * @return a {@link List} of the tokens <i>and</i> the remaining (unmatched) character sequence (residue)
     * @see #getResidue()
     */
    private static final Pair<List<SqlToken>, CharSequence> tokenize(CharSequence cseq) {

        List<SqlToken> tokenList = new ArrayList<SqlToken>();
        Matcher m = getMatcher(cseq);

        int cseqIndex = 0;
        int cseqIndexNext = 0;
        final int cseqLength = cseq.length();
        do {
            cseqIndex = cseqIndexNext;
            cseqIndexNext = matchFirstSqlTokenType(tokenList, m.region(cseqIndex, cseqLength));
        } while (cseqIndex < cseqIndexNext && cseqIndexNext < cseqLength);

        return new Pair<List<SqlToken>, CharSequence>(tokenList, cseq.subSequence(cseqIndexNext, cseqLength));
    }

    private static final int matchFirstSqlTokenType(List<SqlToken> tokenList, Matcher m) {
        // See SqlTokenType: order is important
        for (SqlTokenType tt : SqlTokenType.values()) {
            Pattern ttPattern = tt.pattern();
            if (null != ttPattern) { // tokens with null patterns are ignored
                // set pattern to scan with
                m.usePattern(ttPattern);
                // does the pattern make an initial match?
                if (m.lookingAt()) {
                    if (tt.include())
                        tokenList.add(new SqlToken(tt, m.group()));
                    return m.end();  // return end position after first match
                }
            }
        }
        // no matches found, so return the starting position
        return m.regionStart();
    }

    private static final Matcher getMatcher(CharSequence cseq) {
        return JUNK_PATTERN.matcher(cseq); // it doesn't matter what pattern we use here
    }
}
