/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.parse;

/**
 * A {@link Parser} produces a parse tree of nodes.
 * <p>
 * Parsing never throws an exception but instead sets a success indicator and error message.
 * </p>
 *
 * @param <Node> - type of node in the ParseTree produced
 */
public interface Parser<Node> {

    /**
     * This method returns the parse tree. It is idempotent.
     *
     * @return a {@link ParseTree ParseTree&lt;Node&gt;} capturing the result of a complete parse attempt.
     */
    ParseTree<Node> parse();

    /**
     * This call is idempotent.
     * @return <b><code>true</code></b> if the parse completed successfully, otherwise <b><code>false</code></b>, in which case the
     *         termination message is set.
     */
    boolean parseOk();

    /**
     * This call is idempotent.
     * @return a string with a reason for termination. Only valid if {@link #parseOk()} returns <code>false</code>.
     */
    String getErrorMessage();

}
