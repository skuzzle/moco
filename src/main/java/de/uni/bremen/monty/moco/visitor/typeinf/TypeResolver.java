package de.uni.bremen.monty.moco.visitor.typeinf;

import java.util.ArrayList;
import java.util.List;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.Location;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Typed;

public interface TypeResolver {

    public void resolveTypeOf(ASTNode node);

    public void resolveTypeAgain(ASTNode node);

    public default List<Type> resolveTypesOf(Iterable<? extends ASTNode> nodes) {
        final List<Type> result = new ArrayList<>();
        for (final ASTNode node : nodes) {
            resolveTypeOf(node);
            if (node instanceof Typed) {
                result.add(((Typed) node).getType());
            }
        }
        return result;
    }

    public void reportError(Location location, String msg, Object... format);
}
