package de.uni.bremen.monty.typeinf;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.CoreTypes;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;

public class FactorialTest extends AbstractTypeInferenceTest {

    public FactorialTest() {
        super("factorial.monty");
    }

    @Test
    public void testInferCallType() throws Exception {
        final ASTNode root = getTypeCheckedAST();
        final VariableDeclaration decl = searchFor(VariableDeclaration.class)
                .and(Predicates.hasName("x"))
                .in(root)
                .get();

        assertUniqueTypeIs(CoreTypes.get("Int"), decl);
    }

    @Test
    public void testExplicitTargetType() throws Exception {
        final ASTNode root = getTypeCheckedAST();
        final VariableDeclaration decl = searchFor(VariableDeclaration.class)
                .and(Predicates.hasName("y"))
                .in(root)
                .get();

        assertUniqueTypeIs(CoreTypes.get("Int"), decl);
    }

    @Test
    public void testInferReturnType() throws Exception {
        final ASTNode root = getTypeCheckedAST();
        final FunctionDeclaration decl = searchFor(FunctionDeclaration.class)
                .and(Predicates.hasName("fak"))
                .in(root)
                .get();

        final Function expected = Function.named("fak")
                .returning(CoreTypes.get("Int"))
                .andParameter(CoreTypes.get("Int"))
                .createType();

        assertUniqueTypeIs(expected, decl);
        assertEquals(expected.getReturnType(), decl.getReturnType());
    }
}
