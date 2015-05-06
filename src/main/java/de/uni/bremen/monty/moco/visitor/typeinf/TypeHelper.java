package de.uni.bremen.monty.moco.visitor.typeinf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeInstantiation;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Product;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.TypeVariable;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Unification;
import de.uni.bremen.monty.moco.ast.expression.Expression;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;

/**
 * Provides static helper methods to operate with {@link Type Types}.
 *
 * @author Simon Taddiken
 */
public final class TypeHelper {

    private TypeHelper() {
        // hidden
    }

    /**
     * Finds the most specialized, common super type of all types in the given
     * list. The computation should always yield a result as all types inherit
     * from Object. If this is not the case for the input, an exception will be
     * thrown.
     *
     * @param types A list of types.
     * @return The common super type.
     */
    public static Optional<Type> findLeastCommonSuperType(Collection<Type> types) {
        if (types.size() == 1) {
            // hack for now...
            return Optional.of(types.iterator().next());
        }
        final Map<Type, Set<ClassType>> superTypeMap = new HashMap<>();
        // For each type, collect super types
        final Set<ClassType> commonTypes = new HashSet<>();
        for (final Type type : types) {
            final Set<ClassType> superTypes = new HashSet<>();
            traverseSuperTypes(type.asClass(), superTypes);
            superTypeMap.put(type, superTypes);
            commonTypes.addAll(superTypes);
        }

        // build intersection of all encountered super types
        for (final Set<ClassType> superTypes : superTypeMap.values()) {
            commonTypes.retainAll(superTypes);
        }
        if (commonTypes.isEmpty()) {
            return Optional.empty();
        }

        // Chose the most concrete type
        ClassType minDistanceType = null;
        for (final ClassType type : commonTypes) {
            int dist = type.distanceToObject();
            if (minDistanceType == null || dist > minDistanceType.distanceToObject()) {
                minDistanceType = type;
            }
        }
        return Optional.of(minDistanceType);
    }

    private static void traverseSuperTypes(ClassType current, Set<ClassType> types) {
        if (types.add(current)) {
            for (final ClassType parent : current.getSuperClasses()) {
                traverseSuperTypes(parent, types);
            }
        }
    }

    /**
     * Represents the result of finding the best fitting procedure declaration
     * to a function/procedure call.
     *
     * @author Simon Taddiken
     * @see TypeHelper#bestFit(Collection, FunctionCall, TypeResolver)
     */
    public final static class BestFit {
        private final List<ProcedureDeclaration> matches;
        private final Unification unification;
        private final Function callType;

        private BestFit(List<ProcedureDeclaration> matches, Unification unification,
                Function callType) {
            this.matches = matches;
            this.unification = unification;
            this.callType = callType;
        }

        public boolean isUnique() {
            return this.matches.size() == 1;
        }

        public Unification getUnification() {
            assert isUnique();
            return this.unification;
        }

        public Function getCallType() {
            assert isUnique();
            return this.callType;
        }

        public ProcedureDeclaration getBestMatch() {
            assert isUnique();
            return this.matches.get(0);
        }

        private BestFit checkIsUnique(FunctionCall call, TypeResolver typeResolver) {
            if (this.matches.isEmpty()) {
                typeResolver.reportError(call,
                        "Found no matching overload of <%s>",
                        call.getIdentifier());
            } else if (this.matches.size() > 1) {
                final StringBuilder b = new StringBuilder();
                final Iterator<ProcedureDeclaration> it = this.matches.iterator();
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
                typeResolver.reportError(call,
                        "Ambiguous call.%nCall: %s%nCandidates:%n%s",
                        b2.toString(),
                        b.toString());
            }
            return this;
        }
    }

    /**
     * Resolves the best fitting procedure or function declaration to the given
     * call. If the overload can not be resolved uniquely, an error is reported
     * at the given {@code typeResolver}.
     *
     * @param candidates The overload candidates.
     * @param call The call.
     * @param typeResolver The type resolver.
     * @return A {@link BestFit} instance holding the best matching result and
     *         an {@link Unification} containing substitutions for type
     *         variables.
     */
    public static BestFit bestFit(Collection<ProcedureDeclaration> candidates,
            FunctionCall call, TypeResolver typeResolver) {

        final List<Type> actualSignature = new ArrayList<>(call.getArguments().size());
        for (final Expression actual : call.getArguments()) {
            typeResolver.resolveTypeOf(actual);
            actualSignature.add(actual.getType());
        }

        final Function callType = Function.named(call.getIdentifier())
                .atLocation(call)
                .returning(TypeVariable.anonymous().createType())
                .andParameters(actualSignature)
                .createType();

        final Unification callScope = call.getScope().getSubstitutions();

        int bestRating = Integer.MAX_VALUE;
        Unification bestUnification = Unification.EMPTY;
        final List<ProcedureDeclaration> matches = new ArrayList<>();

        outer: for (final ProcedureDeclaration candidate : candidates) {
            Unification unification = Unification.EMPTY;

            if (candidate.getParameter().size() != call.getArguments().size()) {
                continue;
            }

            typeResolver.resolveTypeOf(candidate);

            // determine the context which supplies external substitutions
            Unification context = callScope;
            if (!call.getTypeArguments().isEmpty()) {
                // type args have explicitly been specified at the call
                if (candidate.getTypeParameters().size() != call.getTypeArguments().size()) {
                    continue;
                }
                context = Unification.substitute(candidate.getTypeParameters().stream())
                        .simultaneousFor(call.getTypeArguments());
            }

            final Function candidateType = context.apply(candidate).asFunction();

            unification = Unification
                    .given(call.getScope())
                    .testIf(callType)
                    .isA(candidateType)
                    .mergeIfSuccessful(context);
            if (!unification.isSuccessful()) {
                continue outer;
            }
            int rating = rateSignature(callType.getParameters(),
                    candidateType.getParameters());

            if (rating < bestRating) {
                matches.clear();
                bestRating = rating;
                matches.add(candidate);
                bestUnification = unification;
            } else if (bestRating == rating) {
                matches.add(candidate);
            }
        }

        assert bestUnification.isSuccessful();

        for (final TypeInstantiation actualTypeArg : call.getTypeArguments()) {
            bestUnification = bestUnification.merge(actualTypeArg.getUnification());
        }
        return new BestFit(matches, bestUnification, callType)
                .checkIsUnique(call, typeResolver);
    }

    private static int rateSignature(Product fun1, Product fun2) {
        assert fun1.getComponents().size() == fun2.getComponents().size();
        final Iterator<Type> fun1It = fun1.getComponents().iterator();
        final Iterator<Type> fun2It = fun2.getComponents().iterator();

        int rate = 0;
        while (fun1It.hasNext()) {
            rate += rate(fun1It.next(), fun2It.next());
        }
        return rate;
    }

    private static int rate(Type t1, Type t2) {
        assert !t1.isFunction();
        assert !t2.isFunction();
        if (t1 == t2) {
            return 0;
        } else if (t1.isClass() && t2.isClass()) {
            return Math.abs(t1.asClass().distanceToObject()
                    - t2.asClass().distanceToObject());
        } else if (t1.isVariable() || t2.isVariable()) {
            return Integer.MAX_VALUE;
        }
        return 0;
    }
}
