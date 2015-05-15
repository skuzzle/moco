package de.uni.bremen.monty.typeinf;

import static org.junit.Assert.assertSame;

import org.junit.Test;

import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;
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
        this.compiler.assertAllTypesResolved();

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
        this.compiler.assertAllTypesResolved();

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
        this.compiler.assertAllTypesResolved();

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
        this.compiler.assertAllTypesResolved();

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
        this.compiler.assertAllTypesResolved();

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
                "Return type <Int> not compatible to overriden return type <$void>");
    }
}
