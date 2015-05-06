package de.uni.bremen.monty.typeinf;

import org.junit.Rule;
import org.junit.Test;

import de.uni.bremen.monty.moco.test.util.CompileRule;
import de.uni.bremen.monty.moco.test.util.Monty;

public class TestConditional extends AbstractTypeInferenceTest {

    @Rule
    public final CompileRule compiler = new CompileRule();

    @Test
    @Monty(
    "Bool a := true\n" +
    "String c := \"a\" if a else \"b\""
    )
    public void testConditionalExpressionSuccess() throws Exception {
        assertAllTypesResolved(this.compiler.getAst());
    }
}
