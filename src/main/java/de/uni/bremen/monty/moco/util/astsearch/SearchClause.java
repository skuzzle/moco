package de.uni.bremen.monty.moco.util.astsearch;

import java.util.Optional;

import de.uni.bremen.monty.moco.ast.ASTNode;

public interface SearchClause<C extends ASTNode> {

    public Optional<C> in(ASTNode root);
}
