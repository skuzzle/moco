package de.uni.bremen.monty.moco.util.astsearch;

import java.util.Optional;
import java.util.function.Predicate;

import de.uni.bremen.monty.moco.ast.ASTNode;

/**
 * Allows to add predicates for refining the search.
 *
 * @author Simon Taddiken
 * @param <C> Type of the node to search for.
 */
public interface WhereClause<C extends ASTNode> extends SearchClause<C> {

    /**
     * Adds the given predicate to the list of predicates. In order for the
     * search to be successful, all added predicates must match on a node.
     *
     * @param pred The predicate to add.
     * @return A {@link InClause} for specializing the search.
     */
    public InClause<C> where(Predicate<C> pred);

    /**
     * Creates a predicate from a sub query which is executed on every node that
     * is searched. The created predicate will hold <code>true</code>, if the
     * sub query yields a result. Sample usage:
     *
     * <pre>
     * SearchAST.forNode(VariableDeclaration.class)
     *         .where(SearchAST.forParent(ClassDeclaration.class))
     *         .in(myAst);
     * </pre>
     *
     * @param subQuery The sub query.
     * @return A {@link InClause} for specializing the search.
     */
    public InClause<C> where(SearchClause<?> subQuery);

    /**
     * Performs the search immediately without specifying any predicates. Will
     * yield the first occurred node with the specified type.
     *
     * @param root The AST t o search in.
     * @return The resulting node or an empty {@link Optional}.
     */
    @Override
    public Optional<C> in(ASTNode root);
}
