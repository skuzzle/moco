package de.uni.bremen.monty.typeinf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;
import de.uni.bremen.monty.moco.util.Monty;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;

public class ConstructorCallTest extends AbstractTypeInferenceTest {

    @Test
    @Monty(
    "class Circle:\n" +
    "    pass\n" +
    "test():\n" +
    "    ? var := Circle()"
    )
    public void testAssignCtorCallToDeclaration() throws Exception {
        this.compile();

        final VariableDeclaration decl = this.compiler.searchFor(
                VariableDeclaration.class, Predicates.hasName("var"));

        final FunctionCall call = this.compiler.searchFor(FunctionCall.class,
                Predicates.hasName("Circle"));

        final ClassType object = CoreClasses.objectType().getType().asClass();
        final Type circle = ClassType.classNamed("Circle").withSuperClass(object).createType();
        final Type expectedCallDeclType = Function.named("initializer")
                .returning(circle)
                .createType();

        assertTrue(call.isConstructorCall());
        assertEquals(circle, call.getType());
        assertEquals(expectedCallDeclType, call.getDeclaration().getType());
        assertEquals(circle, decl.getType());
    }

    @Test
    @Monty(
    "class A:\n" +
    "    +initializer():\n" +
    "        pass\n" +
    "class B inherits A:\n" +
    "    +initializer():\n" +
    "        pass\n" +
    "A a := A()\n" +
    "A b := B()"
    )
    public void testAssignSubTypeConstructor() throws Exception {
        this.compile();
    }

    @Test
    @Monty(
    "class Foo<X>:\n" +
    "    +initializer():\n" +
    "        return 5"
    )
    public void testConstructorReturnsValue() throws Exception {
        typeCheckAndExpectFailure("must not return a value");
    }

    @Test
    @Monty(
    "class Foo<X>:\n" +
    "    +<X> initializer(X x):\n" +
    "        pass"
    )
    public void testConstructorWithExplicitTypeArg() throws Exception {
        typeCheckAndExpectFailure("can not redeclare generic");
    }
}
