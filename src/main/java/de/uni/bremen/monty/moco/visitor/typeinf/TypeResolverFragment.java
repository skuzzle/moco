package de.uni.bremen.monty.moco.visitor.typeinf;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.Location;

abstract class TypeResolverFragment implements TypeResolver {

    private final TypeResolver resolver;

    public TypeResolverFragment(TypeResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void resolveTypeOf(ASTNode node) {
        this.resolver.resolveTypeOf(node);
    }

    @Override
    public void resolveTypeAgain(ASTNode node) {
        this.resolver.resolveTypeAgain(node);
    }

    @Override
    public void reportError(Location location, String message, Object... format) {
        throw new TypeInferenceException(location, String.format(message, format));
    }
}
