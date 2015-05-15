package de.uni.bremen.monty.typeinf;

import org.junit.Test;

import de.uni.bremen.monty.moco.util.Monty;

public class OverrideTest extends AbstractTypeInferenceTest {

    @Test
    @Monty(
    "B b := B()\n" +
    "b.test(5)\n" +
    "class A:\n" +
    "    +test(Int a):\n" +
    "        print(\"a\")\n" +
    "class B inherits A:\n" +
    "    +test(Int b):\n" +
    "        print(\"b\")"
    )
    public void testOverrideProcedureDirectSubClass() throws Exception {
        this.compiler.compile();
        this.compiler.assertAllTypesResolved();
    }
}
