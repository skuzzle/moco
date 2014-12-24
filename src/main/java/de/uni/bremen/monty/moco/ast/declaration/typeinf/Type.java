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

    protected static final Position UNKNOWN_POSITION = new Position("unknown", 0, 0);
    public static final Location UNKNOWN_LOCATION = new Location() {

        @Override
        public Position getPosition() {
            return UNKNOWN_POSITION;
        }
    };

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

    /**
     * Applies the given unification to this type. If the unification contains a
     * substitute for this type, the substitute will be returned.
     *
     * @param unification The unification to apply.
     * @return The substitute type.
     */
    abstract Type apply(Unification unification);

    @Override
    public String toString() {
        return this.name.getSymbol();
    }
}
