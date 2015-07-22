package de.uni.bremen.monty.typeinf;

import org.junit.Test;

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
}
