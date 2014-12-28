package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import java.util.ArrayList;
import java.util.List;

import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.Location;
import de.uni.bremen.monty.moco.ast.Position;

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

    protected Type(Identifier name, Position positionHint) {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        } else if (positionHint == null) {
            throw new IllegalArgumentException("positionHint is null");
        }

        this.name = name;
        this.positionHint = positionHint;
    }

    public Identifier getName() {
        return this.name;
    }

    @Override
    public Position getPosition() {
        return this.positionHint;
    }

    /**
     * Whether this is a type variable.
     *
     * @return Whether this is a type variable.
     */
    public abstract boolean isVariable();

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

    @Override
    public String toString() {
        return this.name.getSymbol();
    }
}
