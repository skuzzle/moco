package de.uni.bremen.monty.moco.util.astsearch;

import java.util.function.Predicate;

import de.uni.bremen.monty.moco.ast.ASTNode;

public interface InClause<C extends ASTNode> extends SearchClause<C> {

    public InClause<C> and(Predicate<C> pred);

    public InClause<C> and(SearchClause<?> subQuery);

}
