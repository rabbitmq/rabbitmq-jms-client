/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
/**
 * A stream of tokens. Supports getNextToken() and putBackToken() operations as well as moreTokens().
 */
package com.rabbitmq.jms.parse;

/**
 * A read-only sequence of {@link Token}s that are read one-by-one. Reads are not destructive and the stream can be reset and reread.
 * At any time a position may be obtained, and subsequently used to reset the stream <i>to that position</i>.
 *
 * @param <Token> - the type of items in the stream
 * @param <Position> - the type which implements the positions in the stream
 */
public interface TokenStream<Token, Position> {

    /**
     * @return true if there are more tokens after the current position in the stream
     */
    boolean moreTokens();

    /**
     * Reads token at the current position and the position is not changed.
     * @return the current {@link Token} in the stream if any, otherwise <code>null</code>
     */
    Token readToken();

    /**
     * Increment position in the stream; no-op if already at the end.
     * @return the next {@link Token} in the stream if any, otherwise <code>null</code>
     */
    Token getNext();

    /**
     * Decrement position in the stream; no-op if already at the beginning.
     */
    void stepBack();

    /**
     * @return the current position in the stream
     */
    Position position();

    /**
     * Reset the stream to the position provided, if it is a valid position. Otherwise undefined.
     * @param position - previously returned place in the stream
     */
    void reset(Position position);

    /**
     * Reset the stream to the start. Equivalent to
     * <code>tokenStream.reset(START)</code> where <code>Position START = tokenStream.getPosition();</code>
     * was executed first after instantiation of the {@link TokenStream}.
     */
    void reset();

}
