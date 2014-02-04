/* Copyright (c) 2013 Pivotal Software, Inc. All rights reserved. */
/**
 * Parse trees capture a structured view of a parsed token stream.
 * They encapsulate the result of a parse() operation.
 * <p>
 *
 * </p>
 * <p>
 * Traversal algorithms will assume that there are no loops in the tree (no child can be its own ancestor).
 * </p>
 */
package com.rabbitmq.jms.parse;

/**
 * A non-empty tree of {@link Node}s which can be traversed.
 * <p>
 * The tree is a {@link Node} and a(n array of) children (if there are no children this must be a zero-length array and must not be <code>null</code>).
 * Each child in the array is a {@link ParseTree ParseTree&lt;Node&gt;}.
 * </p>
 * @param <Node> - the type of nodes in the tree, a {@link Node} is attached to the root of each subtree.
 */
public interface ParseTree<Node> {

    /**
     * The node at the root of the tree.
     * @return
     */
    Node getNode();

    /**
     * Convenience method to avoid creating children prematurely. Must be the same as <code>getChildren().length</code>.
     * @return the number of children of the root.
     */
    int getNumberOfChildren();


    /**
     * Get the immediate children of the root of the tree.
     * @return an array of children.
     */
    ParseTree<Node>[] getChildren();

    /**
     * Get the nodes of the immediate children of the root of the tree.
     * @return an array of {@link Node}s.
     */
    Node[] getChildNodes();

}
