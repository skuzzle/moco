package de.uni.bremen.monty.moco.util;

import de.uni.bremen.monty.moco.ast.ASTNode;

public final class ASTUtil {

    private ASTUtil() {}

    /**
     * Traverses the parents of the provided node until one node with the
     * specified type is found. The search also matches sub types of the
     * provided type.
     *
     * @param <T> Type of the node.
     * @param type Type of the ancestor to find.
     * @param root Node to start at. The search starts at the parent of this
     *            node.
     * @return The found ancestor or <code>null</code> if none was found.
     */
    public static <T extends ASTNode> T findAncestor(ASTNode root, Class<T> type) {
        ASTNode current = root;
        while (current != null) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            current = current.getParentNode();
        }
        return null;
    }
}
