package de.uni.bremen.monty.moco.visitor.typeinf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.uni.bremen.monty.moco.ast.Scope;
import de.uni.bremen.monty.moco.ast.declaration.ClassDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.Declaration;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Unification;
import de.uni.bremen.monty.moco.ast.expression.Expression;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;
import de.uni.bremen.monty.moco.exception.UnknownIdentifierException;
import de.uni.bremen.monty.moco.exception.UnknownTypeException;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;
import de.uni.bremen.monty.moco.util.astsearch.SearchAST;
import de.uni.bremen.monty.moco.visitor.typeinf.TypeHelper.BestFit;

class CallTypeResolver extends TypeResolverFragment {

    public CallTypeResolver(TypeResolver resolver) {
        super(resolver);
    }

    public void resolveCallType(FunctionCall node) {
        final List<ProcedureDeclaration> overloads;
        if (checkIsConstructorCall(node)) {
            overloads = getConstructorOverloads(node);
        } else {
            overloads = getOverloads(node);
        }
        resolveCall(node, overloads);
    }

    private void resolveCall(FunctionCall node, List<ProcedureDeclaration> candidates) {
        resolveTypesOf(node.getTypeArguments());

        final BestFit bestFit = TypeHelper.bestFit(candidates, node, this);
        checkIsUnique(bestFit, node);

        final ProcedureDeclaration match = bestFit.getBestMatch();
        final Unification unification = bestFit.getUnification();
        final Function callType = bestFit.getCallType();

        checkValidTypeParameterDecls(node, bestFit);
        final Function unified = unification.apply(callType);
        node.setType(unified.getReturnType());
        node.setDeclaration(match);
        match.addUsage(node);
        if (isRecursive(node, match)) {
            match.addRecursiveCall(node);

            final Function matchType = match.getType().asFunction();
            final Type retType = matchType.getReturnType();
            if (retType.isVariable() && retType.asVariable().isIntermediate()) {
                reportError(node, "Functions with inferred return type can not be called recursively");
            }
        }
        node.setTypeDeclarationIfResolved(match);
    }

    private boolean isRecursive(FunctionCall call, ProcedureDeclaration callDecl) {
        return SearchAST
                .forParent(ProcedureDeclaration.class)
                .where(Predicates.is(callDecl))
                .in(call)
                .isPresent();
    }

    private List<ProcedureDeclaration> getConstructorOverloads(FunctionCall call) {
        final ClassDeclaration decl = call.getConstructorType();
        final List<ProcedureDeclaration> result = new ArrayList<>();
        for (final Declaration declaration : decl.getBlock().getDeclarations()) {
            if ("initializer".equals(declaration.getIdentifier().getSymbol())) {
                if (declaration instanceof ProcedureDeclaration) {
                    // and not a function
                    if (!(declaration instanceof FunctionDeclaration)) {
                        result.add((ProcedureDeclaration) declaration);
                    }
                }
            }
        }

        if (result.isEmpty()) {
            return Collections.singletonList(decl.getDefaultInitializer());
        }
        return result;
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
            resolveTypeOf(typeDecl);
            call.setConstructorCall(typeDecl);
            return true;
        } catch (UnknownTypeException | UnknownIdentifierException e) {
        }
        return false;
    }

    private void checkIsUnique(BestFit bestFit, FunctionCall call) {
        final Collection<ProcedureDeclaration> matches = bestFit.getMatches();
        if (matches.isEmpty()) {
            reportError(call, "Found no matching overload of <%s>",
                    call.getIdentifier());
        } else if (matches.size() > 1) {
            final StringBuilder b = new StringBuilder();
            final Iterator<ProcedureDeclaration> it = matches.iterator();
            while (it.hasNext()) {
                b.append(it.next().getType());
                b.append("\n");
            }
            final StringBuilder b2 = new StringBuilder();
            b2.append(call.getIdentifier()).append("(");
            final Iterator<Expression> expIt = call.getArguments().iterator();
            while (expIt.hasNext()) {
                b2.append(expIt.next().getType());
                if (expIt.hasNext()) {
                    b2.append(" x ");
                }
            }
            b2.append(")");
            reportError(call, "Ambiguous call.%nCall: %s%nCandidates:%n%s",
                    b2.toString(),
                    b.toString());
        }
    }

    private void checkValidTypeParameterDecls(FunctionCall node, BestFit bestFit) {
        final ProcedureDeclaration match = bestFit.getBestMatch();
        if (!node.getTypeArguments().isEmpty() &&
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
