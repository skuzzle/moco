package de.uni.bremen.monty.moco.visitor.typeinf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Typed.TypeContext;
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
    public static Type findLeastCommonSuperType(Collection<Type> types) {
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
            // This should not happen, because all types should have Object as
            // super class
            throw new IllegalStateException(String.format(
                    "The types %s do not share a common super type", types));
        }

        // Chose the most concrete type
        ClassType minDistanceType = null;
        for (final ClassType type : commonTypes) {
            int dist = type.distanceToObject();
            if (minDistanceType == null || dist > minDistanceType.distanceToObject()) {
                minDistanceType = type;
            }
        }
        return minDistanceType;
    }

    private static void traverseSuperTypes(ClassType current, Set<ClassType> types) {
        if (types.add(current)) {
            for (final ClassType parent : current.getSuperClasses()) {
                traverseSuperTypes(parent, types);
            }
        }
    }

    /**
     * Creates the cartesian product of all possible signature types.
     *
     * @param actual The actual signature of a call.
     * @return Cartesian product of all types.
     */
    public static List<List<Type>> signatureTypes(List<Expression> actual) {
        final List<List<Type>> parameterTypes = new ArrayList<>(actual.size());
        for (final Expression parameter : actual) {
            final List<Type> types = new ArrayList<>();
            for (final TypeContext ctx : parameter.getTypes()) {
                types.add(ctx.getType());
            }
            parameterTypes.add(types);
        }
        return cartesianProduct(parameterTypes);
    }

    /**
     * Create the Cartesian product of given lists.
     *
     * @param lists The list to create the products of.
     * @return The Cartesian product.
     */
    private static <T> List<List<T>> cartesianProduct(List<List<T>> lists) {
        final List<List<T>> resultLists = new ArrayList<List<T>>();
        if (lists.size() == 0) {
            resultLists.add(new ArrayList<T>());
            return resultLists;
        } else {
            final List<T> firstList = lists.get(0);
            final List<List<T>> remainingLists = cartesianProduct(
                    lists.subList(1, lists.size()));

            for (T condition : firstList) {
                for (List<T> remainingList : remainingLists) {
                    ArrayList<T> resultList = new ArrayList<T>();
                    resultList.add(condition);
                    resultList.addAll(remainingList);
                    resultLists.add(resultList);
                }
            }
        }
        return resultLists;
    }

    public static ProcedureDeclaration bestFit(
            Collection<ProcedureDeclaration> candidates, FunctionCall call) {

        ProcedureDeclaration bestMatch = null;
        int bestRating = 0;
        boolean doubleMatch = false;

        outer: for (final ProcedureDeclaration candidate : candidates) {
            if (candidate.getParameter().size() != call.getArguments().size()) {
                continue;
            }

            final Iterator<VariableDeclaration> formalIt = candidate.getParameter().iterator();
            final Iterator<Expression> actualIt = call.getArguments().iterator();

            int rating = 0;
            while (formalIt.hasNext()) {
                final VariableDeclaration formal = formalIt.next();
                final Expression actual = actualIt.next();

                final Unification unification = Unification.testIf(actual).isA(formal);
                if (!unification.isSuccessful()) {
                    continue outer;
                }
                rating += rate(actual.getType(), formal.getType());
            }

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
            return null;
        } else if (bestMatch == null) {
            // no match
            return null;
        } else {
            // we have a winner
            return bestMatch;
        }
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
