package de.uni.bremen.monty.moco.util.astsearch;

import java.util.Optional;

import de.uni.bremen.monty.moco.ast.ASTNode;

public interface Searchable<C extends ASTNode> {

    public Optional<C> in(ASTNode root);
}
