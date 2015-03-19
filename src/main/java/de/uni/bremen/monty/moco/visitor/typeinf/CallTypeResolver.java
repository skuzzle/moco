package de.uni.bremen.monty.moco.visitor.typeinf;

import java.util.Collections;
import java.util.List;

import de.uni.bremen.monty.moco.ast.Location;
import de.uni.bremen.monty.moco.ast.ResolvableIdentifier;
import de.uni.bremen.monty.moco.ast.Scope;
import de.uni.bremen.monty.moco.ast.declaration.ClassDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeDeclaration;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;
import de.uni.bremen.monty.moco.exception.UnknownIdentifierException;
import de.uni.bremen.monty.moco.exception.UnknownTypeException;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;

class CallTypeResolver extends TypeResolverFragment {

    public CallTypeResolver(BaseVisitor resolver) {
        super(resolver);
    }

    public void resolveType(FunctionCall node) {
        if (checkIsConstructorCall(node)) {
            resolveConstructorCall(node);
        } else {
            resolveFunctionCall(node);
        }
    }

    private void resolveConstructorCall(FunctionCall node) {

    }

    private void resolveFunctionCall(FunctionCall node) {

    }

    private List<ProcedureDeclaration> getConstructorOverloads(Location location,
            ClassDeclaration decl) {
        try {
            return decl.getScope().resolveProcedure(location,
                    ResolvableIdentifier.of("initializer"));
        } catch (UnknownIdentifierException e) {
            return Collections.singletonList(decl.getDefaultInitializer());
        }
    }

    private boolean checkIsConstructorCall(FunctionCall call) {
        final Scope scope = call.getScope();
        try {
            final TypeDeclaration typeDecl = scope.resolveType(call, call.getIdentifier());
            if (!(typeDecl instanceof ClassDeclaration)) {
                reportError(call, "<%s> is not callable", call.getIdentifier());
            }
            call.setConstructorCall(typeDecl);
            return true;
        } catch (UnknownTypeException | UnknownIdentifierException e) {
        }
        return false;
    }
}
