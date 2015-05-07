package de.uni.bremen.monty.typeinf;

import org.junit.Test;

import de.uni.bremen.monty.moco.util.Monty;

public class FunctionCallTest extends AbstractTypeInferenceTest {

    @Test
    @Monty(
    "Int a := method(\"foo\", 5)\n"+
    "method(1337)\n" +
    "Int method(String a, Int b):\n" +
    "    return 5\n" +
    "method(Int a):\n" +
    "    pass"
    )
    public void testOverloadWithPrimitives() throws Exception {
        this.compiler.compile();
        this.compiler.assertAllTypesResolved();
    }

    @Test
    @Monty(
    "Int a := method(\"foo\", 5)\n"+
    "Int method(String a, Int b):\n" +
    "    return 5\n" +
    "method(String a, Int b):\n" +
    "    pass")
    public void testDuplicateSignature() throws Exception {
        typeCheckAndExpectFailure("Ambiguous");
    }

    @Test
    @Monty(
    "A a := method(A())\n"+
    "B b := method(B())\n"+
    "class A:\n"+
    "    pass\n" +
    "class B inherits A:\n" +
    "    pass\n" +
    "A method(A a):\n" +
    "    return a\n" +
    "B method(B b):\n" +
    "    return b"
    )
    public void testBestFit() throws Exception {
        this.compiler.compile();
        this.compiler.assertAllTypesResolved();
    }

    @Test
    @Monty(
    "A a := method(A())\n"+
    "class A:\n"+
    "    pass\n" +
    "class B inherits A:\n" +
    "    pass\n" +
    "A method(A a):\n" +
    "    return a\n" +
    "<X> Int method(X x):\n" +
    "    return 5"
    )
    public void testBestFitGenerics() throws Exception {
        this.compiler.compile();
        this.compiler.assertAllTypesResolved();
    }

    @Test
    @Monty(
    "Int a := method<B>(B())\n"+
    "class A:\n"+
    "    pass\n" +
    "class B inherits A:\n" +
    "    pass\n" +
    "A method(A a):\n" +
    "    return a\n" +
    "<X> Int method(X x):\n" +
    "    return 5"
    )
    public void testBestFitExplicitGenerics() throws Exception {
        this.compiler.compile();
        this.compiler.assertAllTypesResolved();
    }

    @Test
    @Monty(
    "Int a := method<B>(B())\n"+
    "class A:\n"+
    "    pass\n" +
    "class B inherits A:\n" +
    "    pass\n" +
    "<Y> String method(Y y):\n" +
    "    return \"\"\n" +
    "<X> Int method(X x):\n" +
    "    return 5")
    public void testBestFit2GenericMethods() throws Exception {
        typeCheckAndExpectFailure("Ambiguous");
    }

    @Test
    @Monty(
    "method():\n" +
    "    method()"
    )
    public void testRecursiveProcedure() throws Exception {
        this.compiler.compile();
        this.compiler.assertAllTypesResolved();
    }
}
