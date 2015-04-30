package de.uni.bremen.monty.moco.visitor.typeinf;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.Location;

public interface TypeResolver {

    public void resolveTypeOf(ASTNode node);

    public default void resolveTypesOf(Iterable<? extends ASTNode> nodes) {
        for (final ASTNode node : nodes) {
            resolveTypeOf(node);
        }
    }

    public void reportError(Location location, String msg, Object... format);
}
