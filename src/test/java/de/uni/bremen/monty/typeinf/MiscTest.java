package de.uni.bremen.monty.typeinf;

import org.junit.Test;

import de.uni.bremen.monty.moco.util.Debug;
import de.uni.bremen.monty.moco.util.ExpectOutput;
import de.uni.bremen.monty.moco.util.Monty;


public class MiscTest extends AbstractTypeInferenceTest {

    @Test
    @Monty(
    "Int a := foo\n" +
    "foo():\n" +
    "    pass"
    )
    public void testVarAccessNoVar() throws Exception {
        this.exception.expectMessage("Identifier is not defined: foo");
        this.compiler.typeCheck();
    }

    @Test
    @Monty(
    "foo := 1\n" +
    "foo():\n" +
    "    pass"
    )
    public void testAssignToNonVar() throws Exception {
        this.exception.expectMessage("Identifier is not defined: foo");
        this.compiler.typeCheck();
    }

    @Test
    @Monty(
    "print(\"a\")\n"+
    "print(foo(true))\n" +
    "print(foo(false))\n" +
    "? foo(Bool cond):\n" +
    "    ? result := \"b\"\n"+
    "    if cond:\n" +
    "        result := \"a\"\n" +
    "    else:\n" +
    "        result := \"b\"\n" +
    "    return result"
    )
    @ExpectOutput("aab")
    @Debug
    public void testIfStatement() throws Exception {
        this.compiler.compile();
    }

    @Test
    @Monty(
    "print(\"a\")"
    )
    @ExpectOutput("a")
    public void testPrint() throws Exception {
        this.compiler.compile();
    }
}
