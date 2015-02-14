package de.uni.bremen.monty.typeinf;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;

public class ShapesTest extends AbstractTypeInferenceTest {

    private ClassType circle;
    private ClassType shape;
    private ClassType rectangle;

    public ShapesTest() {
        super("shapes.monty");
    }

    @Before
    public void setup() {
        this.shape = ClassType.named("Shape").createType();
        this.circle = ClassType.named("Circle").withSuperClass(this.shape).createType();
        this.rectangle = ClassType.named("Rectangle").withSuperClass(this.shape).createType();
    }

    @Test
    public void testConstructorAssignments() throws Exception {
        final ASTNode root = getTypeCheckedAST();
        final VariableDeclaration s1 = searchFor(VariableDeclaration.class)
                .and(Predicates.hasName("s1"))
                .in(root).get();
        final VariableDeclaration s2 = searchFor(VariableDeclaration.class)
                .and(Predicates.hasName("s2"))
                .in(root).get();

        assertUniqueTypeIs(ClassType.named("Circle").createType(), s1);
        assertUniqueTypeIs(ClassType.named("Rectangle").createType(), s2);
    }

    @Test
    public void testInferEnclosingRect1() throws Exception {
        final ASTNode root = getTypeCheckedAST();
        final FunctionDeclaration decl = searchFor(FunctionDeclaration.class)
                .and(Predicates.hasParameters(this.shape, this.shape))
                .in(root).get();

        final Function expected = Function.named("enclosingRect")
                .returning(this.shape)
                .andParameters(this.shape, this.shape)
                .createType();

        assertUniqueTypeIs(expected, decl);
        assertEquals(expected.getReturnType(), decl.getReturnType());
    }

    @Test
    public void testInferEnclosingRect2() throws Exception {
        final ASTNode root = getTypeCheckedAST();
        final FunctionDeclaration decl = searchFor(FunctionDeclaration.class)
                .and(Predicates.hasParameters(this.circle, this.shape))
                .in(root).get();

        final Function expected = Function.named("enclosingRect")
                .returning(this.shape)
                .andParameters(this.circle, this.shape)
                .createType();

        assertUniqueTypeIs(expected, decl);
        assertEquals(expected.getReturnType(), decl.getReturnType());
    }
}
