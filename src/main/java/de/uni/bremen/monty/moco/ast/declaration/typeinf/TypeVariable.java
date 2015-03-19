package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.Location;
import de.uni.bremen.monty.moco.ast.Position;

public class TypeVariable extends Type {

    public static final class Named {
        private final String name;
        private Location location;

        private Named(String name) {
            this.name = name;
            this.location = Location.UNKNOWN_LOCATION;
        }

        public Named atLocation(Location location) {
            if (location == null) {
                throw new IllegalArgumentException("location is null");
            }
            this.location = location;
            return this;
        }

        public TypeVariable createType() {
            return new TypeVariable(new Identifier(this.name),
                    this.location.getPosition());
        }
    }

    @Deprecated
    public static TypeVariable createType(String name, Position position) {
        return new TypeVariable(new Identifier(name), position);
    }

    @Deprecated
    public static TypeVariable createAnonymous(Position position) {
        return new TypeVariable(new Identifier(PREFIX + String.valueOf(counter++)),
                position);
    }

    public static Named named(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        return new Named(name);
    }

    public static Named named(Identifier identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException("identifier is null");
        }
        return named(identifier.getSymbol());
    }

    public static Named anonymous() {
        return new Named(PREFIX + String.valueOf(counter++));
    }

    private static int counter;
    private static final String PREFIX = "$VAR_";

    private TypeVariable(Identifier name, Position positionHint) {
        super(name, positionHint);
    }

    @Override
    Type apply(Unification unification) {
        return unification.getSubstitute(this);
    }

    @Override
    public boolean isA(Type other) {
        return this == other;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}
