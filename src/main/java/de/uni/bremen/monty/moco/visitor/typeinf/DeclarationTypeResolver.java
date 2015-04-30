package de.uni.bremen.monty.moco.visitor.typeinf;

import de.uni.bremen.monty.moco.ast.ClassScope;
import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.declaration.ClassDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeInstantiation;
import de.uni.bremen.monty.moco.ast.declaration.TypeVariableDeclaration;

public class DeclarationTypeResolver extends TypeResolverFragment {

    public DeclarationTypeResolver(TypeResolver resolver) {
        super(resolver);
    }

    public void resolveClassDeclaration(ClassDeclaration node) {
        final ClassScope scope = node.getScope();

        // TODO: move to declaration visitor
        for (final Identifier typeParam : node.getTypeParameters()) {
            final TypeDeclaration decl = new TypeVariableDeclaration(node.getPosition(), typeParam);
            scope.define(typeParam, decl);
        }

        for (final TypeInstantiation superClass : node.getSuperClassIdentifiers()) {
            resolveTypeOf(superClass);
        }
    }
}
