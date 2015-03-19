package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import java.util.ArrayList;
import java.util.List;

import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.Location;
import de.uni.bremen.monty.moco.ast.Position;
import de.uni.bremen.monty.moco.ast.Scope;
import de.uni.bremen.monty.moco.util.DebugUtil;

public abstract class Type implements Location {

    public static List<Type> convert(List<? extends Typed> types) {
        final List<Type> result = new ArrayList<>(types.size());
        for (final Typed typed : types) {
            if (typed.getType() == null) {
                throw new IllegalStateException("encountered null type");
            }
            result.add(typed.getType());
        }
        return result;
    }

    private final Identifier name;
    private final Position positionHint;

    // XXX: remove
    private final String trace;

    protected Type(Identifier name, Position positionHint) {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        } else if (positionHint == null) {
            throw new IllegalArgumentException("positionHint is null");
        }

        this.name = name;
        this.positionHint = positionHint;
        this.trace = DebugUtil.getCurrentLocation();
    }

    public Identifier getName() {
        return this.name;
    }

    @Override
    public Position getPosition() {
        return this.positionHint;
    }

    /**
     * Creates a structural identical type from this one, replacing all bound
     * type variables with fresh mono types.
     *
     * @param scope The current scope.
     * @return A fresh copy of this type.
     */
    public Type fresh(Scope scope) {
        return Unification.fresh(this, scope);
    }

    /**
     * Whether this is a type variable.
     *
     * @return Whether this is a type variable.
     */
    public boolean isVariable() {
        return this instanceof TypeVariable;
    }

    /**
     * Convenience method for casting this type to {@link TypeVariable}.
     *
     * @return This, casted to {@link TypeVariable}.
     */
    public TypeVariable asVariable() {
        return (TypeVariable) this;
    }

    /**
     * Whether this is a {@link ClassType}.
     *
     * @return Whether this is a {@link ClassType}.
     */
    public boolean isClass() {
        return this instanceof ClassType;
    }

    /**
     * Convenience method for casting this type to {@link ClassType}.
     *
     * @return This, casted to {@link ClassType}.
     */
    public ClassType asClass() {
        return (ClassType) this;
    }

    /**
     * Whether this is a {@link Function}.
     *
     * @return Whether this is a {@link Function}.
     */
    public boolean isFunction() {
        return this instanceof Function;
    }

    /**
     * Convenience method for casting this type to {@link Function}.
     *
     * @return This, casted to {@link Function}.
     */
    public Function asFunction() {
        return (Function) this;
    }

    /**
     * Whether this is a {@link Product}.
     *
     * @return Whether this is a {@link Product}.
     */
    public boolean isProduct() {
        return this instanceof Product;
    }

    /**
     * Convenience method for casting this type to {@link Product}.
     *
     * @return This, casted to {@link Product}.
     */
    public Product asProduct() {
        return (Product) this;
    }

    abstract Type apply(Unification unification);

    /**
     * Determines exact equality of two Types. This does not take any
     * inheritance into account. Type compatibility must always be checked using
     * the {@link Unification} class.
     *
     * @param obj The object to compare to.
     * @return Whether the types are exactly equal.
     */
    @Override
    public abstract boolean equals(Object obj);

    public abstract boolean isA(Type other);

    @Override
    public String toString() {
        return this.name.getSymbol();
    }
}
