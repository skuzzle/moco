package de.uni.bremen.monty.typeinf;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.util.TestResource;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;

public class ShapesTest extends AbstractTypeInferenceTest {

    private ClassType object;
    private ClassType circle;
    private ClassType shape;
    private ClassType rectangle;

    @Before
    public void setup() {
        this.object = CoreClasses.objectType().getType().asClass();
        this.shape = ClassType.classNamed("Shape").withSuperClass(this.object).createType();
        this.circle = ClassType.classNamed("Circle").withSuperClass(this.shape).createType();
        this.rectangle = ClassType.classNamed("Rectangle").withSuperClass(this.shape).createType();
    }

    @Test
    @TestResource("shapes.monty")
    public void testConstructorAssignments() throws Exception {
        this.compiler.compile();
        final VariableDeclaration s1 = this.compiler.searchFor(VariableDeclaration.class,
                Predicates.hasName("s1"));
        final VariableDeclaration s2 = this.compiler.searchFor(VariableDeclaration.class,
                Predicates.hasName("s2"));

        assertUniqueTypeIs(this.circle, s1);
        assertUniqueTypeIs(this.rectangle, s2);
    }

    @Test
    @TestResource("shapes.monty")
    public void testInferEnclosingRect1() throws Exception {
        this.compiler.compile();
        final FunctionDeclaration decl = this.compiler.searchFor(FunctionDeclaration.class,
                Predicates.hasParameters(this.shape, this.shape));

        final Function expected = Function.named("enclosingRect")
                .returning(this.shape)
                .andParameters(this.shape, this.shape)
                .createType();

        assertUniqueTypeIs(expected, decl);
        assertEquals(expected.getReturnType(), decl.getType().asFunction().getReturnType());
    }

    @Test
    @TestResource("shapes.monty")
    public void testInferEnclosingRect2() throws Exception {
        this.compiler.compile();
        final FunctionDeclaration decl = this.compiler.searchFor(
                FunctionDeclaration.class,
                Predicates.hasParameters(this.circle, this.shape));

        final Function expected = Function.named("enclosingRect")
                .returning(this.shape)
                .andParameters(this.circle, this.shape)
                .createType();

        assertUniqueTypeIs(expected, decl);
        assertEquals(expected.getReturnType(), decl.getType().asFunction().getReturnType());
    }
}
