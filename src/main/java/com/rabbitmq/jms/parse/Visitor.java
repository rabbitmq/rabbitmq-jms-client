package com.rabbitmq.jms.parse;

/**
 * A visitor is guided by a {@link ParseTreeTraverser} to each subtree of a tree.
 * <p>
 * The order of traversal is determined by the {@link ParseTreeTraverser} method (algorithm) used.
 * </p>
 * <p>
 * The abort return boolean is interpreted by the traversal algorithm and its meaning is not stipulated here.
 * </p>
 * @param <Node>
 */
public interface Visitor<Node> {

    boolean visit(ParseTree<Node> subtree);
}
