/**
 *
 */
package com.rabbitmq.jms.parse.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rabbitmq.jms.parse.TokenStream;
import com.rabbitmq.jms.parse.Multiples.Pair;

/**
 * A simple stream of SQL tokens generated from an input character sequence at instantiation time.
 * <p>
 * Apart from the {@link TokenStream} operations this implementation also supports a {@link #getResidue()}
 * method, which returns the character sequence remaining after lexical scanning. If this is not empty,
 * the tokeniser failed to completely scan the input.
 * </p>
 * @author spowell
 */
public class SqlTokenStream implements TokenStream<SqlToken, Integer> {

    private final List<SqlToken> tokenSequence;
    private final int tokenSequenceSize;
    private final CharSequence residue;

    private int currentPosition = 0;

    SqlTokenStream(CharSequence cseq) {
        Pair<List<SqlToken>, CharSequence> result = tokenize(cseq);
        this.tokenSequence = result.left();
        this.tokenSequenceSize = result.left().size();
        this.residue = result.right();
    }

    public CharSequence getResidue() {
        return this.residue;
    }

//    @Override
    public boolean moreTokens() {
        return (currentPosition < tokenSequenceSize);
    }

//    @Override
    public SqlToken readToken() {
        if (this.moreTokens())
            return this.tokenSequence.get(this.currentPosition);
        else
            return null;
    }

//    @Override
    public SqlToken getNext() {
        SqlToken token = readToken();
        incrementPosition();
        return token;
    }

    private void incrementPosition() {
        if (currentPosition == tokenSequenceSize) return;
        ++this.currentPosition;
    }

//    @Override
    public Integer position() {
        return this.currentPosition;
    }

//    @Override
    public void stepBack() {
        if (currentPosition == 0) return;
        --this.currentPosition;
    }

//    @Override
    public void reset(Integer position) {
        if (position < 0) this.currentPosition = 0;
        else if (position < tokenSequenceSize) this.currentPosition = position;
        else this.currentPosition = tokenSequenceSize;
    }

//    @Override
    public void reset() {
        this.currentPosition = 0;
    }

    /**
     * <i><b>Strategy for lexical analysis</b></i>:
     * <p>
     * Match each of the token types in turn to the 'next' characters in <code>cseq</code> until one of them matches
     * completely. Take that as the token to generate. And repeat. (We omit token line.)
     * </p>
     * <p>
     * If none of the token types match at any point we terminate, with the remaining character sequence and the tokens
     * already built as output.
     * </p>
     *
     * @param cseq - the character sequence to tokenise
     * @return a Deque of the tokens <i>and</i> the remaining (unmatched) character sequence (residue)
     */
    private static final Pair<List<SqlToken>, CharSequence> tokenize(CharSequence cseq) {

        SqlTokenType[] tokenTypes = SqlTokenType.values();
        List<SqlToken> deq = new ArrayList<SqlToken>();
        Matcher m = tokenTypes[0].pattern().matcher(cseq);

        boolean foundOne = false;
        int cseqIndex = 0;
        final int cseqLength = cseq.length();
        do {
            m.region(cseqIndex, cseqLength);
            foundOne = false;
            for (SqlTokenType tt:tokenTypes) {
                Pattern ttPattern = tt.pattern();
                if (null != ttPattern) { // a token not found by scanning
                    m.usePattern(tt.pattern());
                    if (m.lookingAt()) {
                        foundOne = true;
                        if (tt.include())
                            deq.add(new SqlToken(tt,m.group()));
                        cseqIndex = m.end();
                        break;
                    }
                }
            }
        } while (cseqIndex < cseqLength && foundOne);
        return new Pair<List<SqlToken>, CharSequence>(deq, cseq.subSequence(cseqIndex, cseqLength));
    }
}
