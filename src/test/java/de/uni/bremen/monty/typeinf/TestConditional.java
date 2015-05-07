package de.uni.bremen.monty.typeinf;

import org.junit.Test;

import de.uni.bremen.monty.moco.util.Monty;

public class TestConditional extends AbstractTypeInferenceTest {

    @Test
    @Monty(
    "Bool a := true\n" +
    "String c := \"a\" if a else \"b\""
    )
    public void testConditionalExpressionSuccess() throws Exception {
        this.compiler.compile();
        this.compiler.assertAllTypesResolved();
    }
}
