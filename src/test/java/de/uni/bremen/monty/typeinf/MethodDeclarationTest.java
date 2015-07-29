package de.uni.bremen.monty.typeinf;

import org.junit.Test;

import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.util.Debug;
import de.uni.bremen.monty.moco.util.ExpectOutput;
import de.uni.bremen.monty.moco.util.Monty;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;

public class MethodDeclarationTest extends AbstractTypeInferenceTest {

    @Test
    @Monty(
    "? bar(Int b):\n" +
    "    return b\n" +
    "? foo(? a):\n" +
    "    return bar(a)" 
    )
    @Debug
    public void testInferParameterTypesFromCallInBody() throws Exception {
        compile();
        final VariableDeclaration a = compiler.searchFor(VariableDeclaration.class, 
                Predicates.hasName("a"));
        
        assertUniqueTypeIs(CoreClasses.intType().getType(), a);
    }
    
    @Test
    @Monty(
    "? foo(? a):\n" +
    "    a := 1\n"+
    "    return a"
    )
    public void testInferParameterTypesFromBody() throws Exception {
        compile();
        final VariableDeclaration a = compiler.searchFor(VariableDeclaration.class, 
                Predicates.hasName("a"));
        
        assertUniqueTypeIs(CoreClasses.intType().getType(), a);
    }
    
    @Test
    @Monty(
    "Bool c := foo(1)\n"+
    "? foo(? a):\n" +
    "    if a:\n" +
    "        return a\n"+
    "    else:\n" +
    "        return false"
    )
    public void testInferParameterTypeWrongCallType() throws Exception {
        typeCheckAndExpectFailure("Found no matching overload of <foo>");
    }
    
    @Test
    @Monty(
    "foo(1)\n"+
    "foo(? a):\n" +
    "    if a:\n" +
    "        return\n"+
    "    else:\n" +
    "        return"
    )
    public void testInferParameterTypeWrongCallType2() throws Exception {
        typeCheckAndExpectFailure("Found no matching overload of <foo>");
    }
    
    @Test
    @Monty(
    "Bool c := foo(true)\n"+
    "print(c)\n" +
    "? foo(? a):\n" +
    "    ? result := false\n" +
    "    if a:\n" +
    "        result := a\n"+
    "    else:\n" +
    "        result :=false\n" +
    "    return result"
    )
    @ExpectOutput("1")
    public void testTargetTypeConditional() throws Exception {
        compile();
        final VariableDeclaration a = compiler.searchFor(VariableDeclaration.class, 
                Predicates.hasName("a"));
        
        assertUniqueTypeIs(CoreClasses.boolType().getType(), a);
    }
    
    @Test
    @Monty(
    "? foo(? a):\n" +
    "    return a"
    )
    public void testUnresolvedParameterType() throws Exception {
        typeCheckAndExpectFailure("Could not infer return type of <foo>");
    }

    
    @Test
    @Monty(
    "? foo():\n" +
    "    if true:\n" +
    "        return false\n" +
    "    else:\n" +
    "        return 1"
    )
    public void testNoCommonTypeFunction() throws Exception {
        typeCheckAndExpectFailure("Could not uniquely determine type of function's body");
    }

    @Test
    @Monty(
    "foo():\n" +
    "    if true:\n" +
    "        return false\n" +
    "    else:\n" +
    "        return 1"
    )
    public void testNoCommonTypeProcedure() throws Exception {
        typeCheckAndExpectFailure("Could not uniquely determine type of function's body");
    }

    @Test
    @Monty(
    "foo():\n" +
    "    if true:\n" +
    "        return 1\n" +
    "    else:\n" +
    "        return 1"
    )
    public void testProcedureNotAVoid() throws Exception {
        typeCheckAndExpectFailure("Could not uniquely determine type of function's body");
    }

    @Test
    @Monty(
    "Int foo():\n" +
    "    if true:\n" +
    "        return '1'\n" +
    "    else:\n" +
    "        return '1'"
    )
    public void testReturnTypeMismatch() throws Exception {
        typeCheckAndExpectFailure("Body type <Char> not compatible with return type <Int>");
    }

    @Test
    @Monty(
    "<A> ? foo(A a):\n" +
    "    if true:\n" +
    "        return a\n" +
    "    else:\n" +
    "        return 'c' as Object\n"
    )
    public void testInferTypeVarReturnType() throws Exception {
        this.compile();
    }
}
