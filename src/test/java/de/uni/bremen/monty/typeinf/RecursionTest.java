package de.uni.bremen.monty.typeinf;

import org.junit.Test;

import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.util.Monty;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;

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
}
