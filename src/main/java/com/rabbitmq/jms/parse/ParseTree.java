/**
 * Parse trees offer a structured view of a parsed token stream.
 * They include parsing errors and parse failures, so essentially encapsulate the result of a parse() operation.
 * There must be no sharing of children in the tree, but there might be sharing of node data.
 */
package com.rabbitmq.jms.parse;

/**
 * A non-empty tree of {@link Node}s which can be traversed.
 * <p>
 * The tree is a (root) {@link Node} and a(n array of) children (if there are no children this can be null).
 * Each child in the array is a {@link ParseTree ParseTree&lt;Node&gt;}.
 * </p>
 * @param <Node> - the type of nodes in the tree
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
     * Get the children of the root of the tree.
     * @return an array of children.
     */
    ParseTree<Node>[] getChildren();

    /**
     * Get the nodes of the children of the root of the tree.
     * @return an array of {@link Node}s.
     */
    Node[] getChildNodes();

}
