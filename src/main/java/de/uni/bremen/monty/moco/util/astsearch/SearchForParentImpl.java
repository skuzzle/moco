package de.uni.bremen.monty.moco.util.astsearch;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.uni.bremen.monty.moco.ast.ASTNode;

class SearchForParentImpl<C extends ASTNode> extends AbstractSearchImpl<C> {

    protected SearchForParentImpl(Class<? extends C> type, Predicate<ASTNode> typeEq) {
        super(type, typeEq);
    }

    @Override
    public Optional<C> in(ASTNode root) {
        final Stream<C> parentStream = SearchAST.parentStream(root)
                .filter(this::allPredicatesMatch)
                .map(node -> this.type.cast(node));
        return parentStream.findFirst();
    }

}
