package de.uni.bremen.monty.typeinf;

import org.junit.Test;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;
import de.uni.bremen.monty.moco.util.astsearch.SearchAST;

public class ExplicitRecursionTest extends AbstractTypeInferenceTest {

    @Test
    public void testExplicitRecursiveFunction() throws Exception {
        final ASTNode root = getASTFromString("testExplicitRecursiveFunction.monty",
                code -> code
                        .append("Int fact(Int n):").indent()
                        .append("if (n < 2):").indent()
                        .append("return 1")
                        .dedent()
                        .append("else:").indent()
                        .append("return n * fact(n - 1)"));

        final FunctionDeclaration fun = SearchAST.forNode(FunctionDeclaration.class)
                .where(Predicates.hasName("fact"))
                .in(root).get();
        final Function expected = Function.named("fact")
                .atLocation(fun)
                .returning(CoreClasses.intType().getType())
                .andParameter(CoreClasses.intType().getType())
                .createType();
        assertUniqueTypeIs(expected, fun);
    }
}
