package de.uni.bremen.monty.typeinf;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.util.TestResource;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;

public class FactorialTest extends AbstractTypeInferenceTest {
    @Test
    @TestResource("factorial.monty")
    public void testInferCallType() throws Exception {
        this.compiler.compile();

        final VariableDeclaration decl = this.compiler.searchFor(VariableDeclaration.class,
                Predicates.hasName("x"));
        assertUniqueTypeIs(CoreClasses.intType().getType(), decl);
        this.compiler.assertAllTypesResolved();
    }

    @Test
    @TestResource("factorial.monty")
    public void testExplicitTargetType() throws Exception {
        this.compiler.compile();
        final VariableDeclaration decl = this.compiler.searchFor(VariableDeclaration.class,
                Predicates.hasName("y"));
        assertUniqueTypeIs(CoreClasses.intType().getType(), decl);
        this.compiler.assertAllTypesResolved();
    }

    @Test
    @TestResource("factorial.monty")
    public void testInferReturnType() throws Exception {
        this.compiler.compile();
        final FunctionDeclaration decl = this.compiler.searchFor(FunctionDeclaration.class,
                Predicates.hasName("fak"));

        final Function expected = Function.named("fak")
                .returning(CoreClasses.intType().getType())
                .andParameter(CoreClasses.intType().getType())
                .createType();

        assertUniqueTypeIs(expected, decl);
        assertEquals(expected.getReturnType(), decl.getType().asFunction().getReturnType());
    }
}
