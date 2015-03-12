package de.uni.bremen.monty.moco.visitor.typeinf;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.Location;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;

abstract class TypeResolverFragment {

    private final BaseVisitor resolver;

    public TypeResolverFragment(BaseVisitor resolver) {
        this.resolver = resolver;
    }

    protected void resolveTypeOf(ASTNode node) {
        node.visit(this.resolver);
    }

    protected void reportError(Location location, String message, Object...format) {
        throw new TypeInferenceException(location, String.format(message, format));
    }
}
