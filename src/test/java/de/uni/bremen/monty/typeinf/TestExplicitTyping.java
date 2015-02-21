package de.uni.bremen.monty.typeinf;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;
import de.uni.bremen.monty.moco.util.astsearch.SearchAST;

public class TestExplicitTyping extends AbstractTypeInferenceTest {

    @Test
    public void testExplicitAttribute() throws Exception {
        final ASTNode root = getASTFromResource("explicit.monty");
        final VariableDeclaration decl = searchFor(VariableDeclaration.class)
                .where(Predicates.hasName("bar"))
                .in(root).get();
        assertUniqueTypeIs(CoreClasses.stringType().getType(), decl);
    }

    @Test
    public void testExplicitFunction() throws Exception {
        final ASTNode root = getASTFromResource("explicit.monty");
        final FunctionDeclaration decl = searchFor(FunctionDeclaration.class)
                .where(Predicates.hasName("add"))
                .in(root).get();
        final Function expected = Function.named("add")
                .returning(CoreClasses.intType().getType())
                .andParameters(CoreClasses.intType().getType())
                .andParameters(CoreClasses.intType().getType())
                .createType();
        assertUniqueTypeIs(expected, decl);
        assertEquals(expected.getReturnType(), decl.getType().asFunction().getReturnType());
    }

    @Test
    public void testExplicitFunctionParameter() throws Exception {
        final ASTNode root = getASTFromResource("explicit.monty");
        final VariableDeclaration decl = searchFor(VariableDeclaration.class)
                .where(Predicates.hasName("a"))
                .and(SearchAST.forParent(ProcedureDeclaration.class)
                        .where(Predicates.hasName("add")))
                .in(root).get();
        assertUniqueTypeIs(CoreClasses.intType().getType(), decl);
    }

    @Test
    public void testVoidMethod() throws Exception {
        final ASTNode root = getASTFromResource("explicit.monty");
        final ProcedureDeclaration decl = searchFor(ProcedureDeclaration.class)
                .where(Predicates.hasName("noop"))
                .in(root).get();

        final Function expected = Function.named("noop")
                .returningVoid()
                .andParameter(CoreClasses.stringType().getType())
                .createType();
        assertUniqueTypeIs(expected, decl);
        assertEquals(expected.getReturnType(), CoreClasses.voidType().getType());
    }

    @Test
    public void testExplicitProcedureParameter() throws Exception {
        final ASTNode root = getASTFromResource("explicit.monty");
        final VariableDeclaration decl = searchFor(VariableDeclaration.class)
                .where(Predicates.hasName("a"))
                .and(SearchAST.forExactParent(ProcedureDeclaration.class))
                .in(root).get();
        assertUniqueTypeIs(CoreClasses.stringType().getType(), decl);
    }
}
