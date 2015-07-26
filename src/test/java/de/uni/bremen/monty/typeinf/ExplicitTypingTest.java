package de.uni.bremen.monty.typeinf;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.util.Monty;
import de.uni.bremen.monty.moco.util.TestResource;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;
import de.uni.bremen.monty.moco.util.astsearch.SearchAST;

public class ExplicitTypingTest extends AbstractTypeInferenceTest {

    @Test
    @TestResource("explicit.monty")
    public void testExplicitAttribute() throws Exception {
        this.compile();
        final VariableDeclaration decl = this.compiler.searchFor(VariableDeclaration.class,
                Predicates.hasName("bar"));
        assertUniqueTypeIs(CoreClasses.stringType().getType(), decl);
    }

    @Test
    @TestResource("explicit.monty")
    public void testExplicitFunction() throws Exception {
        this.compile();
        final FunctionDeclaration decl = this.compiler.searchFor(FunctionDeclaration.class,
                Predicates.hasName("add"));

        final Function expected = Function.named("add")
                .returning(CoreClasses.intType().getType())
                .andParameters(CoreClasses.intType().getType())
                .andParameters(CoreClasses.intType().getType())
                .createType();
        assertUniqueTypeIs(expected, decl);
        assertEquals(expected.getReturnType(), decl.getType().asFunction().getReturnType());
    }

    @Test
    @TestResource("explicit.monty")
    public void testExplicitFunctionParameter() throws Exception {
        this.compile();
        final VariableDeclaration decl = SearchAST.forNode(VariableDeclaration.class)
                .where(Predicates.hasName("a"))
                .and(SearchAST.forParent(ProcedureDeclaration.class)
                .where(Predicates.hasName("add")))
                .in(this.compiler.getAst()).get();
        assertUniqueTypeIs(CoreClasses.intType().getType(), decl);
    }

    @Test
    @TestResource("explicit.monty")
    public void testVoidMethod() throws Exception {
        this.compile();
        final ProcedureDeclaration decl = this.compiler.searchFor(ProcedureDeclaration.class,
                Predicates.hasName("noop"));

        final Function expected = Function.named("noop")
                .returningVoid()
                .andParameter(CoreClasses.stringType().getType())
                .createType();
        assertUniqueTypeIs(expected, decl);
        assertEquals(expected.getReturnType(), CoreClasses.voidType().getType());
    }

    @Test
    @TestResource("explicit.monty")
    public void testExplicitProcedureParameter() throws Exception {
        this.compile();
        final VariableDeclaration decl = this.compiler.searchFor(VariableDeclaration.class,
                Predicates.hasName("a"));
        assertUniqueTypeIs(CoreClasses.stringType().getType(), decl);
    }

    @Test
    @Monty(
    "Int foo():\n" +
    "    return \"abc\""
    )
    public void testReturnTypeMismatchFunction() throws Exception {
        typeCheckAndExpectFailure("type <String> not compatible with return type <Int>");
    }

    @Test
    @Monty(
    "foo():\n" +
    "    return \"abc\""
    )
    public void testReturnTypeMismatchProcedure() throws Exception {
        typeCheckAndExpectFailure("must not return a value");
    }

    @Test
    @Monty(
    "class Foo:\n" +
    "    +initializer():\n" +
    "        return \"abc\""
    )
    public void testReturnTypeMismatchConstructor() throws Exception {
        typeCheckAndExpectFailure("must not return a value");
    }
}
