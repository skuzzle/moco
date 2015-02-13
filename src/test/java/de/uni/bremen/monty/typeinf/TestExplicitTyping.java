package de.uni.bremen.monty.typeinf;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.CoreTypes;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;
import de.uni.bremen.monty.moco.util.astsearch.SearchAST;

public class TestExplicitTyping extends AbstractTypeInferenceTest {

    public TestExplicitTyping() {
        super("explicit.monty");
    }

    @Test
    public void testExplicitAttribute() throws Exception {
        final ASTNode root = getTypeCheckedAST();
        final VariableDeclaration decl = searchFor(VariableDeclaration.class)
                .and(Predicates.hasName("bar"))
                .in(root).get();
        assertUniqueTypeIs(CoreTypes.get("String"), decl);
    }

    @Test
    public void testExplicitFunction() throws Exception {
        final ASTNode root = getTypeCheckedAST();
        final FunctionDeclaration decl = searchFor(FunctionDeclaration.class)
                .and(Predicates.hasName("add"))
                .in(root).get();
        final Function expected = Function.named("add")
                .returning(CoreTypes.get("Int"))
                .andParameters(CoreTypes.get("Int"), CoreTypes.get("Int"))
                .createType();
        assertUniqueTypeIs(expected, decl);
        assertEquals(expected.getReturnType(), decl.getReturnType());
    }

    @Test
    public void testExplicitFunctionParameter() throws Exception {
        final ASTNode root = getTypeCheckedAST();
        final VariableDeclaration decl = searchFor(VariableDeclaration.class)
                .and(Predicates.hasName("a"))
                .in(root).get();
        assertUniqueTypeIs(CoreTypes.get("Int"), decl);
    }

    @Test
    public void testVoidMethod() throws Exception {
        final ASTNode root = getTypeCheckedAST();
        final ProcedureDeclaration decl = searchFor(ProcedureDeclaration.class)
                .and(Predicates.hasName("noop"))
                .in(root).get();

        final Function expected = Function.named("noop")
                .returningVoid()
                .andParameter(CoreTypes.get("String"))
                .createType();
        assertUniqueTypeIs(expected, decl);
        assertEquals(expected.getReturnType(), CoreTypes.get("__void"));
    }

    @Test
    public void testExplicitProcedureParameter() throws Exception {
        final ASTNode root = getTypeCheckedAST();
        final VariableDeclaration decl = searchFor(VariableDeclaration.class)
                .and(Predicates.hasName("a"))
                .and(SearchAST.forExactParent(ProcedureDeclaration.class))
                .in(root).get();
        assertUniqueTypeIs(CoreTypes.get("String"), decl);
    }
}
