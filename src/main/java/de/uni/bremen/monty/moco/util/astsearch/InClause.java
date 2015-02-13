package de.uni.bremen.monty.moco.util.astsearch;

import java.util.function.Predicate;

import de.uni.bremen.monty.moco.ast.ASTNode;

public interface InClause<C extends ASTNode> extends Searchable<C> {

    public InClause<C> and(Predicate<C> pred);

    public InClause<C> and(Searchable<?> subQuery);

}
