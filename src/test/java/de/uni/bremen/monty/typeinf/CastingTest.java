package de.uni.bremen.monty.typeinf;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.declaration.ClassDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.util.Monty;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;

public class CastingTest extends AbstractTypeInferenceTest {

    @Test
    @Monty(
    "class Foo<X>:\n" +
    "     pass\n" +
    "Object a := Foo<String>()\n" +
    "Foo<String> b := a as Foo<String>"
    )
    public void testCastToGeneric() throws Exception {
        compile();
    }

    @Test
    @Monty(
    "Int a := \"foo\" as Int"
    )
    public void testImpossibleCast() throws Exception {
        typeCheckAndExpectFailure("Impossible cast");
    }

    @Test
    @Monty(
    "Bool x := (5 is Bool)"
    )
    public void testImpossibleInstance() throws Exception {
        typeCheckAndExpectFailure("Impossible cast");
    }

    @Test
    @Monty(
    "class Foo<X>:\n" +
    "     pass\n" +
    "Object a := Foo<String>()\n" +
    "? xyz := a as Foo<String>"
    )
    public void testInferCastToGeneric() throws Exception {
        this.compile();
        final VariableDeclaration xyz = this.compiler.searchFor(VariableDeclaration.class,
                Predicates.hasName("xyz"));

        final ClassDeclaration decl = this.compiler.searchFor(ClassDeclaration.class,
                Predicates.hasName("Foo"));

        final Type expected = ClassType.classNamed("Foo")
                .withSuperClass(CoreClasses.objectType().getType().asClass())
                .addTypeParameter(CoreClasses.stringType().getType())
                .createType();

        assertUniqueTypeIs(expected, xyz);
        assertEquals(decl, xyz.getTypeDeclaration());
    }
}
