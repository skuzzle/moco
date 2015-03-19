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

import de.uni.bremen.monty.moco.ast.Scope;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Product;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.TypeVariable;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Unification;
import de.uni.bremen.monty.moco.ast.expression.Expression;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;

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

    public static Optional<ProcedureDeclaration> bestFit(
            Collection<ProcedureDeclaration> candidates, FunctionCall call,
            BaseVisitor typeResolver) {

        ProcedureDeclaration bestMatch = null;
        int bestRating = 0;
        boolean doubleMatch = false;

        final Scope scope = call.getScope();

        final List<Type> actualSignature = new ArrayList<>(call.getArguments().size());
        for (final Expression actual : call.getArguments()) {
            actualSignature.add(actual.getType());
        }

        final Function callType = Function.named(call.getIdentifier())
                .atLocation(call)
                .returning(TypeVariable.anonymous().createType())
                .andParameters(actualSignature)
                .createType();

        outer: for (final ProcedureDeclaration candidate : candidates) {
            if (candidate.getParameter().size() != call.getArguments().size()) {
                continue;
            }

            candidate.visit(typeResolver);

            final Function candidateType = candidate.getType().asFunction().fresh(candidate.getScope());

            if (!Unification.testIf(callType).isA(candidateType).isSuccessful()) {
                continue outer;
            }
            int rating = rateSignature(callType.getParameters(), candidateType.getParameters());

            if (bestMatch == null || rating > bestRating) {
                bestMatch = candidate;
                bestRating = rating;
                doubleMatch = false;
            } else if (bestRating == rating) {
                doubleMatch = true;
            }
        }

        // TODO: error handling
        if (doubleMatch) {
            // ambiguous call
            return Optional.empty();
        } else if (bestMatch == null) {
            // no match
            return Optional.empty();
        } else {
            // we have a winner
            return Optional.of(bestMatch);
        }
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
        if (t1.isClass() && t2.isClass()) {
            return Math.abs(t1.asClass().distanceToObject()
                    - t2.asClass().distanceToObject());
        }
        return 0;
    }
}
