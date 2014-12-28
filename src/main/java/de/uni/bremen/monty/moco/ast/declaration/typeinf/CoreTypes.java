package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import java.util.HashMap;
import java.util.Map;

import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.Position;
import de.uni.bremen.monty.moco.ast.ResolvableIdentifier;
import de.uni.bremen.monty.moco.exception.UnknownIdentifierException;

public final class CoreTypes {

    private CoreTypes() {}

    public static final Type VOID =
            new Type(new Identifier("$void"), new Position("unknown", 1, 1)) {

        @Override
        public boolean isVariable() {
            return false;
        }

        @Override
        public Type apply(Unification unification) {
                    return this;
        }

                @Override
                public boolean equals(Object obj) {
                    return obj == VOID;
                }
    };

    private static final Map<String, Type> CORE_TYPES;
    static {
        CORE_TYPES = new HashMap<>();
        final String[] coreTypes = { "Char", "String", "Int", "Float", "Bool", "Array" };
        final ClassType object = ClassType.named("Object").createType();
        CORE_TYPES.put("Object", object);
        CORE_TYPES.put("__void", VOID);
        for (final String typeName : coreTypes) {
            CORE_TYPES.put(typeName, ClassType
                    .named(typeName)
                    .withSuperClass(object)
                    .createType());
        }
    }

    public static Type get(Identifier name) {
        return get(name.getSymbol());
    }

    public static Type get(String name) {
        final Type result = CORE_TYPES.get(name);
        if (result == null) {
            throw new UnknownIdentifierException(Type.UNKNOWN_LOCATION,
                    new ResolvableIdentifier(name));
        }
        return result;
    }

}
