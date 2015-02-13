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
        final Iterator<ASTNode> nodeIterator = new SteppingVisitor(root);
        final Spliterator<ASTNode> splitIt = Spliterators.spliterator(nodeIterator,
                Long.MAX_VALUE, Spliterator.NONNULL);
        return StreamSupport.stream(splitIt, false);
    }

    public static Stream<ASTNode> parentStream(ASTNode root) {
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
    public static <C extends ASTNode> WhereClause<C> forNode(Class<? extends C> nodeType) {
        return new SearchForNodeImpl<C>(nodeType, nodeType::isInstance);
    }

    public static <C extends ASTNode> WhereClause<C> forExactNode(
            Class<? extends C> nodeType) {
        return new SearchForNodeImpl<>(nodeType, c -> c.getClass() == nodeType);
    }

    /**
     * Creates a search query which does not impose any restrictions on the
     * node's type.
     *
     * @return A {@link WhereClause} for specializing the search.
     */
    public static WhereClause<ASTNode> forAnyNode() {
        return forNode(ASTNode.class);
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

    public static WhereClause<ASTNode> forAnyParent() {
        return forParent(ASTNode.class);
    }

    public static <C extends ASTNode> WhereClause<C> forExactParent(
            Class<? extends C> parentType) {
        return new SearchForParentImpl<>(parentType, c -> c.getClass() == parentType);
    }
}
