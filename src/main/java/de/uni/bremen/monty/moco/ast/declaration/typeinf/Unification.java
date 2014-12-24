package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class Unification {

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

    private static final Map<TypePair, Unification> UNIFICATION_CACHE =
            new HashMap<>();

    public static final class UnificationBuilder {
        private final Type first;

        private UnificationBuilder(Type first) {
            this.first = first;
        }

        /**
         * Creates a Unification of the first type which has been specified at
         * {@link Unification#of(Type)} and the given second type. If those
         * types are not unifiable, the resulting Unification will be
         * {@link Unification#isSuccessful() unsuccessful}. If the types are
         * unifiable, the result provides the resolved substitutes for the type
         * variables which occurred in both type expressions.
         *
         * @param second The type to unify with.
         * @return The Unification.
         */
        public Unification with(Type second) {
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

    /**
     * Creates a unification from two types. Usage:
     *
     * <pre>
     * final Unification unification = Unification.of(type1).with(type2);
     * </pre>
     *
     * @param first The left hand type of the unification.
     * @return Builder object to specify the right hand type of the unification.
     */
    public static UnificationBuilder of(Type first) {
        if (first == null) {
            throw new IllegalArgumentException("first is null");
        }

        return new UnificationBuilder(first);
    }

    /**
     * Creates a new failed unification. Failed unifications do not contain any
     * substitutes. They can not be merged into successful ones and other
     * Unifications can not be merged into them.
     *
     * @return A new failed unification.
     */
    public static Unification failed() {
        return new Unification(false,
                Collections.<TypeVariable, Type> emptyMap());
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

    private final boolean success;
    private final Map<TypeVariable, Type> subst;

    private Unification(boolean success, Map<TypeVariable, Type> subst) {
        this.success = success;
        this.subst = Collections.unmodifiableMap(subst);
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
     * Tries to find a substitute for the given type in this unification. If no
     * substitute exists, the passed type itself will be returned. If this is
     * not a successful unification, this method throws an Exception.
     *
     * @param other The type to find the substitute for.
     * @return The substitute type of {@code other} if no substitute was found.
     */
    Type getSubstitute(Type other) {
        if (!isSuccessful()) {
            throw new IllegalStateException(
                    "Can't obtain substitute from unsuccessful unification");
        } else if (other == null) {
            throw new IllegalArgumentException("other is null");
        }

        final Type substitute = this.subst.get(other);
        return substitute == null ? other : substitute;
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
}
