package de.uni.bremen.monty.moco.util.astsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import de.uni.bremen.monty.moco.ast.ASTNode;

abstract class AbstractSearchImpl<C extends ASTNode> implements WhereClause<C>,
        InClause<C> {

    protected final Class<? extends C> type;
    protected final List<Predicate<C>> predicates;
    protected final Predicate<ASTNode> typeEq;

    protected AbstractSearchImpl(Class<? extends C> type, Predicate<ASTNode> typeEq) {
        this.type = type;
        this.predicates = new ArrayList<>();
        this.typeEq = typeEq;
    }

    protected boolean allPredicatesMatch(ASTNode node) {
        if (this.typeEq.test(node)) {
            final C n = this.type.cast(node);
            for (final Predicate<C> pred : this.predicates) {
                if (!pred.test(n)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public abstract Optional<C> in(ASTNode root);

    @Override
    public InClause<C> and(Predicate<C> pred) {
        this.predicates.add(pred);
        return this;
    }

    @Override
    public InClause<C> where(Predicate<C> pred) {
        this.predicates.add(pred);
        return this;
    }

    @Override
    public InClause<C> where(SearchClause<?> subQuery) {
        if (subQuery == this) {
            throw new IllegalArgumentException();
        }
        final Predicate<C> subPred = c -> subQuery.in(c).isPresent();
        this.predicates.add(subPred);
        return this;
    }

    @Override
    public InClause<C> and(SearchClause<?> subQuery) {
        return where(subQuery);
    }

}