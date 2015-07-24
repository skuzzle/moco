package de.uni.bremen.monty.moco.visitor.typeinf;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.Location;

/**
 * Abstract type resolver which resolves types of certain nodes and relies on a
 * parent resolver to resolve types of other nodes.
 * 
 * @author Simon Taddiken
 */
abstract class TypeResolverFragment implements TypeResolver {

    private final TypeResolver resolver;

    protected TypeResolverFragment(TypeResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void resolveTypeOf(ASTNode node) {
        this.resolver.resolveTypeOf(node);
    }

    @Override
    public void reportError(Location location, String message, Object... format) {
        throw new TypeInferenceException(location, String.format(message, format));
    }
}
