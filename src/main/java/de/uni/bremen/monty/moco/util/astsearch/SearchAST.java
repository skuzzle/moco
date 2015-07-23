package de.uni.bremen.monty.moco.util.astsearch;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import de.uni.bremen.monty.moco.ast.ASTNode;

/**
 * Provides a little DSL to search for AST nodes that match a list of
 * predicates.
 *
 * @author Simon Taddiken
 */
public final class SearchAST {

    /**
     * Creates a sequential {@link Stream} for processing ASTNodes.
     *
     * @param root The root node.
     * @return A Stream of nodes.
     */
    public static Stream<ASTNode> stream(ASTNode root) {
        if (root == null) {
            throw new IllegalArgumentException("root is null");
        }

        final Iterator<ASTNode> nodeIterator = SteppingVisitor.depthFirst(root);
        final Spliterator<ASTNode> splitIt = Spliterators.spliterator(nodeIterator,
                Long.MAX_VALUE, Spliterator.NONNULL);
        return StreamSupport.stream(splitIt, false);
    }

    /**
     * Creates a sequential {@link Stream} for processing the parents of the
     * given {@code root} node. Note: the first element in the stream is the
     * given node itself.
     *
     * @param root The root node.
     * @return A Stream of nodes.
     */
    public static Stream<ASTNode> parentStream(ASTNode root) {
        if (root == null) {
            throw new IllegalArgumentException("root is null");
        }

        final Iterator<ASTNode> nodeIterator = new ParentIterator(root);
        final Spliterator<ASTNode> splitIt = Spliterators.spliterator(nodeIterator,
                Long.MAX_VALUE, Spliterator.NONNULL);
        return StreamSupport.stream(splitIt, false);
    }

    /**
     * Specifies the node type to search for. This will match the exact type as
     * well as sub types of the specified type.
     *
     * @param nodeType The type to search for.
     * @return A {@link WhereClause} for specializing the search.
     */
    public static <C extends ASTNode> WhereClause<C> forNode(
            Class<? extends C> nodeType) {
        return new SearchForNodeImpl<C>(nodeType, nodeType::isInstance);
    }

    /**
     * Creates a search query which searches for a parent node with certain
     * attributes.
     *
     * @param parentType The type of the parent.
     * @return A {@link WhereClause} for specializing the search.
     */
    public static <C extends ASTNode> WhereClause<C> forParent(
            Class<? extends C> parentType) {
        return new SearchForParentImpl<>(parentType, parentType::isInstance);
    }
}
