package de.uni.bremen.monty.typeinf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.declaration.ClassDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.CoreTypes;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.TypeVariable;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Unification;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;
import de.uni.bremen.monty.moco.util.astsearch.SearchAST;

public class ExplicitGenericsTest extends AbstractTypeInferenceTest {

    final ClassType Object = (ClassType) CoreTypes.get("Object");

    public ExplicitGenericsTest() {
        super("explicitGenerics.monty");
    }

    @Test
    public void testClassWithSimpleGenerics() throws Exception {
        final ASTNode root = getTypeCheckedAST();
        final ClassDeclaration decl = SearchAST.forNode(ClassDeclaration.class)
                .where(Predicates.hasName("Pair"))
                .in(root).get();

        final Type expected = ClassType.named("Pair").withSuperClass(this.Object)
                .addTypeParameter(TypeVariable.anonymous().createType())
                .addTypeParameter(TypeVariable.anonymous().createType())
                .createType();
        assertTrue(Unification.testIf(decl.getType()).isA(expected).isSuccessful());

    }

    @Test
    public void testClassWithInheritedInstantiation() throws Exception {
        final ASTNode root = getTypeCheckedAST();
        final ClassDeclaration decl = SearchAST.forNode(ClassDeclaration.class)
                .where(Predicates.hasName("Bc"))
                .in(root).get();

        final Type expected1 = ClassType.named("Pair").withSuperClass(this.Object)
                .addTypeParameter(TypeVariable.anonymous().createType())
                .addTypeParameter(TypeVariable.anonymous().createType())
                .createType();

        final Type expected2 = ClassType.named("Pair").withSuperClass(this.Object)
                .addTypeParameter(CoreTypes.get("Char"), CoreTypes.get("String"))
                .createType();

        assertTrue(Unification.testIf(decl.getType()).isA(expected1).isSuccessful());
        assertTrue(Unification.testIf(decl.getType()).isA(expected2).isSuccessful());
    }

    @Test
    public void testClassWithInheritedRecursiveInstantiation() throws Exception {
        final ASTNode root = getTypeCheckedAST();
        final ClassDeclaration decl = SearchAST.forNode(ClassDeclaration.class)
                .where(Predicates.hasName("Recursive"))
                .in(root).get();

        final Type expected1 = ClassType.named("Recursive").withSuperClass(this.Object)
                .addTypeParameter(TypeVariable.anonymous().createType())
                .createType();

        final Type expected2 = ClassType.named("Pair").withSuperClass(this.Object)
                .addTypeParameter(TypeVariable.named("A").createType())
                .addTypeParameter(CoreTypes.get("String"))
                .createType();

        assertTrue(Unification.testIf(decl.getType()).isA(expected1).isSuccessful());
        assertTrue(Unification.testIf(decl.getType()).isA(expected2).isSuccessful());
    }

    @Test
    public void testGenericDeclarationWithAssignment() throws Exception {
        final ASTNode root = getTypeCheckedAST();
        final VariableDeclaration decl = SearchAST.forNode(VariableDeclaration.class)
                .where(Predicates.hasName("pair"))
                .in(root).get();

        final FunctionCall ctor = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.onLine(2))
                .in(root).get();

        final Type expected = ClassType.named("Pair")
                .withSuperClass(this.Object)
                .addTypeParameter(CoreTypes.get("Int"), CoreTypes.get("String"))
                .createType();

        assertEquals(expected, ctor.getType());
        assertEquals(expected, decl.getType());
    }
}
