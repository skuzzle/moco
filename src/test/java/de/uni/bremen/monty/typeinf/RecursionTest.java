package de.uni.bremen.monty.typeinf;

import org.junit.Test;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;

public class RecursionTest extends AbstractTypeInferenceTest {

    @Test
    public void testRecursionExplicitReturnType() throws Exception {
        final ASTNode root = getASTFromString("testRecursionExplicitReturnType.monty",
                code -> code.append("Int fak(Int n):").indent()
                        .append("if (n < 2):").indent()
                        .append("return 1")
                        .dedent()
                        .append("else:").indent()
                        .append("return fak(n - 1)"));
        final FunctionDeclaration decl = searchFor(FunctionDeclaration.class)
                .where(Predicates.hasName("fak"))
                .in(root).get();

        final Type expected = Function
                .named("fak")
                .returning(CoreClasses.intType().getType())
                .andParameter(CoreClasses.intType().getType())
                .createType();

        assertUniqueTypeIs(expected, decl);
    }
}
