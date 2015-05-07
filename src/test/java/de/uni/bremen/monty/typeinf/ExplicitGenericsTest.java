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

    @Test
    @Monty(
    "Foo<Int> a := Foo<String>(5)\n" +
    "class Foo<X>:\n" +
    "    +X x\n" +
    "    +initializer(X x):\n" +
    "        pass")
    public void testCallWithExplicitWrongParameter2() throws Exception {
        typeCheckAndExpectFailure("no matching overload of <Foo>");
    }

    @Test
    @Monty(
    "class Pair<A, B>:\n" +
    "    -A t1\n" +
    "    -B t2\n" +
    "    +initializer(A f, B s):\n" +
    "        self.t1 := f\n" +
    "        self.t2 := s\n" +
    "    +A get1():\n" +
    "        return self.t1\n" +
    "    +B get2():\n" +
    "        return self.t2\n" +
    "foo():\n" +
    "    Pair<Int, String> pair := Pair<Int, String>(2, \"5\")"
    )
    public void testGenericDeclarationWithAssignment() throws Exception {
        this.compiler.compile();
        final VariableDeclaration decl = this.compiler.searchFor(
                VariableDeclaration.class, Predicates.hasName("pair"));

        final FunctionCall ctor = this.compiler.searchFor(
                FunctionCall.class, FunctionCall::isConstructorCall);

        final Type expected = ClassType.classNamed("Pair")
                .withSuperClass(CoreClasses.objectType().getType().asClass())
                .addTypeParameter(CoreClasses.intType().getType())
                .addTypeParameter(CoreClasses.stringType().getType())
                .createType();

        assertEquals(expected, ctor.getType());
        assertEquals(expected, decl.getType());
        this.compiler.assertAllTypesResolved();
    }

    @Test
    @Monty(
    "Foo<String> a := Foo<String>()\n" +
    "Foo<String> temp := Foo<String>()\n" +
    "a.x := \"b\"\n" +
    "temp.x := a.x\n" +
    "class Foo<X>:\n" +
    "    +X x"
    )
    public void testAssignAttribute() throws Exception {
        this.compiler.compile();

        final VariableDeclaration a = this.compiler.searchFor(VariableDeclaration.class,
                Predicates.hasName("a"));
        final VariableDeclaration temp = this.compiler.searchFor(VariableDeclaration.class,
                Predicates.hasName("temp"));

        final Type expected = ClassType.classNamed("Foo")
                .addTypeParameter(CoreClasses.stringType().getType())
                .withSuperClass(CoreClasses.objectType().getType().asClass())
                .createType();

        assertUniqueTypeIs(expected, a);
        assertUniqueTypeIs(expected, temp);
        this.compiler.assertAllTypesResolved();
    }

    @Test
    @Monty(
    "class Pair<A, B>:\n" +
    "    -A t1\n" +
    "    -B t2\n" +
    "    +initializer(A f, B s):\n" +
    "        self.t1 := f\n" +
    "        self.t2 := s\n" +
    "Pair<Int, Int> foo(Int a, Int b):\n" +
    "    return Pair(a, b)\n" +
    "test():\n" +
    "    Pair<Int, Int> p := foo(1, 2)"
    )
    public void testReturnGeneric() throws Exception {
        this.compiler.compile();
        final FunctionCall call = this.compiler.searchFor(FunctionCall.class,
                Predicates.hasName("foo"));

        final Type expected = ClassType.classNamed("Pair")
                .addTypeParameter(CoreClasses.intType().getType())
                .addTypeParameter(CoreClasses.intType().getType())
                .withSuperClass(CoreClasses.objectType().getType().asClass())
                .createType();

        assertUniqueTypeIs(expected, call);
        this.compiler.assertAllTypesResolved();
    }

    @Test
    @Monty(
    "class Foo<X>:\n" +
    "    +X x\n" +
    "    +initializer(X x):\n" +
    "        self.x := \"fail!\""
    )
    public void testAssignToTypeVarFail() throws Exception {
        typeCheckAndExpectFailure("Can not assign <String> to <X>");
    }

    @Monty(
    "class Foo<X>:\n" +
    "    +X x\n" +
    "    +initializer(X x):\n" +
    "        self.x := x"
    )
    public void testGenericConstructor() throws Exception {
        this.compiler.compile();
        this.compiler.assertAllTypesResolved();
    }

    @Test
    @Monty(
    "main():\n" +
    "    Foo<Int> myfoo := Foo(5)\n" +
    "    Int a := myfoo.bar(\"a\")\n" +
    "class Foo<X>:\n" +
    "    +initializer(X x):\n" +
    "        pass\n" +
    "    +Int bar(X x):\n" +
    "        return 2"
    )
    public void testWrongGenericParameter() throws Exception {
        typeCheckAndExpectFailure("no matching overload of <bar>");
    }

    @Test
    @Monty(
    "main():\n" +
    "    Foo<Int> myfoo := Foo(\"5\")\n" +
    "class Foo<X>:\n" +
    "    +initializer(X x):\n" +
    "        pass"
    )
    public void testGenericAssignmentFail() throws Exception {
        typeCheckAndExpectFailure("Can not assign <Foo<String>> to <Foo<Int>>");
    }

    @Test
    @Monty(
    "class Pair<A, B>:\n" +
    "    -A t1\n" +
    "    -B t2\n" +
    "    +A get1():\n" +
    "        return self.t2"
    )
    public void testGenericReturnTypeMismatch() throws Exception {
        typeCheckAndExpectFailure("Body type <B> not compatible with return type <A>");
    }

    @Test
    @Monty(
    "class Foo<X>:\n" +
    "    +X y\n" +
    "    +initializer(X x):\n" +
    "        self.y := self.identity<X>(x)\n" +
    "    +<X> X identity(X x):\n" +
    "        return x"
    )
    public void testShadowing() throws Exception {
        this.compiler.compile();
        final VariableDeclaration y = this.compiler.searchFor(VariableDeclaration.class,
                Predicates.hasName("y"));

        final VariableDeclaration x = this.compiler.searchFor(VariableDeclaration.class,
                Predicates.hasName("x"));

        final FunctionDeclaration identity = SearchAST.forNode(FunctionDeclaration.class)
                .where(Predicates.hasName("identity"))
                .in(this.compiler.getAst()).get();

        assertNotSame(y.getType(), x.getType());
        assertSame(x.getType(), identity.getType().asFunction().getReturnType());
        this.compiler.assertAllTypesResolved();
    }

    @Test
    @Monty(
    "X x := Pair<String, String>()\n" +
    "Y y := Pair<String, String>()\n" +
    "Pair<String, String> p := Pair<String, String>()\n" +
    "x.barX()\n" +
    "p.barX()\n" +
    "p.barY()\n" +
    "p.foo()\n" +
    "class X:\n" +
    "    +barX():\n" +
    "        pass\n" +
    "class Y:\n" +
    "    +barY():\n" +
    "        pass\n" +
    "class Pair<A, B> inherits X, Y:\n" +
    "    +foo():\n" +
    "        pass"
    )
    public void testAssignMultipleInheritance() throws Exception {
        this.compiler.compile();

        final VariableDeclaration x = this.compiler.searchFor(VariableDeclaration.class,
                Predicates.hasName("x"));
        final VariableDeclaration y = this.compiler.searchFor(VariableDeclaration.class,
                Predicates.hasName("y"));
        final VariableDeclaration p = this.compiler.searchFor(VariableDeclaration.class,
                Predicates.hasName("p"));

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
        this.compiler.assertAllTypesResolved();
    }

    @Test
    @Monty(
    "class Foo<X>:\n" +
    "    +X x\n" +
    "    +initializer(X x):\n" +
    "        pass\n" +
    "    +X get():\n" +
    "        return self.x\n" +
    "<Y> Y callFoo(Foo<Y> foo):\n" +
    "    return foo.get()\n" +
    "Int a := callFoo(Foo<Int>(1337))"
    )
    public void testCallGenericArgument() throws Exception {
        this.compiler.compile();

        final MemberAccess fooAcces = SearchAST.forNode(VariableAccess.class)
                .where(Predicates.hasName("foo"))
                .and(Predicates.onLine(8)).in(this.compiler.getAst())
                .map(ASTNode::getParentNode)
                .map(node -> (MemberAccess) node)
                .get();

        final FunctionCall fooCall = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.hasName("Foo"))
                .and(Predicates.onLine(9))
                .in(this.compiler.getAst())
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
        this.compiler.assertAllTypesResolved();
    }

    @Test
    @Monty(
    "foo<Int>(5, 4)\n" +
    "<X,Y> foo(X x, Y y):\n" +
    "    pass"
    )
    public void testCallWithPartialArguments() throws Exception {
        // TODO: we might expect a more helpful exception here
        typeCheckAndExpectFailure("Found no matching overload of <foo>");
    }
}
