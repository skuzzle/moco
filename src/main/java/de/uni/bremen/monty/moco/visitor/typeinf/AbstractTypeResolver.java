package de.uni.bremen.monty.moco.visitor.typeinf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.Location;
import de.uni.bremen.monty.moco.ast.ResolvableIdentifier;
import de.uni.bremen.monty.moco.ast.Scope;
import de.uni.bremen.monty.moco.ast.declaration.ClassDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.TypeVariable;
import de.uni.bremen.monty.moco.exception.UnknownIdentifierException;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;

public class AbstractTypeResolver extends BaseVisitor {

    private static final String TYPE_VAR = "?";
    private final Set<ASTNode> visited;

    public AbstractTypeResolver() {
        setStopOnFirstError(true);
        this.visited = new HashSet<>();
    }

    protected boolean shouldVisit(ASTNode node) {
        return this.visited.add(node);
    }


    protected Type resolveType(ASTNode node, ResolvableIdentifier name) {
        if (name.getSymbol().equals(TYPE_VAR)) {
            return TypeVariable.anonymous().atLocation(node).createType();
        } else {
            final Scope scope = node.getScope();
            final TypeDeclaration declaredType = scope.resolveType(node, name);

            visitDoubleDispatched(declaredType);
            return declaredType.getType();
        }
    }

    /**
     * Resolves all possible types of the given call.
     *
     * @param scope
     * @param identifier
     * @param location
     * @return Collection of function types.
     */
    protected Collection<Function> resolveTypes(Scope scope,
            ResolvableIdentifier identifier, Location location) {
        final List<ProcedureDeclaration> declarations = scope.resolveProcedure(location,
                identifier);
        final List<Function> result = new ArrayList<>(declarations.size());

        for (final ProcedureDeclaration decl : declarations) {
            // ensure that declaration's type has been resolved
            visitDoubleDispatched(decl);

            result.add((Function) decl.getType());
        }
        return result;
    }

    protected Collection<Function> resolveConstructorTypes(ClassDeclaration type, Location location) {
        try {
            return resolveTypes(type.getScope(), ResolvableIdentifier.of("initializer"),
                    location);
        } catch (UnknownIdentifierException e) {
            // no explicit constructors available
            final ProcedureDeclaration defInit = type.getDefaultInitializer();
            visitDoubleDispatched(defInit);
            return Collections.singleton(defInit.getType().asFunction());
        }
    }

}