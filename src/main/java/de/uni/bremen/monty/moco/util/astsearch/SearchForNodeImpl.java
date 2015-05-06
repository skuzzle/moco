package de.uni.bremen.monty.moco.util.astsearch;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.uni.bremen.monty.moco.ast.ASTNode;

class SearchForNodeImpl<C extends ASTNode> extends AbstractSearchImpl<C> implements
        WhereClause<C>, InClause<C> {

    SearchForNodeImpl(Class<? extends C> type, Predicate<ASTNode> typeEq) {
        super(type, typeEq);
    }

    @Override
    public Optional<C> in(ASTNode root) {
        final Stream<C> nodeStream = SearchAST.stream(root)
                .filter(this::allPredicatesMatch)
                .map(node -> this.type.cast(node));

        return nodeStream.findFirst();
    }
}
