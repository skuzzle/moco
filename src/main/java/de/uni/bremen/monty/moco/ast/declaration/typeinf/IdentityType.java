package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.Position;

public class IdentityType extends Type {

    public IdentityType(String name, Position position) {
        super(Identifier.of(name), position);
    }

    public IdentityType(String name) {
        super(Identifier.of(name), UNKNOWN_POSITION);
    }

    @Override
    public int distanceToObject() {
        throw new UnsupportedOperationException("distanceToObject on identity type");
    }

    @Override
    public final boolean isA(Type other) {
        return this == other;
    }

    @Override
    Type apply(Unification unification) {
        return this;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public final boolean equals(Object obj) {
        return obj == this;
    }
}