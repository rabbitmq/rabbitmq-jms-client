package com.rabbitmq.jms.parse;

/**
 * This traverser class encapsulates two methods of traversing a tree, calling a {@link Visitor} for
 * each subtree of a {@link ParseTree ParseTree&lt;Node&gt;}.
 * <p>
 * {@link #traversePreOrder()} visits each subtree in ‘pre-order’, and {@link #traversePostOrder()}
 * in ‘post-order’. ‘Pre-order’ visits each subtree <i>before</i> any of its child subtrees, and ‘post-order’
 * visits each subtree <i>after</i> visiting its child subtrees.
 * </p>
 * <p>
 * The {@link Visitor} contract returns a <code>boolean</code> flag from {@link Visitor#visit()} which these
 * traversal algorithms interpret as immediate abort: no further visits will be made after the first returns false.
 * Each traversal method returns <code>true</code> if all the tree is visited, and <code>false</code> if any visit aborted.
 * </p>
 */
public abstract class ParseTreeTraverser {  // stop direct instantiation
    private ParseTreeTraverser() {};        // stop indirect instantiation

    /**
     * {@link #traversePreOrder()} visits each subtree in ‘pre-order’, where each subtree is visited
     * <i>before</i> any of its child subtrees.
     * @param tree - the tree all of whose subtrees are to be visited
     * @param visitor - the {@link Visitor} whose {@link Visitor#visit()} method is passed each subtree
     * @return <code>true</code> if the traversal completed, <code>false</code> if it was aborted prematurely
     */
    public static <Node> boolean traversePreOrder(ParseTree<Node> tree, Visitor<Node> visitor) {
        if (!visitor.visit(tree.getNode(), tree.getChildNodes())) return false;
        for (ParseTree<Node> st : tree.getChildren()) {
            if (!traversePreOrder(st, visitor)) return false;
        }
        return true;
    }

    /**
     * {@link #traversePostOrder()} visits each subtree in ‘post-order’, where each subtree is visited
     * <i>after</i> all of its child subtrees are visited.
     * @param tree - the tree all of whose subtrees are to be visited
     * @param visitor - the {@link Visitor} whose {@link Visitor#visit()} method is passed each subtree
     * @return <code>true</code> if the traversal completed, <code>false</code> if it was aborted prematurely
     */
    public static <Node> boolean traversePostOrder(ParseTree<Node> tree, Visitor<Node> visitor) {
        for (ParseTree<Node> st : tree.getChildren()) {
            if (!traversePostOrder(st, visitor)) return false;
        }
        if (!visitor.visit(tree.getNode(), tree.getChildNodes())) return false;
        return true;
    }

}
