package de.uni.bremen.monty.typeinf;

import static org.junit.Assert.assertSame;

import org.junit.Test;

import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;
import de.uni.bremen.monty.moco.util.Debug;
import de.uni.bremen.monty.moco.util.ExpectOutput;
import de.uni.bremen.monty.moco.util.Monty;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;
import de.uni.bremen.monty.moco.util.astsearch.SearchAST;

public class FunctionCallTest extends AbstractTypeInferenceTest {

    @Test
    @Monty(
    "Int foo(? n):\n" +
    "    return foo(n)"
    )
    public void testInferParametersRecursiveCall() throws Exception {
        typeCheckAndExpectFailure("Functions with inferred return- or parameter type can not be called recursively");
    }

    @Test
    @Monty(
    "Int foo(Object o):\n" +
    "    return 1\n" +
    "Int foo(? n):\n" +
    "    return foo(n)"
    )
    public void testInferParametersRecursiveCallWithOverload() throws Exception {
        typeCheckAndExpectFailure("Ambiguous cal");
    }

    @Test
    @Monty(
    "Object c := f('c')\n" +
    "Int i := f(5)\n" +
    "<A> A f(A a):\n"+
    "    return a\n" +
    "Int f(Int i):\n" +
    "    return i"
    )
    public void testBestFitTypeVarVSConcreteType() throws Exception {
        compile();
        final FunctionDeclaration declA = SearchAST.forNode(FunctionDeclaration.class).where(Predicates.hasName("f")).and(Predicates.onLine(3)).in(this.compiler.getAst()).get();
        final FunctionDeclaration declB = SearchAST.forNode(FunctionDeclaration.class).where(Predicates.hasName("f")).and(Predicates.onLine(5)).in(this.compiler.getAst()).get();
        final FunctionCall callA = SearchAST.forNode(FunctionCall.class).where(Predicates.hasName("f")).and(Predicates.onLine(1)).in(this.compiler.getAst()).get();
        final FunctionCall callB = SearchAST.forNode(FunctionCall.class).where(Predicates.hasName("f")).and(Predicates.onLine(2)).in(this.compiler.getAst()).get();

        assertSame(callA.getDeclaration(), declA);
        assertSame(callB.getDeclaration(), declB);
    }

    @Test
    @Monty(
    "Int a := method(\"foo\", 5)\n"+
    "method(1337)\n" +
    "Int method(String a, Int b):\n" +
    "    return 5\n" +
    "method(Int a):\n" +
    "    pass"
    )
    public void testOverloadWithPrimitives() throws Exception {
        compile();
    }

    @Test
    @Monty(
    "Int a := method(\"foo\", 5)\n"+
    "Int method(String a, Int b):\n" +
    "    return 5\n" +
    "method(String a, Int b):\n" +
    "    pass")
    public void testDuplicateSignature() throws Exception {
        typeCheckAndExpectFailure("Ambiguous");
    }

    @Test
    @Monty(
    "A a := method(A())\n"+
    "B b := method(B())\n"+
    "class A:\n"+
    "    pass\n" +
    "class B inherits A:\n" +
    "    pass\n" +
    "A method(A a):\n" +
    "    return a\n" +
    "B method(B b):\n" +
    "    return b"
    )
    public void testBestFit() throws Exception {
        compile();
        final FunctionCall first = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.onLine(1))
                .and(Predicates.hasName("method"))
                .in(this.compiler.getAst())
                .get();

