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
 * {@link #traverse()} visits each subtree twice: combining ‘pre-order’ and ‘post-order’ traversal in one pass.
 * </p>
 * <p>
 * The {@link Visitor} contract returns a <code>boolean</code> flag from its visit methods which these
 * traversal algorithms interpret as immediate abort: no further visits will be made after the first returns false.
 * Each traversal method returns <code>true</code> if all the tree is visited, and <code>false</code> if any visit aborted.
 * </p>
 */
public abstract class ParseTreeTraverser {  // stop direct instantiation
    private ParseTreeTraverser() {};        // stop indirect instantiation

    /**
     * {@link #traverse()} visits each subtree in <i>both</i> ‘pre-order’ and ‘post-order’, in one pass.
     * Each subtree is visited <i>before</i> (by calling {@link #visitBefore()}) and <i>after</i> (by calling {@link #visitAfter()})
     * all of its child subtrees are visited.
     * @param tree - the tree all of whose subtrees are to be visited
     * @param visitor - the {@link Visitor} whose {@link #visitBefore()} and {@link #visitAfter()} methods are passed each subtree’s node and child nodes.
     * @return <code>true</code> if the traversal completed, <code>false</code> if it was aborted prematurely
     */
    public static <Node> boolean traverse(ParseTree<Node> tree, Visitor<Node> visitor) {
        if (!visitor.visitBefore(tree.getNode(), tree.getChildNodes())) return false;
        for (ParseTree<Node> st : tree.getChildren()) {
            if (!traverse(st, visitor)) return false;
        }
        if (!visitor.visitAfter(tree.getNode(), tree.getChildNodes())) return false;
        return true;
    }

}
