package de.uni.bremen.monty.typeinf;

import org.junit.Test;

import de.uni.bremen.monty.moco.util.Monty;

public class MethodDeclarationTest extends AbstractTypeInferenceTest {

    @Test
    @Monty(
    "? foo():\n" +
    "    if true:\n" +
    "        return false\n" +
    "    else:\n" +
    "        return 1"
    )
    public void testNoCommonTypeFunction() throws Exception {
        typeCheckAndExpectFailure("Could not uniquely determine type of function's body");
    }

    @Test
    @Monty(
    "foo():\n" +
    "    if true:\n" +
    "        return false\n" +
    "    else:\n" +
    "        return 1"
    )
    public void testNoCommonTypeProcedure() throws Exception {
        typeCheckAndExpectFailure("Could not uniquely determine type of function's body");
    }

    @Test
    @Monty(
    "foo():\n" +
    "    if true:\n" +
    "        return 1\n" +
    "    else:\n" +
    "        return 1"
    )
    public void testProcedureNotAVoid() throws Exception {
        typeCheckAndExpectFailure("Could not uniquely determine type of function's body");
    }

    @Test
    @Monty(
    "Int foo():\n" +
    "    if true:\n" +
    "        return '1'\n" +
    "    else:\n" +
    "        return '1'"
    )
    public void testReturnTypeMismatch() throws Exception {
        typeCheckAndExpectFailure("Body type <Char> not compatible with return type <Int>");
    }
}
