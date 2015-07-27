package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

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

    /** A successful unification which does not contain any substitutions */
    public static final Unification EMPTY = successful(
            Collections.<TypeVariable, Type> emptyMap());

    /** An unsuccessful unification */
    public static final Unification FAILED = new Unification(false, null);

    public static final class TestIfBuilder {
        private final Type first;
        private final TypeContext context;
        private final Set<UnificationOption> options;

        private TestIfBuilder(Type first) {
            this(first, var -> false, Collections.emptySet());
        }

        private TestIfBuilder(Type first, TypeContext context,
                Set<UnificationOption> options) {
            this.first = first;
            this.context = context;
            this.options = options;
        }

        public Unification isA(Typed typedNode) {
            Objects.requireNonNull(typedNode);
            return isA(typedNode.getType());
        }

        public Unification isA(Type second) {
            Objects.requireNonNull(second);
            final Unifier unifier = new Unifier(this.context, this.options);
            return unifier.unify(this.first, second).deep();
        }
    }

    public static final class GivenBuilder {
        private final TypeContext context;
        private final Set<UnificationOption> options;

        private GivenBuilder(TypeContext context) {
            this.context = context;
            this.options = new HashSet<>();
        }
        
        public Unification substituteForFresh() {
            final Map<TypeVariable, Type> types = new HashMap<>();
            return new Unification(true, types) {
                @Override
                Type getSubstitute(TypeVariable other) {
                    if (types.containsKey(other)) {
                        return types.get(other);
                    } else if (!context.isFree(other)) {
                        final TypeVariable fresh = TypeVariable.named(other.getName() + "$F")
                                .atLocation(other)
                                .withOrigin(other)
                                .createType();
                        types.put(other, fresh);
                        return fresh;
                    }
                    return other;
                }
            };
        }

        public GivenBuilder and(UnificationOption option) {
            this.options.add(option);
            return this;
        }

        public TestIfBuilder testIf(Type type) {
            return new TestIfBuilder(type, this.context, this.options);
        }

        public TestIfBuilder testIf(Typed typed) {
            return new TestIfBuilder(typed.getType(), this.context, this.options);
        }
    }

    public static final class SimultaneousBuilder {
        private final Iterator<TypeVariable> typeVars;

        public SimultaneousBuilder(Iterator<TypeVariable> typeVars) {
            this.typeVars = typeVars;
        }

        public Unification simultaneousFor(Collection<? extends Typed> typedNodes) {
            Objects.requireNonNull(typedNodes);
            return simultaneousFor(typedNodes.stream().map(Typed::getType).iterator());
        }

        public Unification simultaneousFor(Iterable<? extends Type> types) {
            Objects.requireNonNull(types);
            return simultaneousFor(types.iterator());
        }
        
        public Unification simultaneousFor(Iterator<? extends Type> types) {
            Objects.requireNonNull(types);
            
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

    public static GivenBuilder given(TypeContext context) {
        return new GivenBuilder(context);
    }

    public static TestIfBuilder testIf(Type first) {
        Objects.requireNonNull(first);
        return new TestIfBuilder(first);
    }

    public static TestIfBuilder testIf(Typed typedNode) {
        Objects.requireNonNull(typedNode);
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

    public static SimultaneousBuilder substitute(Collection<? extends Type> typeVars) {
        return new SimultaneousBuilder(typeVars.stream().map(Type::asVariable).iterator());
    }

    public static SimultaneousBuilder substitute(Stream<? extends Typed> stream) {
        return new SimultaneousBuilder(stream
                .map(Typed::getType)
                .map(Type::asVariable)
                .iterator());
    }

    /**
     * Creates a new successful unification containing the given substitutes.
     *
     * @param subst Map of substitutes.
     * @return The new successful unification.
     */
    public static Unification successful(Map<TypeVariable, Type> subst) {
        Objects.requireNonNull(subst);
        return new Unification(true, subst);
    }

    private final boolean success;
    protected final Map<TypeVariable, Type> subst;

    private Unification(boolean success, Map<TypeVariable, Type> subst) {
        this.success = success;
        this.subst = subst;
    }

    private Unification deep() {
        if (this.success) {
            for (final Entry<TypeVariable, Type> e : this.subst.entrySet()) {
                e.setValue(apply(e.getValue()));
            }
        }
        return this;
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
        Objects.requireNonNull(term);
        if (!isSuccessful()) {
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
        Objects.requireNonNull(typedNode);
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
        Objects.requireNonNull(other);
        if (!isSuccessful()) {
            throw new IllegalStateException(
                    "Can't obtain substitute from unsuccessful unification");
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
        Objects.requireNonNull(other);
        if (!isSuccessful()) {
            throw new IllegalStateException(
                    "Can not merge into unsuccessful unification");
        } else if (!other.isSuccessful()) {
            throw new IllegalArgumentException(
                    "Can not merge from unsuccessful unification");
        } else if (other == this) {
            return this;
        }
        final Map<TypeVariable, Type> resultMap = new HashMap<>(
                this.subst.size() + other.subst.size());
        resultMap.putAll(this.subst);
        resultMap.putAll(other.subst);
        return Unification.successful(resultMap);
    }

    /**
     * Creates a unification containing the substitutions from both this
     * Unification and the given {@code other} Unification object. If there
     * exist two substitutions for the same variable, the one from {@code other}
     * takes precedence.
     *
     * <p>
     * if either this or the other unification is not successful, then
     * {@code this} is returned with no modifications
     * </p>
     *
     * @param other The Unification to merge with this one.
     * @return A new successful unification, containing all substitutions from
     *         this and {@code other}
     */
    public Unification mergeIfSuccessful(Unification other) {
        Objects.requireNonNull(other);
        if (!isSuccessful() || !other.isSuccessful()) {
            return this;
        }
        final Map<TypeVariable, Type> resultMap = new HashMap<>(
                this.subst.size() + other.subst.size());
        resultMap.putAll(this.subst);
        resultMap.putAll(other.subst);
        return Unification.successful(resultMap);
    }

    /**
     * Determines whether this Unification contains substitutions for all types
     * in the given collection.
     *
     * @param types The types to check for.
     * @return Whether all given types are substituted by this Unification.
     */
    public boolean substitutesAll(Collection<? extends Typed> types) {
        return isSuccessful()
            && types.stream().map(Typed::getType)
                    .allMatch(this.subst.keySet()::contains);
    }

    @Override
    public String toString() {
        return this.subst == null
                ? "[]"
                : this.subst.toString();
    }
}
