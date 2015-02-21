package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Unification is the process of determining structural equality of two
 * {@link Type type expressions}. This class represents the result of unifying
 * one type with another. If the unification of two types is successful
 * (&lt;=&gt; the types are compatible), then the resulting Unification object
 * holds substitutions for type variables which occurred in either of the type
 * expressions.
 *
 * @author Simon Taddiken
 */
public class Unification {

    private static final class TypePair {
        private final Type t1;
        private final Type t2;

        private TypePair(Type t1, Type t2) {
            this.t1 = t1;
            this.t2 = t2;
        }

        @Override
        public int hashCode() {
            int hash = 11 * this.t1.hashCode();
            hash += 31 * this.t2.hashCode();
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || obj instanceof TypePair &&
                    this.t1 == ((TypePair) obj).t1 &&
                    this.t2 == ((TypePair) obj).t2;
        }
    }

    /** A successful unification which does not contain any substitutions */
    public static final Unification EMPTY = successful(
            Collections.<TypeVariable, Type> emptyMap());

    /** An unsuccessful unification */
    public static final Unification FAILED = new Unification(false, null);

    private static final Map<TypePair, Unification> UNIFICATION_CACHE =
            new HashMap<>();

    public static final class TestIfBuilder {
        private final Type first;

        private TestIfBuilder(Type first) {
            this.first = first;
        }

        public Unification isA(Typed typedNode) {
            return isA(typedNode.getType());
        }

        public Unification isA(Type second) {
            if (second == null) {
                throw new IllegalArgumentException("second is null");
            }

            final TypePair pair = new TypePair(this.first, second);
            Unification unification = UNIFICATION_CACHE.get(pair);
            if (unification == null) {
                final Unifier unifier = new Unifier();
                unification = unifier.unify(this.first, second);
                UNIFICATION_CACHE.put(pair, unification);
            }
            return unification;
        }
    }

    public static final class SimultaneousBuilder {
        private final Iterator<TypeVariable> typeVars;

        public SimultaneousBuilder(Iterator<TypeVariable> typeVars) {
            this.typeVars = typeVars;
        }

        public Unification simultaneousFor(Collection<? extends Typed> typedNodes) {
            if (typedNodes == null) {
                throw new IllegalArgumentException("typedNodes is null");
            }
            return simultaneousFor(typedNodes.stream().map(Typed::getType).iterator());
        }

        public Unification simultaneousFor(Iterable<Type> types) {
            if (types == null) {
                throw new IllegalArgumentException("types is null");
            }
            return simultaneousFor(types.iterator());
        }

        public Unification simultaneousFor(Iterator<Type> types) {
            if (types == null) {
                throw new IllegalArgumentException("types is null");
            }
            final Map<TypeVariable, Type> subst = new HashMap<>();
            while (this.typeVars.hasNext()) {
                if (!types.hasNext()) {
                    throw new IllegalArgumentException("size mismatch");
                }
                subst.put(this.typeVars.next(), types.next());
            }
            if (types.hasNext()) {
                throw new IllegalArgumentException("size mismatch");
            }
            return Unification.successful(subst);
        }
    }

    public static TestIfBuilder testIf(Type first) {
        if (first == null) {
            throw new IllegalArgumentException("first is null");
        }
        return new TestIfBuilder(first);
    }

    public static TestIfBuilder testIf(Typed typedNode) {
        if (typedNode == null) {
            throw new IllegalArgumentException("typedNode is null");
        }

        return new TestIfBuilder(typedNode.getType());
    }

    /**
     * Creates a failed unification. Failed unifications do not contain any
     * substitutes. They can not be merged into successful ones and other
     * Unifications can not be merged into them.
     *
     * @return A failed unification.
     */
    public static Unification failed() {
        return FAILED;
    }

    public static SimultaneousBuilder substitute(Iterable<TypeVariable> typeVars) {
        return new SimultaneousBuilder(typeVars.iterator());
    }

    public static SimultaneousBuilder substitute(Collection<? extends Type> typeVars) {
        return new SimultaneousBuilder(typeVars.stream().map(Type::asVariable).iterator());
    }

    /**
     * Creates a new successful unification containing the given substitutes.
     *
     * @param subst Map of substitutes.
     * @return The new successful unification.
     */
    public static Unification successful(Map<TypeVariable, Type> subst) {
        if (subst == null) {
            throw new IllegalArgumentException("subst is null");
        }
        return new Unification(true, subst);
    }

