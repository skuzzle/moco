package de.uni.bremen.monty.typeinf;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;
import de.uni.bremen.monty.moco.ast.expression.VariableAccess;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;
import de.uni.bremen.monty.moco.util.astsearch.SearchAST;
import de.uni.bremen.monty.moco.visitor.typeinf.TypeInferenceException;

public class ImplicitGenericsTest extends AbstractTypeInferenceTest {

    @Test
    public void testStaticGenericFunction() throws Exception {
        final ASTNode root = getASTFromString("testStaticGenericFunction_implicit.monty",
                code -> code
                        .append("? x := identity(\"5\")")
                        .append("<A> A identity(A a):").indent()
                        .append("return a"));

        final VariableDeclaration x = SearchAST.forNode(VariableDeclaration.class)
                .where(Predicates.hasName("x"))
                .in(root)
                .get();

        assertUniqueTypeIs(CoreClasses.stringType().getType(), x);
        assertAllTypesResolved(root);
    }

    @Test
    public void testStaticGenericRecursiveFunction() throws Exception {
        final ASTNode root = getASTFromString("testStaticGenericRecursiveFunction_implicit.monty",
                code -> code
                        .append("? x := factorial(5, \"5\")")
                        .append("<A> Int factorial(Int n, A a):").indent()
                        .append("if (n<=1):").indent()
                        .append("return 1").dedent()
                        .append("else:").indent()
                        .append("return n * factorial<A>(n - 1, a)"));

        final VariableDeclaration x = SearchAST.forNode(VariableDeclaration.class)
                .where(Predicates.hasName("x"))
                .in(root)
                .get();

        assertUniqueTypeIs(CoreClasses.intType().getType(), x);
        assertAllTypesResolved(root);
    }

    @Test(expected = TypeInferenceException.class)
    public void testCallWithExplicitWrongParameter1() throws Exception {
        getASTFromString("testCallWithExplicitWrongParameter1_implicit.monty",
                code -> code
                        .append("Foo<String> a := Foo(5)") // type mismatch
                        .append("class Foo<X>:").indent()
                        .append("+X x")
                        .append("+initializer(X x):").indent()
                        .append("pass"));
    }

    @Test(expected = TypeInferenceException.class)
    public void testCallWithExplicitWrongParameter2() throws Exception {
        getASTFromString("testCallWithExplicitWrongParameter2_implicit.monty",
                code -> code
                        .append("? a := Foo<String>(5)") // type mismatch
                        .append("class Foo<X>:").indent()
                        .append("+X x")
                        .append("+initializer(X x):").indent()
                        .append("pass"));
    }

    @Test
    public void testGenericDeclarationWithAssignment() throws Exception {
        final ASTNode root = getASTFromString("testGenericDeclarationWithAssignment_implicit.monty",
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
                        .append("? pair := Pair(2, \"5\")"));

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
        assertAllTypesResolved(root);
    }

    @Test
    public void testAssignAttribute() throws Exception {
        final ASTNode root = getASTFromString("testAssignAttribute_implicit.monty",
                code -> code
                        .append("? a := Foo<String>()")
                        .append("? temp := Foo<String>()")
                        .append("a.x := \"b\"")
                        .append("temp.x := a.x")
                        .append("class Foo<X>:").indent()
                        .append("+X x"));

        final VariableDeclaration a = SearchAST.forNode(VariableDeclaration.class)
                .where(Predicates.hasName("a")).in(root).get();
        final VariableDeclaration temp = SearchAST.forNode(VariableDeclaration.class)
                .where(Predicates.hasName("temp"))
                .in(root).get();

        final Type expected = ClassType.classNamed("Foo")
                .addTypeParameter(CoreClasses.stringType().getType())
                .withSuperClass(CoreClasses.objectType().getType().asClass())
                .createType();

        assertUniqueTypeIs(expected, a);
        assertUniqueTypeIs(expected, temp);
        assertAllTypesResolved(root);
    }

    @Test
    public void testReturnGeneric() throws Exception {
        final ASTNode root = getASTFromString("testReturnGeneric_implicit.monty",
                code -> code
                        .append("class Pair<A, B>:").indent()
                        .append("-A t1")
                        .append("-B t2")
                        .blankLine()
                        .append("+initializer(A f, B s):").indent()
                        .append("t1 := f")
                        .append("t2 := s")
                        .dedent()
                        .dedent()
                        .append("? foo(Int a, Int b):").indent()
                        .append("return Pair(a, b)")
                        .dedent()
                        .append("test():").indent()
                        .append("? p := foo(1, 2)")
                        .dedent());

        final FunctionCall call = searchFor(FunctionCall.class)
                .where(Predicates.hasName("foo"))
                .in(root).get();

        final VariableDeclaration p = searchFor(VariableDeclaration.class)
                .where(Predicates.hasName("p"))
                .in(root).get();

        final Type expected = ClassType.classNamed("Pair")
                .addTypeParameter(CoreClasses.intType().getType())
                .addTypeParameter(CoreClasses.intType().getType())
                .withSuperClass(CoreClasses.objectType().getType().asClass())
                .createType();

        assertUniqueTypeIs(expected, call);
        assertUniqueTypeIs(expected, p);
        assertAllTypesResolved(root);
    }

    @Test(expected = TypeInferenceException.class)
    public void testWrongGenericParameter() throws Exception {
        getASTFromString("testWrongGenericParameter_implicit.monty",
                code -> code
                        .append("main():").indent()
                        .append("Foo<Int> myfoo := Foo(5)")
                        .append("? a := myfoo.bar(\"a\")")
                        .dedent()
                        .append("class Foo<X>:").indent()
                        .append("+initializer(X x):").indent()
                        .append("pass")
                        .dedent()
                        .blankLine()
                        .append("+Int bar(X x):").indent()
                        .append("return 2"));
    }

    @Test
    public void testRecursiveType() throws Exception {
        final ASTNode root = getASTFromString("testRecursiveType_implicit.monty",
                code -> code
                        .append("? root := Node(\"a\")")
                        .append("? child := Node(\"b\")")
                        .append("root.next := child")
                        .append("class Node<A>:").indent()
                        .append("-A data")
                        .append("+Node<A> next")
                        .append("+initializer(A data):").indent()
                        .append("self.data := data"));

        final VariableDeclaration child = searchFor(VariableDeclaration.class)
                .where(Predicates.hasName("child"))
                .in(root).get();

        final VariableAccess var = searchFor(VariableAccess.class)
                .where(Predicates.hasName("next"))
                .and(Predicates.onLine(3))
                .in(root).get();
        final Type expected = ClassType.classNamed("Node")
                .withSuperClass(CoreClasses.objectType().getType().asClass())
                .addTypeParameter(CoreClasses.stringType().getType())
                .createType();

        assertUniqueTypeIs(expected, var);
        assertUniqueTypeIs(expected, child);
        assertAllTypesResolved(root);
    }

    @Test(expected = TypeInferenceException.class)
    public void testRecursiveTypeFail() throws Exception {
        final ASTNode root = getASTFromString("testRecursiveTypeFail_implicit.monty",
                code -> code
                        .append("? root := Node(\"a\")")
                        .append("? child := Node(4)")
                        .append("root.next := child")
                        .append("class Node<A>:").indent()
                        .append("-A data")
                        .append("+Node<A> next")
                        .append("+initializer(A data):").indent()
                        .append("self.data := data"));
        assertAllTypesResolved(root);
    }
}
