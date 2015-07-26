package de.uni.bremen.monty.typeinf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;
import de.uni.bremen.monty.moco.util.Debug;
import de.uni.bremen.monty.moco.util.ExpectOutput;
import de.uni.bremen.monty.moco.util.Monty;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;
import de.uni.bremen.monty.moco.util.astsearch.SearchAST;

public class FunctionCallTest extends AbstractTypeInferenceTest {

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
        this.compile();
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
        this.compile();
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
        this.compile();
    }

    @Test
    @Monty(
    "A a := method(A())\n"+
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
        this.compile();
    }

    @Test
    @Monty(
    "Int a := method<B>(B())\n"+
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
        this.compile();
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
        this.compile();
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
    "? foo(Int a):\n" +
    "    if a < 1:\n" +
    "        return\n" +
    "    return"
    )
    public void testInferVoidResultTypeMultipleReturns() throws Exception {
        this.compile();
        final ProcedureDeclaration decl = this.compiler.searchFor(
                ProcedureDeclaration.class, Predicates.hasName("foo"));

        assertEquals(CoreClasses.voidType().getType(),
                decl.getType().asFunction().getReturnType());
    }

    @Test
    @Monty(
    "? foo(Int a):\n" +
    "    pass"
    )
    public void testInferVoidResultTypeNoReturn() throws Exception {
        this.compile();
        final ProcedureDeclaration decl = this.compiler.searchFor(
                ProcedureDeclaration.class, Predicates.hasName("foo"));

        assertEquals(CoreClasses.voidType().getType(),
                decl.getType().asFunction().getReturnType());
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
