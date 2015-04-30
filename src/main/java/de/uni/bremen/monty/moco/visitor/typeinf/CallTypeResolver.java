package de.uni.bremen.monty.moco.visitor.typeinf;

import java.util.Collections;
import java.util.List;

import de.uni.bremen.monty.moco.ast.ResolvableIdentifier;
import de.uni.bremen.monty.moco.ast.Scope;
import de.uni.bremen.monty.moco.ast.declaration.ClassDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Unification;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;
import de.uni.bremen.monty.moco.exception.UnknownIdentifierException;
import de.uni.bremen.monty.moco.exception.UnknownTypeException;
import de.uni.bremen.monty.moco.visitor.typeinf.TypeHelper.BestFit;

class CallTypeResolver extends TypeResolverFragment {

    public CallTypeResolver(TypeResolver resolver) {
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
        resolveCall(node, this::getConstructorOverloads);
    }

    private void resolveFunctionCall(FunctionCall node) {
        resolveCall(node, this::getOverloads);
    }

    private void resolveCall(
            FunctionCall node,
            java.util.function.Function<FunctionCall, List<ProcedureDeclaration>> overloadSupplier) {
        resolveTypesOf(node.getTypeArguments());

        final List<ProcedureDeclaration> candidates = overloadSupplier.apply(node);
        final BestFit bestFit = TypeHelper.bestFit(candidates, node, this);
        final ProcedureDeclaration match = bestFit.getBestMatch();
        final Unification unification = bestFit.getUnification();
        final Function callType = bestFit.getCallType();

        checkValidTypeParameterDecls(node, bestFit);
        final Function unified = unification.apply(callType);
        node.setType(unified.getReturnType());
        node.setDeclaration(match);
    }
    
    private List<ProcedureDeclaration> getConstructorOverloads(FunctionCall call) {
        final ClassDeclaration decl = call.getConstructorType();
        try {
            return decl.getScope().resolveProcedure(call,
                    ResolvableIdentifier.of("initializer"));
        } catch (UnknownIdentifierException e) {
            return Collections.singletonList(decl.getDefaultInitializer());
        }
    }

    private List<ProcedureDeclaration> getOverloads(FunctionCall node) {
        return node.getScope().resolveProcedure(node, node.getIdentifier());
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

    private void checkValidTypeParameterDecls(FunctionCall node, BestFit bestFit) {
        final ProcedureDeclaration match = bestFit.getBestMatch();
        if (node.getTypeArguments().size() > 0 &&
                node.getTypeArguments().size() != match.getTypeParameters().size()) {
            // either specify none or all type parameters
            reportError(node, "Call <%s> only specifies partial type parameters",
                    node.getIdentifier());
        }
        final Unification unification = bestFit.getUnification()
                .merge(node.getScope().getSubstitutions());
        if (!unification.substitutesAll(match.getTypeParameters())) {
            reportError(node, "Could not recover all type parameters from call of <%s>",
                    node.getIdentifier());
        }
    }
}
