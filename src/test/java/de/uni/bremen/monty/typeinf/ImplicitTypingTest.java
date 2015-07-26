package de.uni.bremen.monty.typeinf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.junit.Ignore;
import org.junit.Test;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.TypeVariable;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;
import de.uni.bremen.monty.moco.ast.expression.MemberAccess;
import de.uni.bremen.monty.moco.ast.expression.VariableAccess;
import de.uni.bremen.monty.moco.util.ExpectOutput;
import de.uni.bremen.monty.moco.util.Monty;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;
import de.uni.bremen.monty.moco.util.astsearch.SearchAST;

public class ImplicitTypingTest extends AbstractTypeInferenceTest {

    @Test
    @Monty(
    "? isEmpty():\n" +
    "    return true"
    )
    public void testInferReturnType() throws Exception {
        this.compile();
        final FunctionDeclaration decl = this.compiler.searchFor(
                FunctionDeclaration.class, Predicates.hasName("isEmpty"));

        assertEquals(CoreClasses.boolType(), decl.getTypeDeclaration());
    }

    @Test
    @Monty(
    "class Ab:\n" +
    "    + initializer():\n" +
    "        print(self.attr)\n" +
    "        self.attr := \"Bernd\"\n" +
    "    + String attr := \"Hallo\""
    )
    public void testFoo() throws Exception {
        this.compile();
    }

    @Test
    @Monty(
    "A a := A()\n"+
    "String b := a.attr\n"+
    "class A:\n" +
    "    +? attr"
    )
    @Ignore
    public void testImplicitAttributeDeclarationUsageBeforeAssignment() throws Exception {
        typeCheckAndExpectFailure();
    }

    @Test
    @Monty(
    "foo():\n" +
    "    ? a\n" +
    "    if true:\n" +
    "        a := 1\n" +
    "    else:\n" +
    "        a := 'c'"
    )
    public void testInferLocalVariableNoInitializationMultipleBranchesFail() throws Exception {
        typeCheckAndExpectFailure("Can not assign <Char> to <Int>");
    }

    @Test
    @Monty(
    "foo():\n" +
    "    ? a\n" +
    "    if true:\n" +
    "        a := 1\n" +
    "    else:\n" +
    "        a := 2\n" +
    "    String b := a"
    )
    public void testInferLocalVariableNoInitializationMultipleBranchesUsageFail() throws Exception {
        typeCheckAndExpectFailure("Can not assign <Int> to <String>");
    }

    @Test
    @Monty(
    "foo():\n" +
    "    ? a\n" +
    "    if true:\n" +
    "        a := 1\n" +
    "    else:\n" +
    "        a := 2\n" +
    "    Int b := a"
    )
    public void testInferLocalVariableNoInitializationMultipleBranches() throws Exception {
        this.compile();
    }

    @Test
    @Monty(
    "foo():\n" +
    "    ? a\n" +
    "    ? b := a"
    )
    @Ignore
    public void testLocalVariableUsageBeforeAssignment() throws Exception {
        typeCheckAndExpectFailure();
    }

    @Test
    @Monty(
    "? x := identity<>(\"5\")\n" +
    "print(x)\n" +
    "<A> ? identity(A a):\n" +
    "    return a"
    )
    @ExpectOutput("5")
    public void testStaticGenericFunction() throws Exception {
        this.compile();
        final VariableDeclaration x = this.compiler.searchFor(VariableDeclaration.class,
                Predicates.hasName("x"));

        assertUniqueTypeIs(CoreClasses.stringType().getType(), x);
    }

    @Test
    @Monty(
    "? x := identity(identity(identity(\"5\")))\n" +
    "print(x)\n" +
    "<A> A identity(A a):\n" +
    "    return a"
    )
    @ExpectOutput("5")
    public void testStaticNestedGenericFunctionCall() throws Exception {
        this.compile();
        final VariableDeclaration x = this.compiler.searchFor(VariableDeclaration.class,
                Predicates.hasName("x"));

        assertUniqueTypeIs(CoreClasses.stringType().getType(), x);
    }

    @Test
    @Monty(
    "? x := factorial<>(5, \"5\")\n" +
    "<A> ? factorial(Int n, A a):\n" +
    "    if (n<=1):\n" +
    "        return 1\n" +
    "    else:\n" +
    "        return n * factorial<A>(n - 1, a)"
    )
    public void testStaticGenericRecursiveFunction() throws Exception {
        typeCheckAndExpectFailure("Encountered unresolved return type");
    }

