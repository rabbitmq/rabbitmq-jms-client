package com.rabbitmq.jms.parse;

/**
 * A {@link Parser} generates a parse tree of nodes.
 *
 * @author spowell
 *
 * @param <Node> - type of node in the ParseTree produced
 */
public interface Parser<Node> {

    /**
     * This method initiates parsing and sets the error status.
     *
     * @return a {@link ParseTree ParseTree&lt;Node&gt;} capturing the result of a complete parse attempt.
     */
    ParseTree<Node> parse();

    /**
     * @return <b><code>true</code></b> if the parse completed successfully, otherwise <b><code>false</code></b>, in which case the
     *         termination message is set.
     */
    boolean parseOk();

    /**
     * @return a string with a reason for termination. Only valid if {@link #parseOk()} returns <code>false</code>.
     */
    String getTerminationMessage();

}
