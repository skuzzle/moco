package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.Position;

public class MonoType extends Type {

    protected MonoType(Identifier name, Position positionHint) {
        super(name, positionHint);
    }

    @Override
    public int distanceToObject() {
        throw new UnsupportedOperationException("distanceToObject on mono type");
    }

    @Override
    MonoType apply(Unification unification) {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof MonoType &&
                isA((MonoType) obj);
    }

    @Override
    public boolean isA(Type other) {
        return getName().equals(other.getName());
    }
}