    static <T extends Type> T fresh(T type) {
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }
        final Unification fresh = new Unification(true, new HashMap<>()) {
            @Override
            Type getSubstitute(TypeVariable other) {
                Type subst = this.subst.get(other);
                if (subst == null) {
                    subst = TypeVariable
                            .named(other.getName())
                            .atLocation(other.getPosition())
                            .createType();
                    this.subst.put(other, subst);
                }
                return subst;
            }
        };
        return fresh.apply(type);
    }

    private final boolean success;
    protected final Map<TypeVariable, Type> subst;

    private Unification(boolean success, Map<TypeVariable, Type> subst) {
        this.success = success;
        this.subst = subst;
    }

    /**
     * Whether this unification was successful.
     *
     * @return Whether this unification was successful.
     */
    public boolean isSuccessful() {
        return this.success;
    }

    /**
     * Builds the composition of this Unification with the given {@code other}.
     * Applying the resulting Unification to a {@link Type} {@code S} yields the
     * same result as if first applying {@code other} to {@code S}, and then
     * applying {@code this} to the resulting term. If
     *
     * <pre>
     * Unification delta = ...
     * Unification gamma = ...
     * Type S = ...
     * </pre>
     *
     * then
     *
     * <pre>
     * delta.apply(gamma).apply(S) = delta.apply(gamma.apply(S))
     * </pre>
     *
     * @param other The Unification to compose with.
     * @return A new Unification representing the composition.
     */
    public Unification apply(Unification other) {
        if (other == null) {
            throw new IllegalArgumentException("other is null");
        } else if (other == this) {
            throw new IllegalArgumentException("can not apply to self");
        } else if (!other.isSuccessful()) {
            throw new IllegalStateException("can not apply unsuccesful unification");
        } else if (!isSuccessful()) {
            throw new IllegalStateException("can not apply on unsuccessful unification");
        }
        final Map<TypeVariable, Type> resultMap = new HashMap<>(this.subst.size());
        for (final Entry<TypeVariable, Type> e : other.subst.entrySet()) {
            resultMap.put(e.getKey(), e.getKey().apply(this));
        }
        for (final Entry<TypeVariable, Type> e : this.subst.entrySet()) {
            if (!other.subst.containsKey(e.getKey())) {
                resultMap.put(e.getKey(), e.getValue());
            }
        }
        return successful(resultMap);
    }

    /**
     * Applies this Unification to the given {@link Type}. The result is a new
     * {@linkplain Type}, in which all occurrences of {@link TypeVariable type
     * variables} which occur in the domain of this Unification are replaced by
     * their respective substitute.
     *
     * @param term The type to which this Unification should be applied.
     * @return A new Type.
     */
    @SuppressWarnings("unchecked")
    public <T extends Type> T apply(T term) {
        if (term == null) {
            throw new IllegalArgumentException("term is null");
        } else if (!isSuccessful()) {
            throw new IllegalStateException("can not apply non-successful unification");
        }

        return (T) term.apply(this);
    }

    /**
     * Applies this Unification to the type of the given {@link Typed typed
     * node}.
     * 
     * @param typedNode Node to obtain the type from.
     * @return A new type.
     * @see #apply(Type)
     */
    public Type apply(Typed typedNode) {
        if (typedNode == null) {
            throw new IllegalArgumentException("typedNode is null");
        }
        return apply(typedNode.getType());
    }

    /**
     * Tries to find a substitute for the given type in this unification. If no
     * substitute exists, the passed type itself will be returned. If this is
     * not a successful unification, this method throws an Exception.
     *
     * @param other The type to find the substitute for.
     * @return The substitute type of {@code other} if no substitute was found.
     */
    Type getSubstitute(TypeVariable other) {
        if (!isSuccessful()) {
            throw new IllegalStateException(
                    "Can't obtain substitute from unsuccessful unification");
        } else if (other == null) {
            throw new IllegalArgumentException("other is null");
        }

        final Type substitute = this.subst.get(other);
        return substitute == null
                ? other
                : substitute;
    }

    /**
     * Creates a unification containing the substitutions from both this
     * Unification and the given {@code other} Unification object. If there
     * exist two substitutions for the same variable, the one from {@code other}
     * takes precedence.
     *
     * <p>
     * If this is not a successful Unification or {@code other} is not a
     * successful Unification, this method throws an Exception.
     * </p>
     *
     * @param other The Unification to merge with this one.
     * @return A new successful unification, containing all substitutions from
     *         this and {@code other}
     */
    public Unification merge(Unification other) {
        if (other == null) {
            throw new IllegalArgumentException("other is null");
        } else if (!isSuccessful()) {
            throw new IllegalStateException(
                    "Can not merge into unsuccessful unification");
        } else if (!other.isSuccessful()) {
            throw new IllegalArgumentException(
                    "Can not merge from unsuccessful unification");
        }
        final Map<TypeVariable, Type> resultMap = new HashMap<>(
                this.subst.size() + other.subst.size());
        resultMap.putAll(this.subst);
        resultMap.putAll(other.subst);
        return Unification.successful(resultMap);
    }

    @Override
    public String toString() {
        return this.subst == null
                ? "[]"
                : this.subst.toString();
    }
}
