package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import de.uni.bremen.monty.moco.ast.Identifier;

public class IdentityType extends Type {

    public IdentityType(String name) {
        super(Identifier.of(name), UNKNOWN_POSITION);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    @Override
    Type apply(Unification unification) {
        return this;
    }
}