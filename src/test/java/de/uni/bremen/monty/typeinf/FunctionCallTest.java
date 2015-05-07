package de.uni.bremen.monty.typeinf;

import org.junit.Rule;
import org.junit.Test;

import de.uni.bremen.monty.moco.util.CompileRule;
import de.uni.bremen.monty.moco.util.Monty;
import de.uni.bremen.monty.moco.visitor.typeinf.TypeInferenceException;

public class FunctionCallTest extends AbstractTypeInferenceTest {

    @Rule
    public final CompileRule compiler = new CompileRule();

    @Test
    @Monty(
    "Int a := method(\"foo\", 5)\n"+
    "method(1337)\n" +
    "Int method(String a, Int b):\n" +
    "    return 5\n" +
    "method(Int a):\n" +
    "    pass"
    )
    public void testOverloadWithPrimitives() {
        assertAllTypesResolved(this.compiler.getAst());
    }

    @Test
    @Monty(value =
    "Int a := method(\"foo\", 5)\n"+
    "Int method(String a, Int b):\n" +
    "    return 5\n" +
    "method(String a, Int b):\n" +
    "    pass",
    expect = TypeInferenceException.class,
    matching = "Ambiguous"
    )
    public void testDuplicateSignature() throws Exception {
        // XXX: this error should already be recognized when duplicate method is declared
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
        assertAllTypesResolved(this.compiler.getAst());
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
        assertAllTypesResolved(this.compiler.getAst());
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
        assertAllTypesResolved(this.compiler.getAst());
    }

    @Test
    @Monty(value =
    "Int a := method<B>(B())\n"+
    "class A:\n"+
    "    pass\n" +
    "class B inherits A:\n" +
    "    pass\n" +
    "<Y> String method(Y y):\n" +
    "    return \"\"\n" +
    "<X> Int method(X x):\n" +
    "    return 5",
    expect = TypeInferenceException.class,
    matching = "Ambiguous")
    public void testBestFit2GenericMethods() throws Exception {}
}
