package de.uni.bremen.monty.moco.visitor.typeinf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.Location;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeInstantiation;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Product;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.TypeContext;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.TypeVariable;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Typed;
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


    public static void testIsPossibleCast(TypeResolver resolver, Location location,
            Typed from, Typed to) {
        final Unification u1 = Unification
                .testIf(from)
                .isA(to);

        if (!u1.isSuccessful()) {
            final Unification u2 = Unification
                    .testIf(to)
                    .isA(from);

            if (!u2.isSuccessful()) {
                resolver.reportError(location, "Impossible cast: <%s> to <%s>",
                        from.getType(),
                        to.getType());
            }
        }
    }

    public static Optional<Type> findCommonType(Set<Type> types,
            TypeContext scope) {
        if (types.size() == 1) {
            return Optional.of(types.iterator().next());
        }

        boolean hasVoid = false;
        boolean hasNoneVoid = false;
        for (final Type type : types) {
            final boolean isVoid = type.equals(CoreClasses.voidType().getType());
            hasVoid |= isVoid;
            hasNoneVoid |= !isVoid;
        }
        if (hasVoid && hasNoneVoid) {
            return Optional.empty();
        } else if (hasVoid) {
            // this should not happen as input is a set
            throw new IllegalStateException("two different void types around");
        }

        final Map<Type, Set<Type>> typeMap = new HashMap<>(types.size());
        for (final Type outer : types) {
            for (final Type inner : types) {
                if (inner == outer) {
                    continue;
                }

                final Unification u1 = Unification.given(scope).testIf(outer).isA(inner);

                Type commonInner = outer;
                if (u1.isSuccessful()) {
                    commonInner = inner;
                } else {
                    final Unification u2 = Unification.given(scope).testIf(inner).isA(outer);
                    if (u2.isSuccessful()) {
                        commonInner = outer;
                    }
                }
                typeMap.computeIfAbsent(outer, type -> new HashSet<>()).add(commonInner);
            }
        }

        final Iterator<Set<Type>> it = typeMap.values().iterator();
        Set<Type> current = it.next();
        while (it.hasNext()) {
            current.retainAll(it.next());
        }
        final Comparator<Type> byDistance = Comparator
                .comparing(Type::distanceToObject)
                .reversed();

        return current.stream().sorted(byDistance).findFirst();
    }

    public static Optional<Type> findCommonTyped(TypeContext scope, Typed... typedNodes) {
        return findCommonType(Arrays.stream(typedNodes)
                .map(Typed::getType)
                .collect(Collectors.toSet()), scope);
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

        final List<Type> actualSignature = typeResolver.resolveTypesOf(call.getArguments());

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

            int rating = rateSignature(unification, callType.getParameters(),
                    candidateType.getParameters());

            if (rating <= bestRating) {
                if (rating < bestRating) {
                    matches.clear();
                }
                bestRating = rating;
                matches.add(candidate);
                bestUnification = unification;
            }
        }

        assert bestUnification.isSuccessful();

        for (final TypeInstantiation actualTypeArg : call.getTypeArguments()) {
            bestUnification = bestUnification.merge(actualTypeArg.getUnification());
        }
        return new BestFit(matches, bestUnification, callType)
                .checkIsUnique(call, typeResolver);
    }

    private static int rateSignature(Unification subst, Product call, Product candidate) {
        assert call.getComponents().size() == candidate.getComponents().size();

        final Product fun1Unified = subst.apply(call);
        final Product fun2Unified = candidate;

        final Iterator<Type> fun1It = fun1Unified.getComponents().iterator();
        final Iterator<Type> fun2It = fun2Unified.getComponents().iterator();

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
            // TODO: XOR instead of OR?
            return Integer.MAX_VALUE - 1;
        }
        return 0;
    }
}
