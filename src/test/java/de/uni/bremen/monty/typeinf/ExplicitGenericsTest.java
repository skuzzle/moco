package de.uni.bremen.monty.typeinf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.declaration.ClassDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeInstantiation;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.TypeVariable;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Unification;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;
import de.uni.bremen.monty.moco.util.astsearch.SearchAST;
import de.uni.bremen.monty.moco.visitor.typeinf.TypeInferenceException;

public class ExplicitGenericsTest extends AbstractTypeInferenceTest {

    @Test
    public void testClassWithSimpleGenerics() throws Exception {
        final ASTNode root = getASTFromString("testClassWithSimpleGenerics.monty",
                code -> code
                        .append("class Pair<A, B>:")
                        .indent().append("pass"));
        final ClassDeclaration decl = SearchAST.forNode(ClassDeclaration.class)
                .where(Predicates.hasName("Pair"))
                .in(root).get();

        final Type expected = ClassType.classNamed("Pair")
                .withSuperClass(CoreClasses.objectType().getType().asClass())
                .addTypeParameter(TypeVariable.anonymous().createType())
                .addTypeParameter(TypeVariable.anonymous().createType())
                .createType();
        assertTrue(Unification.testIf(decl.getType()).isA(expected).isSuccessful());

    }

    @Test
    public void testClassWithInheritedInstantiation() throws Exception {
        final ASTNode root = getASTFromString("testClassWithInheritedInstantiation.monty",
                code -> code
                        .append("class Pair<A, B>:")
                        .indent().append("pass").dedent().blankLine()
                        .append("class Bc inherits Pair<Char, String>:")
                        .indent().append("pass"));

        final ClassDeclaration decl = SearchAST.forNode(ClassDeclaration.class)
                .where(Predicates.hasName("Bc"))
                .in(root).get();

        final Type expected1 = ClassType.classNamed("Pair")
                .withSuperClass(CoreClasses.objectType().getType().asClass())
                .addTypeParameter(TypeVariable.anonymous().createType())
                .addTypeParameter(TypeVariable.anonymous().createType())
                .createType();

        final Type expected2 = ClassType.classNamed("Pair")
                .withSuperClass(CoreClasses.objectType().getType().asClass())
                .addTypeParameter(CoreClasses.charType().getType())
                .addTypeParameter(CoreClasses.stringType().getType())
                .createType();

        assertTrue(Unification.testIf(decl.getType()).isA(expected1).isSuccessful());
        assertTrue(Unification.testIf(decl.getType()).isA(expected2).isSuccessful());
    }

    @Test
    public void testClassWithInheritedRecursiveInstantiation() throws Exception {
        final ASTNode root = getASTFromString("testClassWithInheritedRecursiveInstantiation.monty",
                code -> code
                        .append("class Pair<A, B>:")
                        .indent().append("pass").dedent().blankLine()
                        .append("class Recursive<A> inherits Pair<A, String>:")
                        .indent().append("pass"));

        final ClassDeclaration decl = SearchAST.forNode(ClassDeclaration.class)
                .where(Predicates.hasName("Recursive"))
                .in(root).get();

        final TypeInstantiation superClassDecl = SearchAST.forNode(TypeInstantiation.class)
                .where(Predicates.hasName("Pair"))
                .in(root).get();

        final Type expectedTypeInst = ClassType.classNamed("Pair")
                .withSuperClass(CoreClasses.objectType().getType().asClass())
                .addTypeParameter(TypeVariable.named("A").createType())
                .addTypeParameter(CoreClasses.stringType().getType())
                .createType();

        assertTrue(Unification.testIf(superClassDecl.getType()).isA(expectedTypeInst).isSuccessful());
    }

    @Test
    public void testGenericDeclarationWithAssignment() throws Exception {
        final ASTNode root = getASTFromString("testGenericDeclarationWithAssignment.monty",
                code -> code
                        .append("class Pair<A, B>:").indent()
                        .append("-A t1")
                        .append("-B t2")
                        .blankLine()
                        .append("+initializer(A f, B s):").indent()
                        .append("t1 := f")
                        .append("t2 := s")
                        .dedent()
                        .append("+A get1():").indent()
                        .append("return t1")
                        .dedent()
                        .append("+B get2():").indent()
                        .append("return t2")
                        .dedent()
                        .dedent()
                        .append("foo():").indent()
                        .append("Pair<Int, String> pair := Pair(2, \"5\")"));

        final VariableDeclaration decl = SearchAST.forNode(VariableDeclaration.class)
                .where(Predicates.hasName("pair"))
                .in(root).get();

        final FunctionCall ctor = SearchAST.forNode(FunctionCall.class)
                .where(FunctionCall::isConstructorCall)
                .in(root).get();

        final Type expected = ClassType.classNamed("Pair")
                .withSuperClass(CoreClasses.objectType().getType().asClass())
                .addTypeParameter(CoreClasses.intType().getType())
                .addTypeParameter(CoreClasses.stringType().getType())
                .createType();

        assertEquals(expected, ctor.getType());
        assertEquals(expected, decl.getType());
    }

    @Test(expected = TypeInferenceException.class)
    public void testWrongGenericParameter() throws Exception {
        getASTFromString("testWrongGenericParameter.monty",
                code -> code
                        .append("main():").indent()
                        .append("Foo<Int> myfoo := Foo(5)")
                        .append("Int a := myfoo.bar(\"a\")")
                        .dedent()
                        .append("class Foo<X>:").indent()
                        .append("+initializer(X x):").indent()
                        .append("pass")
                        .dedent()
                        .blankLine()
                        .append("+Int bar(X x):").indent()
                        .append("return 2"));
    }

    @Test(expected = TypeInferenceException.class)
    public void testGenericMembers() throws Exception {
        getASTFromString("testGenericDeclarationWithAssignment.monty",
                code -> code
                        .append("class Pair<A, B>:").indent()
                        .append("-A t1")
                        .append("-B t2")
                        .blankLine()
                        .append("+A get1():").indent()
                        .append("return t2")); // type mismatch!
    }
}
