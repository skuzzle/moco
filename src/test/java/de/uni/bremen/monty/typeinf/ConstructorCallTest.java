package de.uni.bremen.monty.typeinf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;
import de.uni.bremen.monty.moco.visitor.typeinf.TypeInferenceException;

public class ConstructorCallTest extends AbstractTypeInferenceTest {

    @Test
    public void testAssignCtorCallToDeclaration() throws Exception {
        final ASTNode root = getASTFromString("testAssignCtorCallToDeclaration.monty",
                code -> code
                        .append("class Circle:").indent()
                        .append("pass")
                        .dedent()
                        .append("test():").indent()
                        .append("? var := Circle()"));

        final VariableDeclaration decl = searchFor(VariableDeclaration.class)
                .where(Predicates.hasName("var"))
                .in(root).get();

        final FunctionCall call = searchFor(FunctionCall.class)
                .where(Predicates.hasName("Circle"))
                .in(root).get();

        final ClassType object = CoreClasses.objectType().getType().asClass();
        final Type circle = ClassType.classNamed("Circle").withSuperClass(object).createType();
        final Type expectedCallDeclType = Function.named("initializer")
                .returning(circle)
                .createType();

        assertTrue(call.isConstructorCall());
        assertEquals(circle, call.getType());
        assertEquals(expectedCallDeclType, call.getDeclaration().getType());
        assertEquals(circle, decl.getType());
    }

    @Test(expected = TypeInferenceException.class)
    public void testConstructorWithExplicitTypeArg() throws Exception {
        // c'tor can not specify a type arg named the same as in surrounding
        // class
        getASTFromString("testConstructorWithExplicitTypeArg.monty",
                code -> code
                        .append("class Foo<X>:").indent()
                        .append("+<X> initializer(X x):").indent()
                        .append("pass"));
    }
}
