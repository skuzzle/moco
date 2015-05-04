package de.uni.bremen.monty.typeinf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.declaration.ClassDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeInstantiation;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration.DeclarationType;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.TypeVariable;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Unification;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;
import de.uni.bremen.monty.moco.ast.expression.MemberAccess;
import de.uni.bremen.monty.moco.ast.expression.VariableAccess;
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
        assertAllTypesResolved(root);

    }

    @Test
    public void testStaticGenericFunction() throws Exception {
        final ASTNode root = getASTFromString("testStaticGenericFunction.monty",
                code -> code
                        .append("? x := identity<String>(\"5\")")
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
        final ASTNode root = getASTFromString("testStaticGenericRecursiveFunction.monty",
                code -> code
                        .append("Int x := factorial<String>(5, \"5\")")
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
        assertAllTypesResolved(root);
    }

    @Test
    public void testAssignmentWithInheritedInstantiation() throws Exception {
        final ASTNode root = getASTFromString("testAssignmentWithInheritedInstantiation.monty",
                code -> code
                        .append("Pair<Char, String> test := Bc()")
                        .append("class Pair<A, B>:")
                        .indent().append("pass").dedent().blankLine()
                        .append("class Bc inherits Pair<Char, String>:")
                        .indent().append("pass"));

        assertAllTypesResolved(root);
    }

    @Test(expected = TypeInferenceException.class)
    public void testAssignmentWithInheritedInstantiationFail() throws Exception {
        getASTFromString("testAssignmentWithInheritedInstantiationFail.monty",
                code -> code
                        .append("Pair<Int, String> test := Bc()")
                        .append("class Pair<A, B>:")
                        .indent().append("pass").dedent().blankLine()
                        .append("class Bc inherits Pair<Char, String>:")
                        .indent().append("pass"));
    }

    @Test
    public void testAssignmentWithInheritedRecursiveInstantiation() throws Exception {
        final ASTNode root = getASTFromString("testAssignmentWithInheritedRecursiveInstantiation.monty",
                code -> code
                        .append("Pair<Int, String> test := Recursive<Int>()")
                        .append("class Pair<A, B>:")
                        .indent().append("pass").dedent().blankLine()
                        .append("class Recursive<A> inherits Pair<A, String>:")
                        .indent().append("pass"));

        assertAllTypesResolved(root);
    }

    @Test(expected = TypeInferenceException.class)
    public void testAssignmentWithInheritedRecursiveInstantiationFail() throws Exception {
        getASTFromString("testAssignmentWithInheritedRecursiveInstantiationFail.monty",
                code -> code
                        .append("Pair<Int, String> test := Recursive<String>()")
                        .append("class Pair<A, B>:")
                        .indent().append("pass").dedent().blankLine()
                        .append("class Recursive<A> inherits Pair<A, String>:")
                        .indent().append("pass"));
    }

    @Test
    public void testClassWithInheritedRecursiveInstantiation() throws Exception {
        final ASTNode root = getASTFromString("testClassWithInheritedRecursiveInstantiation.monty",
                code -> code
                        .append("class Pair<A, B>:")
                        .indent().append("pass").dedent().blankLine()
                        .append("class Recursive<A> inherits Pair<A, String>:")
                        .indent().append("pass"));

        final TypeInstantiation superClassDecl = SearchAST.forNode(TypeInstantiation.class)
                .where(Predicates.hasName("Pair"))
                .in(root).get();

        final Type expectedTypeInst = ClassType.classNamed("Pair")
                .withSuperClass(CoreClasses.objectType().getType().asClass())
                .addTypeParameter(TypeVariable.named("A").createType())
                .addTypeParameter(CoreClasses.stringType().getType())
                .createType();

        assertTrue(Unification.testIf(superClassDecl.getType()).isA(expectedTypeInst).isSuccessful());
        assertAllTypesResolved(root);
    }

    @Test
    public void testRecursiveType() throws Exception {
        final ASTNode root = getASTFromString("testRecursiveType.monty",
                code -> code
                        .append("Node<String> root := Node<String>(\"a\")")
                        .append("Node<String> child := Node<String>(\"b\")")
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
        getASTFromString("testRecursiveTypeFail.monty",
                code -> code
                        .append("Node<String> root := Node<String>(\"a\")")
                        .append("Node<Int> child := Node<Int>(4)")
                        .append("root.next := child")
                        .append("class Node<A>:").indent()
                        .append("-A data")
                        .append("+Node<A> next")
                        .append("+initializer(A data):").indent()
                        .append("self.data := data"));
    }

    @Test(expected = TypeInferenceException.class)
    public void testCallWithExplicitWrongParameter1() throws Exception {
        getASTFromString("testCallWithExplicitWrongParameter1.monty",
                code -> code
                        .append("Foo<String> a := Foo<String>(5)") // type
                                                                   // mismatch
                        .append("class Foo<X>:").indent()
                        .append("+X x")
                        .append("+initializer(X x):").indent()
                        .append("pass"));
    }

    @Test(expected = TypeInferenceException.class)
    public void testCallWithExplicitWrongParameter2() throws Exception {
        getASTFromString("testCallWithExplicitWrongParameter2.monty",
                code -> code
                        .append("Foo<Int> a := Foo<String>(5)") // type
                                                                   // mismatch
                        .append("class Foo<X>:").indent()
                        .append("+X x")
                        .append("+initializer(X x):").indent()
                        .append("pass"));
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
                        .append("Pair<Int, String> pair := Pair<Int, String>(2, \"5\")"));

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
        final ASTNode root = getASTFromString("testAssignAttribute.monty",
                code -> code
                        .append("Foo<String> a := Foo<String>()")
                        .append("Foo<String> temp := Foo<String>()")
                        .append("a.x := \"b\"")
                        .append("temp.x := a.x")
                        .append("class Foo<X>:").indent()
                        .append("+X x"));

        final VariableDeclaration a = SearchAST.forNode(VariableDeclaration.class)
                .where(Predicates.hasName("a")).in(root).get();
        final VariableDeclaration temp = SearchAST.forNode(VariableDeclaration.class)
                .where(Predicates.hasName("temp")).in(root).get();

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
        final ASTNode root = getASTFromString("testReturnGeneric.monty",
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
                        .append("Pair<Int, Int> foo(Int a, Int b):").indent()
                        .append("return Pair(a, b)")
                        .dedent()
                        .append("test():").indent()
                        .append("Pair<Int, Int> p := foo(1, 2)")
                        .dedent());

        final FunctionCall call = searchFor(FunctionCall.class)
                .where(Predicates.hasName("foo"))
                .in(root).get();

        final Type expected = ClassType.classNamed("Pair")
                .addTypeParameter(CoreClasses.intType().getType())
                .addTypeParameter(CoreClasses.intType().getType())
                .withSuperClass(CoreClasses.objectType().getType().asClass())
                .createType();

        assertUniqueTypeIs(expected, call);
        assertAllTypesResolved(root);
    }

    @Test(expected = TypeInferenceException.class)
    public void testAssignToTypeVarFail() throws Exception {
        getASTFromString("testAssignToTypeVarFail.monty",
                code -> code
                        .append("class Foo<X>:").indent()
                        .append("+X x")
                        .append("+initializer(X x):").indent()
                        .append("self.x := \"fail!\""));
    }

    @Test
    public void testGenericConstructor() throws Exception {
        final ASTNode root = getASTFromString("testGenericConstructor.monty",
                code -> code
                        .append("class Foo<X>:").indent()
                        .append("+X x")
                        .append("+initializer(X x):").indent()
                        .append("self.x := x"));
        assertAllTypesResolved(root);
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
    public void testGenericAssignmentFail() throws Exception {
        getASTFromString("testWrongGenericParameter.monty",
                code -> code
                        .append("main():").indent()
                        .append("Foo<Int> myfoo := Foo(\"5\")") // type
                                                                // mismatch!
                        .dedent()
                        .append("class Foo<X>:").indent()
                        .append("+initializer(X x):").indent()
                        .append("pass")
                        .dedent().dedent());
    }

    @Test(expected = TypeInferenceException.class)
    public void testGenericReturnTypeMismatch() throws Exception {
        getASTFromString("testGenericReturnTypeMismatch.monty",
                code -> code
                        .append("class Pair<A, B>:").indent()
                        .append("-A t1")
                        .append("-B t2")
                        .blankLine()
                        .append("+A get1():").indent()
                        .append("return t2")); // type mismatch!
    }

    @Test
    public void testShadowing() throws Exception {
        final ASTNode root = getASTFromString("testShadowing.monty",
                code -> code
                        .append("class Foo<X>:").indent()
                        .append("+X x")
                        .append("+initializer(X x):").indent()
                        .append("self.x := identity<X>(x)").dedent()
                        .append("+<X> X identity(X x):").indent()
                        .append("return x"));

        final VariableDeclaration xmember = searchFor(VariableDeclaration.class)
                .where(Predicates.hasName("x"))
                .and(Predicates.declarationTypeIs(DeclarationType.ATTRIBUTE))
                .in(root).get();

        final VariableDeclaration xparam = searchFor(VariableDeclaration.class)
                .where(Predicates.hasName("x"))
                .and(Predicates.declarationTypeIs(DeclarationType.PARAMETER))
                .in(root).get();

        final FunctionDeclaration identity = searchFor(FunctionDeclaration.class)
                .where(Predicates.hasName("identity"))
                .in(root).get();

        assertNotSame(xmember.getType(), xparam.getType());
        assertSame(xparam.getType(), identity.getType().asFunction().getReturnType());
        assertAllTypesResolved(root);
    }

    @Test
    public void testAssignMultipleInheritance() throws Exception {
        final ASTNode root = getASTFromString("testAssignMultipleInheritance.monty",
                code -> code
                        .append("X x := Pair<String, String>()")
                        .append("Y y := Pair<String, String>()")
                        .append("Pair<String, String> p := Pair<String, String>()")
                        .append("x.barX()")
                        .append("p.barX()")
                        .append("p.barY()")
                        .append("p.foo()")
                        .blankLine()
                        .append("class X:").indent()
                        .append("+barX():").indent()
                        .append("pass").dedent().dedent()
                        .append("class Y:").indent()
                        .append("+barY():").indent()
                        .append("pass").dedent().dedent()
                        .append("class Pair<A, B> inherits X, Y:").indent()
                        .append("+foo():").indent()
                        .append("pass"));

        final VariableDeclaration x = searchFor(VariableDeclaration.class)
                .where(Predicates.hasName("x"))
                .in(root).get();
        final VariableDeclaration y = searchFor(VariableDeclaration.class)
                .where(Predicates.hasName("y"))
                .in(root).get();
        final VariableDeclaration p = searchFor(VariableDeclaration.class)
                .where(Predicates.hasName("p"))
                .in(root).get();

        final ClassType classX = ClassType.classNamed("X")
                .withSuperClass(CoreClasses.objectType().getType().asClass())
                .createType();
        final ClassType classY = ClassType.classNamed("Y")
                .withSuperClass(CoreClasses.objectType().getType().asClass())
                .createType();

        final Type pair = ClassType.classNamed("Pair")
                .withSuperClasses(classX, classY)
                .addTypeParameter(CoreClasses.stringType().getType())
                .addTypeParameter(CoreClasses.stringType().getType())
                .createType();

        assertUniqueTypeIs(classX, x);
        assertUniqueTypeIs(classY, y);
        assertUniqueTypeIs(pair, p);
        assertAllTypesResolved(root);
    }

    @Test
    public void testCallGenericArgument() throws Exception {
        final ASTNode root = getASTFromString("testCallGenericArgument.monty",
                code -> code
                        .append("class Foo<X>:").indent()
                        .append("+X x")
                        .append("+initializer(X x):").indent()
                        .append("pass")
                        .dedent()
                        .append("+X get():").indent()
                        .append("return x")
                        .dedent()
                        .dedent()
                        .append("<Y> Y callFoo(Foo<Y> foo):").indent()
                        .append("return foo.get()")
                        .dedent()
                        .append("Int a := callFoo(Foo<Int>(1337))"));

        final MemberAccess fooAcces = searchFor(VariableAccess.class)
                .where(Predicates.hasName("foo"))
                .and(Predicates.onLine(11)).in(root)
                .map(ASTNode::getParentNode)
                .map(node -> (MemberAccess) node)
                .get();

        final FunctionCall fooCall = searchFor(FunctionCall.class)
                .where(Predicates.hasName("Foo"))
                .and(Predicates.onLine(13))
                .in(root)
                .get();

        final TypeVariable y = fooAcces.getRight().getType().asVariable();

        final ClassType fooInt = ClassType.classNamed("Foo")
                .withSuperClass(CoreClasses.objectType().getType().asClass())
                .addTypeParameter(CoreClasses.intType().getType())
                .createType();

        final ClassType fooY = ClassType.classNamed("Foo")
                .withSuperClass(CoreClasses.objectType().getType().asClass())
                .addTypeParameter(y)
                .createType();

        assertUniqueTypeIs(y, fooAcces.getRight());
        assertUniqueTypeIs(y, fooAcces);
        assertUniqueTypeIs(fooY, fooAcces.getLeft());
        assertUniqueTypeIs(fooInt, fooCall);
        assertAllTypesResolved(root);
    }

    @Test(expected = TypeInferenceException.class)
    public void testCallWithPartialArguments() throws Exception {
        // TODO: we might expect a more helpful exception here
        getASTFromString("testCallWithPartialArguments.monty",
                code -> code
                .append("foo<Int>(5, 4)")
                .append("<X,Y> foo(X x, Y y):").indent()
                        .append("pass"));
    }
}
