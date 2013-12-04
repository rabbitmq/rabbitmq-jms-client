package com.rabbitmq.jms.parse;

/**
 * A visitor is guided by a {@link ParseTreeTraverser} to each {@link Node} of a tree.
 * <p>
 * The order of traversal is determined by the {@link ParseTreeTraverser} method (algorithm) used.
 * </p>
 * <p>
 * The returned <code>boolean</code> is interpreted by the caller and its meaning is not part of the {@link Visitor} contract.
 * </p>
 * @param <Node> - the values attached to the root of a subtree
 */
public interface Visitor<Node> {


    /**
     * Visit a place in the tree: the Node for this place in the tree, and an array of {@link Node}s of the immediate children, are passed as parameters.
     * @param parent - the node (value) of the parent at this point in the tree; can be updated.
     * @param children - array of the nodes of the children of this parent (can be zero-length array; must not be null).
     * @return <code>true</code> or <code>false</code> - interpreted by the caller (typically a tree traversal algorithm).
     */
    boolean visit(Node parent, Node[] children);
}
