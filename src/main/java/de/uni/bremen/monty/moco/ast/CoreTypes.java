package de.uni.bremen.monty.moco.ast;

import java.util.HashMap;
import java.util.Map;

import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.IdentityType;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.exception.UnknownIdentifierException;

public final class CoreTypes {

    private CoreTypes() {}

    public static final Type TOP = new IdentityType("$top");

    public static final Type BOT = new IdentityType("$bottom");

    static final Type VOID = new IdentityType("$void");

    private static final Map<String, Type> CORE_TYPES;
    static {
        CORE_TYPES = new HashMap<>();
        final String[] coreTypes = { "Char", "Int", "Float", "Bool" };
        final ClassType object = ClassType.classNamed("Object").createType();
        CORE_TYPES.put("Object", object);
        CORE_TYPES.put("String", ClassType.classNamed("String").createType());
        CORE_TYPES.put("Array", ClassType.classNamed("Array").createType());
        CORE_TYPES.put("$void", VOID);
        for (final String typeName : coreTypes) {
            CORE_TYPES.put(typeName, ClassType
                    .classNamed(typeName)
                    .withSuperClass(object)
                    .createType());
        }
    }

    static Type get(Identifier name) {
        return get(name.getSymbol());
    }

    static Type get(String name) {
        final Type result = CORE_TYPES.get(name);
        if (result == null) {
            throw new UnknownIdentifierException(Type.UNKNOWN_LOCATION,
                    new ResolvableIdentifier(name));
        }
        return result;
    }

}
