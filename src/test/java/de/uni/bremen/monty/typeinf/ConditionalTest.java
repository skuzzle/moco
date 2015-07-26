package de.uni.bremen.monty.typeinf;

import org.junit.Test;

import de.uni.bremen.monty.moco.util.ExpectOutput;
import de.uni.bremen.monty.moco.util.Monty;

public class ConditionalTest extends AbstractTypeInferenceTest {

    @Test
    @Monty(
    "Bool a := true\n" +
    "String c := \"a\" if a else \"b\"\n" +
    "print(c)"
    )
    public void testConditionalExpressionSuccess() throws Exception {
        this.compile();
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
        this.compile();
    }

    @Test
    @Monty(
    "<X, Y> X conditionalIdentity(X a, Y b):\n" +
    "    return a if true else b"
    )
    public void testConditionalNoCommonTypeVariable() throws Exception {
        typeCheckAndExpectFailure("Conditional branches type mismatch");
    }

    @Test
    @Monty(
    "Object b := true\n" +
    "if b is Bool:\n" +
    "    print(\"Ok\")"
    )
    @ExpectOutput("Ok")
    public void testIsAWithIf() throws Exception {
        this.compile();
    }

    @Test
    @Monty(
    "foo():\n" +
    "    if 1:\n" +
    "        return"
    )
    public void testConditionIsNoBoolean() throws Exception {
        typeCheckAndExpectFailure("Int is not a bool");
    }

    @Test
    @Monty(
    "foo():\n" +
    "    Int a := 1 if 2 else 3\n"
    )
    public void testConditionIsNoBoolean2() throws Exception {
        typeCheckAndExpectFailure("Int is not a bool");
    }

    @Test
    @Monty(
    "foo():\n" +
    "    while 1:\n" +
    "        return"
    )
    public void testConditionIsNoBooleanWhile() throws Exception {
        typeCheckAndExpectFailure("Int is not a bool");
    }

    @Test
    @Monty(
    "? i := 0\n" +
    "while i < 5:\n"+
    "    print(\"i\")\n"+
    "    i += 1\n"
    )
    @ExpectOutput("iiiii")
    public void testWhileLoop() throws Exception {
        this.compile();
    }
}
