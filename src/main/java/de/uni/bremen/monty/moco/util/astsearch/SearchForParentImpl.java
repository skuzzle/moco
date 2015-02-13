package de.uni.bremen.monty.moco.util.astsearch;

import java.util.Optional;
import java.util.function.Predicate;

import de.uni.bremen.monty.moco.ast.ASTNode;

class SearchForParentImpl<C extends ASTNode> extends AbstractSearchImpl<C> {

    protected SearchForParentImpl(Class<? extends C> type, Predicate<ASTNode> typeEq) {
        super(type, typeEq);
    }

    @Override
    public Optional<C> in(ASTNode root) {
        ASTNode current = root;
        while (current != null) {
            if (this.typeEq.test(current) && allPredicatesMatch(current)) {
                return Optional.of(this.type.cast(current));
            }
            current = current.getParentNode();
        }
        return Optional.empty();
    }

}