        final FunctionCall second = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.onLine(2))
                .and(Predicates.hasName("method"))
                .in(this.compiler.getAst())
                .get();

        final FunctionDeclaration decl1 = SearchAST.forNode(FunctionDeclaration.class)
                .where(Predicates.onLine(7))
                .and(Predicates.hasName("method"))
                .in(this.compiler.getAst())
                .get();

        final FunctionDeclaration decl2 = SearchAST.forNode(FunctionDeclaration.class)
                .where(Predicates.onLine(9))
                .and(Predicates.hasName("method"))
                .in(this.compiler.getAst())
                .get();

        assertSame(decl1, first.getDeclaration());
        assertSame(decl2, second.getDeclaration());
    }

    @Test
    @Monty(
    "A a := method(A(), B())\n"+
    "B b := method(B(), C())\n"+
    "class A:\n"+
    "    pass\n" +
    "class B inherits A:\n" +
    "    pass\n" +
    "class C inherits B:\n" +
    "    pass\n" +
    "A method(A a, A c):\n" +
    "    return a\n" +
    "B method(A a, B b):\n" +
    "    return b"
    )
    public void testBestFitMultiple() throws Exception {
        compile();

        final FunctionCall first = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.onLine(1))
                .and(Predicates.hasName("method"))
                .in(this.compiler.getAst())
                .get();

        final FunctionCall second = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.onLine(2))
                .and(Predicates.hasName("method"))
                .in(this.compiler.getAst())
                .get();

        final FunctionDeclaration decl2 = SearchAST.forNode(FunctionDeclaration.class)
                .where(Predicates.onLine(11))
                .and(Predicates.hasName("method"))
                .in(this.compiler.getAst())
                .get();

        assertSame(decl2, first.getDeclaration());
        assertSame(decl2, second.getDeclaration());
    }

    @Test
    @Monty(
    "A a := method(A())\n"+
    "A b := method(B())\n"+
    "class A:\n"+
    "    pass\n" +
    "class B inherits A:\n" +
    "    pass\n" +
    "A method(A a):\n" +
    "    return a\n" +
    "<X> Int method(X x):\n" +
    "    return 5"
    )
    public void testBestFitGenerics() throws Exception {
        compile();

        final FunctionCall first = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.onLine(1))
                .and(Predicates.hasName("method"))
                .in(this.compiler.getAst())
                .get();

        final FunctionCall second = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.onLine(2))
                .and(Predicates.hasName("method"))
                .in(this.compiler.getAst())
                .get();

        final FunctionDeclaration decl1 = SearchAST.forNode(FunctionDeclaration.class)
                .where(Predicates.onLine(7))
                .and(Predicates.hasName("method"))
                .in(this.compiler.getAst())
                .get();

        assertSame(decl1, first.getDeclaration());
        assertSame(decl1, second.getDeclaration());
    }

    @Test
    @Monty(
    "Int a := method<B>(B())\n"+
    "Int b := method<A>(A())\n" +
    "class A:\n"+
    "    pass\n" +
    "class B inherits A:\n" +
    "    pass\n" +
    "A method(A a):\n" +
    "    return a\n" +
    "<X> Int method(X x):\n" +
    "    return 5"
    )
    public void testBestFitExplicitGenerics() throws Exception {
        compile();
        final FunctionCall first = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.onLine(1))
                .and(Predicates.hasName("method"))
                .in(this.compiler.getAst())
                .get();

        final FunctionCall second = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.onLine(2))
                .and(Predicates.hasName("method"))
                .in(this.compiler.getAst())
                .get();

        final FunctionDeclaration decl2 = SearchAST.forNode(FunctionDeclaration.class)
                .where(Predicates.onLine(9))
                .and(Predicates.hasName("method"))
                .in(this.compiler.getAst())
                .get();

        assertSame(decl2, first.getDeclaration());
        assertSame(decl2, second.getDeclaration());
    }

    @Test
    @Monty(
    "Int a := method<B>(B())\n"+
    "class A:\n"+
    "    pass\n" +
    "class B inherits A:\n" +
    "    pass\n" +
    "<Y> String method(Y y):\n" +
    "    return \"\"\n" +
    "<X> Int method(X x):\n" +
    "    return 5")
    public void testBestFit2GenericMethods() throws Exception {
        typeCheckAndExpectFailure("Ambiguous");
    }

    @Test
    @Monty(
    "method():\n" +
    "    method()"
    )
    public void testRecursiveProcedure() throws Exception {
        compile();
    }

    @Test
    @Monty(
    "? foo(Int n):\n" +
    "    return foo(n - 1)"
    )
    @Debug
    public void testRecursiveCall() throws Exception {
        typeCheckAndExpectFailure("Encountered unresolved return type");
    }

    @Test
    @Monty(
    "? factorial(Int n):\n" +
    "    if n < 2:\n" +
    "        return 1\n" +
    "    else:\n" +
    "        return n * factorial(n - 1)"
    )
    public void testRecursiveFactorial() throws Exception {
        typeCheckAndExpectFailure("Encountered unresolved return type");
    }

    @Test
    @Monty(
    "Int i := foo()\n" +
    "<A> Int foo():\n" +
    "    return 1"
    )
    public void testCanNotInferFromCall() throws Exception {
        typeCheckAndExpectFailure("Could not recover all type parameters from call of <foo>");
    }

    @Test
    @Monty(
    "Int i := foo<Int>()\n" +
    "<A,B> Int foo():\n" +
    "    return 1"
    )
    public void testCanNotInferAllFromCall() throws Exception {
        typeCheckAndExpectFailure("Found no matching overload of <foo>");
    }

    @Test
    @Monty(
    "Int a := 10\n"+
    "Int b := a()"
    )
    public void testCallNonCallable() throws Exception {
        this.exception.expectMessage("Identifier is not defined: a");
        this.compiler.typeCheck();
    }

    @Test
    @Monty(
    "foo('c')\n" +
    "Int foo(Int i):\n" +
    "    return i\n" +
    "foo():\n"+
    "    pass"
    )
    public void testOverloadNoMatches() throws Exception {
        typeCheckAndExpectFailure("Found no matching overload of <foo>");
    }

    @Test
    @Monty(
    "foo(1234)\n"+
    "? foo(Int e):\n" +
    "    print(e)"
    )
    @ExpectOutput("1234")
    public void testResolveToVoid() throws Exception {
        compile();
    }
}
