package de.uni.bremen.monty.typeinf;

import org.junit.Test;

import de.uni.bremen.monty.moco.util.Monty;

public class ConditionalTest extends AbstractTypeInferenceTest {

    @Test
    @Monty(
    "Bool a := true\n" +
    "String c := \"a\" if a else \"b\"\n" +
    "print(c)"
    )
    public void testConditionalExpressionSuccess() throws Exception {
        this.compiler.compile();
        this.compiler.assertAllTypesResolved();
    }

    @Test
    @Monty(
    "Bool a := true\n" +
    "String c := \"a\" if a else 5"
    )
    public void testConditionalNoCommonType() throws Exception {
        typeCheckAndExpectFailure("Conditional branches type mismatch");
    }

    @Test
    @Monty(
    "<X> X conditionalIdentity(X a, X b):\n" +
    "    return a if true else b"
    )
    public void testMatchingGenericBranches() throws Exception {
        this.compiler.compile();
        this.compiler.assertAllTypesResolved();
    }

    @Test
    @Monty(
    "Object b := true\n" +
    "if b is Bool:\n" +
    "    print(\"Ok\")"
    )
    public void testIsAWithIf() throws Exception {
        this.compiler.compile();
    }
}
