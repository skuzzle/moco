package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import de.uni.bremen.monty.moco.ast.Identifier;

public class IdentityType extends Type {

    public IdentityType(String name) {
        super(Identifier.of(name), UNKNOWN_POSITION);
    }

    @Override
    public int distanceToObject() {
        throw new UnsupportedOperationException("distanceToObject on identity type");
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    @Override
    public boolean isA(Type other) {
        return this == other;
    }

    @Override
    Type apply(Unification unification) {
        return this;
    }
}