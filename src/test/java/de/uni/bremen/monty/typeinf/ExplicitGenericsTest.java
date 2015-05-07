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
import de.uni.bremen.monty.moco.util.Monty;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;
import de.uni.bremen.monty.moco.util.astsearch.SearchAST;
import de.uni.bremen.monty.moco.visitor.typeinf.TypeInferenceException;

public class ExplicitGenericsTest extends AbstractTypeInferenceTest {

    @Test
    @Monty(
    "class Pair<A, B>:\n" +
    "    pass"
    )
    public void testClassWithSimpleGenerics() throws Exception {
        this.compiler.compile();
        final ClassDeclaration decl = this.compiler.searchFor(
                ClassDeclaration.class, Predicates.hasName("Pair"));

        final Type expected = ClassType.classNamed("Pair")
                .withSuperClass(CoreClasses.objectType().getType().asClass())
                .addTypeParameter(TypeVariable.anonymous().createType())
                .addTypeParameter(TypeVariable.anonymous().createType())
                .createType();
        assertTrue(Unification.testIf(decl.getType()).isA(expected).isSuccessful());
        this.compiler.assertAllTypesResolved();
    }

    @Test
    @Monty(
    "? x := identity<String>(\"5\")\n" +
    "<A> A identity(A a):\n" +
    "    return a"
    )
    public void testStaticGenericFunction() throws Exception {
        this.compiler.compile();
        final VariableDeclaration x = this.compiler.searchFor(VariableDeclaration.class,
                Predicates.hasName("x"));

        assertUniqueTypeIs(CoreClasses.stringType().getType(), x);
        this.compiler.assertAllTypesErased();
        this.compiler.assertAllTypesResolved();
    }

    @Test
    @Monty(
    "Int x := factorial<String>(5, \"5\")\n" +
    "<A> Int factorial(Int n, A a):\n" +
    "    if (n<=1):\n" +
    "        return 1\n" +
    "    else:\n" +
    "        return n * factorial<A>(n - 1, a)"
    )
    public void testStaticGenericRecursiveFunction() throws Exception {
        this.compiler.compile();
        final VariableDeclaration x = this.compiler.searchFor(VariableDeclaration.class,
                Predicates.hasName("x"));

        assertUniqueTypeIs(CoreClasses.intType().getType(), x);
        this.compiler.assertAllTypesResolved();
    }

    @Test
    @Monty(
    "class Pair<A, B>:\n" +
    "    pass\n" +
    "class B inherits Pair<Char, String>:\n"+
    "    pass"
    )
    public void testClassWithInheritedInstantiation() throws Exception {
        this.compiler.compile();
        final ClassDeclaration decl = this.compiler.searchFor(ClassDeclaration.class,
                Predicates.hasName("B"));

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
        this.compiler.assertAllTypesResolved();
    }

    @Test
    @Monty(
    "Pair<Char, String> test := Bc()\n" +
    "class Pair<A, B>:\n" +
    "    pass\n" +
    "class Bc inherits Pair<Char, String>:\n" +
    "    pass"
    )
    public void testAssignmentWithInheritedInstantiation() throws Exception {
        this.compiler.compile();
        this.compiler.assertAllTypesResolved();
    }

    @Test
    @Monty(
    "Pair<Int, String> test := B()\n" +
    "class Pair<A, B>:\n" +
    "    pass\n" +
    "class B inherits Pair<Char, String>:\n" +
    "    pass")
    public void testAssignmentWithInheritedInstantiationFail() throws Exception {
        typeCheckAndExpectFailure("Can not assign <B> to <Pair");
    }

    @Test
    @Monty(
    "Pair<Int, String> test := Recursive<Int>()\n" +
    "class Pair<A, B>:\n" +
    "    pass\n" +
    "class Recursive<A> inherits Pair<A, String>:\n" +
    "    pass"
    )
    public void testAssignmentWithInheritedRecursiveInstantiation() throws Exception {
        this.compiler.compile();
        this.compiler.assertAllTypesResolved();
    }

    @Test
    @Monty(
    "Pair<Int, String> test := Recursive<String>()\n" +
    "class Pair<A, B>:\n" +
    "    pass\n" +
    "class Recursive<A> inherits Pair<A, String>:\n" +
    "    pass")
    public void testAssignmentWithInheritedRecursiveInstantiationFail()
            throws Exception {
        typeCheckAndExpectFailure("Can not assign <Recursive<String>> to <Pair");
    }

    @Test
    @Monty(
    "class Pair<A, B>:\n" +
    "    pass\n" +
    "class Recursive<A> inherits Pair<A, String>:\n" +
    "    pass"
    )
    public void testClassWithInheritedRecursiveInstantiation() throws Exception {
        this.compiler.compile();

        final TypeInstantiation superClassDecl = this.compiler.searchFor(
                TypeInstantiation.class, Predicates.hasName("Pair"));

        final Type expectedTypeInst = ClassType.classNamed("Pair")
                .withSuperClass(CoreClasses.objectType().getType().asClass())
                .addTypeParameter(TypeVariable.named("A").createType())
                .addTypeParameter(CoreClasses.stringType().getType())
                .createType();

        assertTrue(Unification.testIf(superClassDecl.getType()).isA(expectedTypeInst).isSuccessful());
        this.compiler.assertAllTypesResolved();
    }

    @Test
    @Monty(
    "Node<String> root := Node<String>(\"a\")\n"+
    "Node<String> child := Node<String>(\"b\")\n"+
    "root.next := child\n"+
    "class Node<A>:\n"+
    "    -A data\n"+
    "    +Node<A> next\n"+
    "    +initializer(A data):\n"+
    "        self.data := data"
    )
    public void testRecursiveType() throws Exception {
        this.compiler.compile();

        final VariableDeclaration child = this.compiler.searchFor(VariableDeclaration.class,
                Predicates.hasName("child"));
        final VariableAccess var = this.compiler.searchFor(VariableAccess.class,
                Predicates.hasName("next"));
        final Type expected = ClassType.classNamed("Node")
                .withSuperClass(CoreClasses.objectType().getType().asClass())
                .addTypeParameter(CoreClasses.stringType().getType())
                .createType();

        assertUniqueTypeIs(expected, var);
        assertUniqueTypeIs(expected, child);
        this.compiler.assertAllTypesResolved();
        this.compiler.assertAllTypesErased();
    }

    @Test
    @Monty(
    "Node<String> root := Node<String>(\"a\")\n" +
    "Node<Int> child := Node<Int>(4)\n" +
    "root.next := child\n" +
    "class Node<A>:\n" +
    "    -A data\n" +
    "    +Node<A> next\n" +
    "    +initializer(A data):\n" +
    "        self.data := data"
    )
    public void testRecursiveTypeFail() throws Exception {
        typeCheckAndExpectFailure("Can not assign <Node<Int>> to <Node<String>>");
    }

    @Test
    @Monty(
    "Foo<String> a := Foo<String>(5)\n" +
    "class Foo<X>:\n" +
    "    +X x\n" +
    "    +initializer(X x):\n" +
    "        pass"
    )
    public void testCallWithExplicitWrongParameter1() throws Exception {
        typeCheckAndExpectFailure("Found no matching overload");
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
