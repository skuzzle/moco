package de.uni.bremen.monty.typeinf;

import org.junit.Ignore;
import org.junit.Test;

import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.util.Debug;
import de.uni.bremen.monty.moco.util.Monty;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;
il.astsearch.Predicates;

public class RecursionTest extends AbstractTypeInferenceTest {

    @Test
    @Monty(
    "Int fak(Int n):\n" +
    "    if (n < 2):\n" +
    "        return 1\n" +
    "    else:\n" +
    "        return fak(n - 1)"
    )
    public void testRecursionExplicitReturnType() throws Exception {
        this.compiler.compile();
        final FunctionDeclaration decl = this.compiler.searchFor(FunctionDeclaration.class,
                Predicates.hasName("fak"));

        final Type expected = Function
                .named("fak")
                .returning(CoreClasses.intType().getType())
                .andParameter(CoreClasses.intType().getType())
                .createType();

        assertUniqueTypeIs(expected, decl);
    }

    @Test
    @Monty(
    "? fib(Int n):\n" +
    "    if (n < 3):\n" +
    "        return n\n" +
    "    else:\n"+
    "        ? res1 := fib(n - 1)\n" +
    "        ? res2 := fib(n - 2)\n" +
    "        return res1 + res2"
    )
    @Debug
    @Ignore
    public void testInferReturnTypeSimpleRecursion() throws Exception {
        this.compiler.compile();
    }
}
