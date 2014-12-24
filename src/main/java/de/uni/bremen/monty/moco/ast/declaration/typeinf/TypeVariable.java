package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.Position;

public class TypeVariable extends Type {

    public static TypeVariable createType(String name, Position position) {
        return new TypeVariable(new Identifier(name), position);
    }

    public static TypeVariable createAnonymous(Position position) {
        return new TypeVariable(new Identifier(PREFIX + String.valueOf(counter++)),
                position);
    }

    private static int counter;
    private static final String PREFIX = "$VAR_";

    private TypeVariable(Identifier name, Position positionHint) {
        super(name, positionHint);
    }

    @Override
    public boolean isVariable() {
        return true;
    }

    @Override
    Type apply(Unification unification) {
        return unification.getSubstitute(this);
    }
}
