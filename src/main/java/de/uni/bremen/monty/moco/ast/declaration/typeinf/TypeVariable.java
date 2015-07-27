package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import java.util.Objects;

import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.Location;
import de.uni.bremen.monty.moco.ast.Position;
import de.uni.bremen.monty.moco.ast.declaration.TypeVariableDeclaration;

public class TypeVariable extends IdentityType {

    public static final class Named {
        private final String name;
        private Location location;
        private TypeVariable origin;

        private Named(String name) {
            this.name = name;
            this.location = Location.UNKNOWN_LOCATION;
        }

        public Named atLocation(Location location) {
            Objects.requireNonNull(location);
            this.location = location;
            return this;
        }
        
        public Named withOrigin(TypeVariable origin) {
            Objects.requireNonNull(origin);
            this.origin = origin;
            return this;
        }

        public TypeVariable createType() {
            return new TypeVariable(this.name,
                    this.location.getPosition(), origin);
        }
    }

    public static Named named(String name) {
        Objects.requireNonNull(name);
        return new Named(name);
    }

    public static Named named(Identifier identifier) {
        Objects.requireNonNull(identifier);
        return named(identifier.getSymbol());
    }

    public static Named anonymous() {
        return new Named(PREFIX + String.valueOf(counter++));
    }

    private static int counter;
    private static final String PREFIX = "$VAR_";
    private final TypeVariable origin;

    private TypeVariable(String name, Position positionHint, TypeVariable origin) {
        super(name, positionHint);
        this.origin = origin;

    }

    public TypeVariable getOrigin() {
        return this.origin;
    }
    
    @Override
    public int distanceToObject() {
        // Type variables count as object itself (as long as we have no bounded
        // quantification)
        return 0;
    }

    /**
     * Determines whether this is an artificially created intermediate type variable.
     * That is, there is no concrete {@link TypeVariableDeclaration} where this type
     * originates from.
     *
     * @return Whether this is an intermediate variable.
     */
    public boolean isIntermediate() {
        // XXX: probably not the best condition
        return getName().getSymbol().startsWith("$");
    }

    @Override
    Type apply(Unification unification) {
        return unification.getSubstitute(this);
    }
    
    @Override
    public String toString() {
        final int id = System.identityHashCode(this) % 100;
        return super.toString();
    }
}
