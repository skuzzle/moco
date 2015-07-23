package de.uni.bremen.monty.typeinf;

import static org.junit.Assert.assertSame;

import org.junit.Ignore;
import org.junit.Test;

import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;
import de.uni.bremen.monty.moco.util.ExpectOutput;
import de.uni.bremen.monty.moco.util.Monty;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;
import de.uni.bremen.monty.moco.util.astsearch.SearchAST;

public class OverrideTest extends AbstractTypeInferenceTest {

    @Test
    @Monty(
    "A a := A()\n"+
    "a.test(3)\n" +
    "B b := B()\n" +
    "b.test(5)\n" +
    "class A:\n" +
    "    +test(Int a):\n" +
    "        print(\"a\")\n" +
    "class B inherits A:\n" +
    "    +test(Int b):\n" +
    "        print(\"b\")"
    )
    public void testOverrideProcedureDirectSubClass() throws Exception {
        this.compiler.compile();

        final ProcedureDeclaration testA = SearchAST.forNode(ProcedureDeclaration.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(6))
                .in(this.compiler.getAst())
                .get();

        final ProcedureDeclaration testB = SearchAST.forNode(ProcedureDeclaration.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(9))
                .in(this.compiler.getAst())
                .get();

        final FunctionCall callTestA = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(2))
                .in(this.compiler.getAst())
                .get();
        final FunctionCall callTestB = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(4))
                .in(this.compiler.getAst())
                .get();

        assertSame(callTestA.getDeclaration(), testA);
        assertSame(callTestB.getDeclaration(), testB);
    }

    @Test
    @Monty(
    "A a := A()\n" +
    "a.test(1)\n" +
    "B b := B()\n" +
    "b.test(2)\n" +
    "C c := C()\n" +
    "c.test(5)\n" +
    "class A:\n" +
    "    +test(Int a):\n" +
    "        print(\"a\")\n" +
    "class B inherits A:\n" +
    "    pass\n"+
    "class C inherits B:\n" +
    "    +test(Int b):\n" +
    "        print(\"c\")"
    )
    public void testOverrideProcedureNotDirectSubClass() throws Exception {
        this.compiler.compile();

        final ProcedureDeclaration testA = SearchAST.forNode(ProcedureDeclaration.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(8))
                .in(this.compiler.getAst())
                .get();

        final ProcedureDeclaration testC = SearchAST.forNode(ProcedureDeclaration.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(13))
                .in(this.compiler.getAst())
                .get();

        final FunctionCall callTestA = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(2))
                .in(this.compiler.getAst())
                .get();
        final FunctionCall callTestB = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(4))
                .in(this.compiler.getAst())
                .get();
        final FunctionCall callTestC = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(6))
                .in(this.compiler.getAst())
                .get();

        assertSame(callTestA.getDeclaration(), testA);
        assertSame(callTestB.getDeclaration(), testA);
        assertSame(callTestC.getDeclaration(), testC);
    }

    @Test
    @Monty(
    "A a := A()\n" +
    "a.test(1)\n" +
    "B b := B()\n" +
    "b.test(2)\n" +
    "C c := C()\n" +
    "c.test(5)\n" +
    "class A inherits B, C:\n" +
    "    +test(Int a):\n" +
    "        print(\"a\")\n" +
    "class B:\n" +
    "    +test(Int b):\n" +
    "        print(\"b\")\n" +
    "class C:\n" +
    "    +test(Int b):\n" +
    "        print(\"c\")"
    )
    public void testOverrideProcedureMultipleInheritance() throws Exception {
        this.compiler.compile();

        final ProcedureDeclaration testA = SearchAST.forNode(ProcedureDeclaration.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(8))
                .in(this.compiler.getAst())
                .get();
        final ProcedureDeclaration testB = SearchAST.forNode(ProcedureDeclaration.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(11))
                .in(this.compiler.getAst())
                .get();
        final ProcedureDeclaration testC = SearchAST.forNode(ProcedureDeclaration.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(14))
                .in(this.compiler.getAst())
                .get();

        final FunctionCall callTestA = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(2))
                .in(this.compiler.getAst())
                .get();
        final FunctionCall callTestB = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(4))
                .in(this.compiler.getAst())
                .get();
        final FunctionCall callTestC = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(6))
                .in(this.compiler.getAst())
                .get();

        assertSame(callTestA.getDeclaration(), testA);
        assertSame(callTestB.getDeclaration(), testB);
        assertSame(callTestC.getDeclaration(), testC);
    }

    @Test
    @Monty(
    "A a := A()\n" +
    "a.test(1)\n" +
    "B b := B()\n" +
    "b.test(2)\n" +
    "C c := C()\n" +
    "c.test(5)\n" +
    "class A inherits B, C:\n" +
    "    pass\n" +
    "class B:\n" +
    "    +test(Int b):\n" +
    "        print(\"b\")\n" +
    "class C:\n" +
    "    +test(Int b):\n" +
    "        print(\"c\")"
    )
    public void testOverrideMultipleInheritanceChooseFirst() throws Exception {
        this.compiler.compile();

        final ProcedureDeclaration testB = SearchAST.forNode(ProcedureDeclaration.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(10))
                .in(this.compiler.getAst())
                .get();
        final ProcedureDeclaration testC = SearchAST.forNode(ProcedureDeclaration.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(13))
                .in(this.compiler.getAst())
                .get();

        final FunctionCall callTestA = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(2))
                .in(this.compiler.getAst())
                .get();
        final FunctionCall callTestB = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(4))
                .in(this.compiler.getAst())
                .get();
        final FunctionCall callTestC = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(6))
                .in(this.compiler.getAst())
                .get();

        assertSame(callTestA.getDeclaration(), testB);
        assertSame(callTestB.getDeclaration(), testB);
        assertSame(callTestC.getDeclaration(), testC);
    }

    @Test
    @Monty(
    "A a1 := A()\n" +
    "A a2 := a1.test(3)\n" +
    "B b1 := a2.test(2)\n" +
    "class A inherits B:\n" +
    "    +A test(Int b):\n" +
    "        return self\n" +
    "class B:\n" +
    "    +B test(Int b):\n" +
    "        return self"
    )
    public void testOverrideFunctionReturnType() throws Exception {
        this.compiler.compile();
    }

    @Test
    @Monty(
    "class A inherits B:\n" +
    "    +Int test(Int b):\n" +
    "        return 1\n" +
    "class B:\n" +
    "    +test(Int b):\n" +
    "        print(\"b\")"
    )
    public void testOverrideFunctionReturnTypeMismatch() throws Exception {
        typeCheckAndExpectFailure(
                "Return type <Int> not compatible to overridden return type <$void>");
    }

    @Test
    @Monty(
    "class A inherits B:\n" +
    "    +test(Int b):\n" +
    "        print(1)\n" +
    "class B:\n" +
    "    +Int test(Int b):\n" +
    "        return 1"
    )
    public void testOverrideFunctionReturnTypeMismatch2() throws Exception {
        typeCheckAndExpectFailure(
                "Return type <$void> not compatible to overridden return type <Int>");
    }

    @Test
    @Monty(
    "A a := A()\n" +
    "B b := B()\n" +
    "a.test(1)\n" +
    "b.test(2)\n" +
    "a.test('c')\n" +
    "class A inherits B:\n" +
    "    +test(Object b):\n" +
    "        print(1)\n" +
    "class B:\n" +
    "    +test(Int b):\n" +
    "        print(2)"
    )
    public void testOverrideExtendParameter() throws Exception {
        this.compiler.compile();

        final ProcedureDeclaration testA = SearchAST.forNode(ProcedureDeclaration.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(7))
                .in(this.compiler.getAst())
                .get();

        final ProcedureDeclaration testB = SearchAST.forNode(ProcedureDeclaration.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(10))
                .in(this.compiler.getAst())
                .get();

        final FunctionCall callA = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(3))
                .in(this.compiler.getAst())
                .get();

        final FunctionCall callA2 = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(5))
                .in(this.compiler.getAst())
                .get();

        final FunctionCall callB = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(4))
                .in(this.compiler.getAst())
                .get();

        assertSame(callA.getDeclaration(), testB);
        assertSame(callA2.getDeclaration(), testA);
        assertSame(callB.getDeclaration(), testB);
    }

    @Test
    @Monty(
    "A<Char> a := A<Char>()\n" +
    "B<Int> b := B<Int>()\n" +
    "a.test('c')\n" +
    "b.test(2)\n" +
    "class A<T> inherits B<T>:\n" +
    "    +test(T t):\n" +
    "        print(1)\n" +
    "class B<T>:\n" +
    "    +test(T t):\n" +
    "        pass"
    )
    public void testOverrideGenericClassParameter() throws Exception {
        this.compiler.compile();

        final ProcedureDeclaration testA = SearchAST.forNode(ProcedureDeclaration.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(6))
                .in(this.compiler.getAst())
                .get();

        final ProcedureDeclaration testB = SearchAST.forNode(ProcedureDeclaration.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(9))
                .in(this.compiler.getAst())
                .get();

        final FunctionCall callA = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(3))
                .in(this.compiler.getAst())
                .get();

        final FunctionCall callB = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(4))
                .in(this.compiler.getAst())
                .get();

        assertSame(callA.getDeclaration(), testA);
        assertSame(callB.getDeclaration(), testB);
    }

    @Test
    @Monty(
    "class A<T> inherits B<T>:\n" +
    "    +<X> test(T t, X y):\n" +
    "        pass\n" +
    "class B<T>:\n" +
    "    +<X> X test(T t, X y):\n" +
    "        return y"
    )
    public void testOverrideGenericMethodParameterReturnTypeMismatch() throws Exception {
        typeCheckAndExpectFailure("<$void> not compatible to overridden return type <X>");
    }

    @Test
    @Monty(
    "A<Char> a := A<Char>()\n" +
    "B<Int> b := B<Int>()\n" +
    "a.test('c', 4)\n" +
    "b.test(2, 1337)\n" +
    "class A<T> inherits B<T>:\n" +
    "    +<X> X test(T t, X y):\n" +
    "        return y\n" +
    "class B<T>:\n" +
    "    +<X> X test(T t, X y):\n" +
    "        return y"
    )
    public void testOverrideGenericMethodParameter() throws Exception {
        this.compiler.compile();

        final ProcedureDeclaration testA = SearchAST.forNode(ProcedureDeclaration.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(6))
                .in(this.compiler.getAst())
                .get();

        final ProcedureDeclaration testB = SearchAST.forNode(ProcedureDeclaration.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(9))
                .in(this.compiler.getAst())
                .get();

        final FunctionCall callA = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(3))
                .in(this.compiler.getAst())
                .get();

        final FunctionCall callB = SearchAST.forNode(FunctionCall.class)
                .where(Predicates.hasName("test"))
                .and(Predicates.onLine(4))
                .in(this.compiler.getAst())
                .get();

        assertSame(callA.getDeclaration(), testA);
        assertSame(callB.getDeclaration(), testB);
    }

    @Test
    @Monty(
    "class A:\n" +
    "    +<A> test(A a):\n" +
    "        pass\n" +
    "    +<B> test(B b):\n" +
    "        pass"
    )
    @Ignore
    public void testSameErasureSameScope() throws Exception {
        typeCheckAndExpectFailure();
    }

    @Test
    @Monty(
    "foo(A<Char> a):\n"+
    "    pass\n" +
    "foo(A<Int> a):\n" +
    "    pass\n" +
    "class A<T>:\n" +
    "    pass"
    )
    @Ignore
    public void testSameErasureClass() throws Exception {
        typeCheckAndExpectFailure();
    }

    @Test
    @Monty(
    "A a := A()\n"+
    "a.test(3)\n" +
    "B b := B()\n" +
    "b.test(5)\n" +
    "b.test('c')\n"+
    "class A:\n" +
    "    +test(Object a):\n" +
    "        print(\"a\")\n" +
    "class B inherits A:\n" +
    "    +test(Int b):\n" +
    "        print(\"b\")"
    )
    @ExpectOutput("aba")
    public void testOverrideInvariance() throws Exception {
        // no override, because of parameter invariance
        this.compiler.compile();
    }

    @Test
    @Monty(
    "A<Int> a := A<Int>()\n"+
    "a.test(3)\n" +
    "B b := B()\n" +
    "b.test(\"b\")\n" +
    "class A<T>:\n" +
    "    +test(T a):\n" +
    "        print(\"a\")\n" +
    "class B inherits A<String>:\n" +
    "    +test(String b):\n" +
    "        print(\"b\")"
    )
    public void testOverrideTypeParameter() throws Exception {
        this.compiler.compile();
    }
}
