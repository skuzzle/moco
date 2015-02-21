package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import java.util.HashMap;
import java.util.Map;

import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.ResolvableIdentifier;
import de.uni.bremen.monty.moco.exception.UnknownIdentifierException;

public final class CoreTypes {

    private CoreTypes() {}

    private static class IdentityType extends Type {

        protected IdentityType(String name) {
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

    public static final Type TOP = new IdentityType("$top");

    public static final Type BOT = new IdentityType("$bottom");

    public static final Type VOID = new IdentityType("$void");

    private static final Map<String, Type> CORE_TYPES;
    static {
        CORE_TYPES = new HashMap<>();
        final String[] coreTypes = { "Char", "String", "Int", "Float", "Bool", "Array" };
        final ClassType object = ClassType.classNamed("Object").createType();
        CORE_TYPES.put("Object", object);
        CORE_TYPES.put("__void", VOID);
        for (final String typeName : coreTypes) {
            CORE_TYPES.put(typeName, ClassType
                    .classNamed(typeName)
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