    @Test
    @Monty(
    "Foo<String> a := Foo(5)\n" +
    "class Foo<X>:\n" +
    "    +X x\n" +
    "    +initializer(X x):\n" +
    "        pass"
    )
    public void testCallWithExplicitWrongParameter1() throws Exception {
        typeCheckAndExpectFailure("not assign <Foo<Int>> to <Foo<String>>");
    }

    @Test
    @Monty(
    "? a := Foo<String>(5)\n" +
    "class Foo<X>:\n" +
    "    +X x\n" +
    "    +initializer(X x):\n" +
    "        pass"
    )
    public void testCallWithExplicitWrongParameter2() throws Exception {
        typeCheckAndExpectFailure("no matching overload of <Foo>");
    }

    @Test
    @Monty(
    "? test := Recursive<Int>()\n" +
    "class Pair<A, B>:\n" +
    "    pass\n" +
    "class Recursive<A> inherits Pair<A, String>:\n" +
    "    pass"
    )
    public void testAssignmentWithInheritedRecursiveInstantiation() throws Exception {
        this.compile();
    }

    @Test
    @Monty(
    "? root := Node(\"a\")\n"+
    "? child := Node(\"b\")\n"+
    "root.next := child\n"+
    "class Node<A>:\n"+
    "    -A data\n"+
    "    +Node<A> next\n"+
    "    +initializer(A data):\n"+
    "        self.data := data"
    )
    public void testRecursiveType() throws Exception {
        this.compile();

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
    }

    @Test
    @Monty(
    "? root := Node<>(\"a\")\n" +
    "? child := Node<>(4)\n" +
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
    "    ? pair := Pair<>(2, \"5\")"
    )
    public void testGenericDeclarationWithAssignment() throws Exception {
        this.compile();
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
    }

    @Test
    @Monty(
    "? a := Foo<String>()\n" +
    "? temp := Foo<String>()\n" +
    "a.x := \"b\"\n" +
    "temp.x := a.x\n" +
    "class Foo<X>:\n" +
    "    +X x"
    )
    public void testAssignAttribute() throws Exception {
        this.compile();

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
    }

    @Test
    @Monty(
    "class Pair<A, B>:\n" +
    "    -A t1\n" +
    "    -B t2\n" +
    "    +initializer(A f, B s):\n" +
    "        self.t1 := f\n" +
    "        self.t2 := s\n" +
    "? foo(Int a, Int b):\n" +
    "    return Pair(a, b)\n" +
    "test():\n" +
    "    ? p := foo(1, 2)"
    )
    public void testReturnGeneric() throws Exception {
        this.compile();
        final FunctionCall call = this.compiler.searchFor(FunctionCall.class,
                Predicates.hasName("foo"));

        final VariableDeclaration p = this.compiler.searchFor(VariableDeclaration.class,
                Predicates.hasName("p"));

        final Type expected = ClassType.classNamed("Pair")
                .addTypeParameter(CoreClasses.intType().getType())
                .addTypeParameter(CoreClasses.intType().getType())
                .withSuperClass(CoreClasses.objectType().getType().asClass())
                .createType();

        assertUniqueTypeIs(expected, call);
        assertUniqueTypeIs(expected, p);
    }

    @Test
    @Monty(
    "class Foo<X>:\n" +
    "    +X y\n" +
    "    +initializer(X x):\n" +
    "        self.y := self.identity<X>(x)\n" +
    "    +<X> ? identity(X x):\n" +
    "        return x"
    )
    public void testShadowing() throws Exception {
        this.compile();
        final VariableDeclaration y = this.compiler.searchFor(VariableDeclaration.class,
                Predicates.hasName("y"));

        final VariableDeclaration x = this.compiler.searchFor(VariableDeclaration.class,
                Predicates.hasName("x"));

        final FunctionDeclaration identity = SearchAST.forNode(FunctionDeclaration.class)
                .where(Predicates.hasName("identity"))
                .in(this.compiler.getAst()).get();

        assertNotSame(y.getType(), x.getType());
        assertSame(x.getType(), identity.getType().asFunction().getReturnType());
    }

    @Test
    @Monty(
    "class Foo<X>:\n" +
    "    +X x\n" +
    "    +initializer(X x):\n" +
    "        pass\n" +
    "    +? get():\n" +
    "        return self.x\n" +
    "<Y> ? callFoo(Foo<Y> foo):\n" +
    "    return foo.get()\n" +
    "? a := callFoo(Foo<Int>(1337))"
    )
    public void testCallGenericArgument() throws Exception {
        this.compile();

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

    @Test
    @Ignore
    @Monty(
    "? x := Pair<String, String>()\n" +
    "? y := Pair<String, String>()\n" +
    "Pair<String, String> p := Pair<>()\n" +
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
        this.compile();
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
    }

}
