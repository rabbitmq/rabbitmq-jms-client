/* Copyright (c) 2013-2023 Broadcom. All Rights Reserved. The term "Broadcom" refers to Broadcom Inc. and/or its subsidiaries. */
package com.rabbitmq.jms.parse;

/**
 * A visitor is guided by a {@link ParseTreeTraverser} to each {@link Node} of a tree.
 * <p>
 * It is intended that {@link Visitor#visitBefore(Object, Object[])} and {@link Visitor#visitAfter(Object, Object[])} are called for each node in the tree.
 * The order of traversal is both pre- and post-order: {@link #visitBefore(Object, Object[])} is called on a parent <i>before</i> the children are called,
 * and {@link Visitor#visitAfter(Object, Object[])} is called <i>after</i> the children are called.
 * </p>
 * <p>
 * The returned <code>boolean</code> is interpreted by the caller and its meaning is not part of the {@link Visitor} contract.
 * </p>
 */
public interface Visitor<Node> {


    /**
     * Visit a place in the tree: the Node for this place in the tree, and an array of {@link Node}s of the immediate children, are passed as parameters.
     * @param parent - the node (value) of the parent at this point in the tree; can be updated.
     * @param children - array of the nodes of the children of this parent (can be zero-length array; must not be null).
     * @return <code>true</code> or <code>false</code> - interpreted by the caller (typically a tree traversal algorithm).
     */
    boolean visitBefore(Node parent, Node[] children);

    /**
     * Visit a place in the tree: the Node for this place in the tree, and an array of {@link Node}s of the immediate children, are passed as parameters.
     * @param parent - the node (value) of the parent at this point in the tree; can be updated.
     * @param children - array of the nodes of the children of this parent (can be zero-length array; must not be null).
     * @return <code>true</code> or <code>false</code> - interpreted by the caller (typically a tree traversal algorithm).
     */
    boolean visitAfter (Node parent, Node[] children);
}
