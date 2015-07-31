package de.uni.bremen.monty.typeinf;

import org.junit.Ignore;
import org.junit.Test;

import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.util.Debug;
import de.uni.bremen.monty.moco.util.ExpectOutput;
import de.uni.bremen.monty.moco.util.Monty;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;


public class MiscTest extends AbstractTypeInferenceTest {
    
    @Test
    @Monty(
    "? h := Holder(Holder2(1))\n"+
    "? outer := h.value\n" +
    "? inner := outer.value\n" +
    "print(inner)\n" +
    "class Holder2<T>:\n" +
    "    +T value\n" +
    "    +initializer(? value):\n" +
    "        self.value := value\n"+
    "    +T get():\n" +
    "        return self.value\n" +
    "class Holder<T>:\n" +
    "    +T value\n" +
    "    +initializer(? value):\n" +
    "        self.value := value\n"+
    "    +T get():\n" +
    "        return self.value"
    )
    @ExpectOutput("1")
    @Debug
    public void testAccessErasedMember() throws Exception {
        compile();
    }
    
    @Test
    @Monty(
    "? a\n" +
    "a.foo()"
    )
    public void testMemberAccessOnUnknown() throws Exception {
        typeCheckAndExpectFailure("Uninitialized variable");
    }
    
    @Test
    @Monty(
    "? a\n" +
    "? b\n" +
    "foo(a, b)\n" +
    "foo(String c, Int d):\n"+
    "    pass"
    )
    @Ignore
    public void testUseNonInitializedAsParameter() throws Exception {
        typeCheckAndExpectFailure("Uninitialized variable");
    }

    @Test
    @Monty(
    "? a\n"+
    "? b := a"
    )
    public void testAssignNonInitialized() throws Exception {
        typeCheckAndExpectFailure("Assignment of uninitialized variable");
    }
    
    @Test
    @Monty(
    "class A:\n" +
    "    +? attribute := 'c'"
    )
    public void testInferAttribute() throws Exception {
        final VariableDeclaration attr = compile().searchFor(VariableDeclaration.class, 
                Predicates.hasName("attribute"));
        
        assertUniqueTypeIs(CoreClasses.charType().getType(), attr);
    }
    
    @Test
    @Monty(
    "class A:\n" +
    "    +? attribute\n" +
    "    +initializer():\n" +
    "        if true:\n"+
    "            self.attribute := 10\n" +
    "        else:\n"+
    "            self.attribute := 'c'"
    )
    @Debug
    public void testUninitializedAttribute() throws Exception {
        typeCheckAndExpectFailure("Can not assign <Char> to <Int>");
    }
    
    @Test
    @Monty(
    "class A:\n" +
    "    +? attribute\n" +
    "    +initializer():\n" +
    "            self.attribute := 10\n"
    )
    @Debug
    public void testUninitializedAttribute2() throws Exception {
        compile();
        final VariableDeclaration decl = compiler.searchFor(VariableDeclaration.class, 
                Predicates.hasName("attribute"));
        
        assertUniqueTypeIs(CoreClasses.intType().getType(), decl);
    }
    
    @Test
    @Monty(
    "class A:\n" +
            "    +String attribute\n" +
            "    +initializer():\n" +
            "            attribute := \"10\"\n"
    )
    public void testUnqualifiedMemberAccess() throws Exception {
        typeCheckAndExpectFailure("Unqualified member access: <attribute>");
    }
    
    @Test
    @Monty(
    "Int a := foo\n" +
    "foo():\n" +
    "    pass"
    )
    public void testVarAccessNoVar() throws Exception {
        this.exception.expectMessage("Identifier is not defined: foo");
        this.compiler.typeCheck();
    }

    @Test
    @Monty(
    "foo := 1\n" +
    "foo():\n" +
    "    pass"
    )
    public void testAssignToNonVar() throws Exception {
        this.exception.expectMessage("Identifier is not defined: foo");
        this.compiler.typeCheck();
    }

    @Test
    @Monty(
    "print(\"a\")\n"+
    "print(foo(true))\n" +
    "print(foo(false))\n" +
    "? foo(Bool cond):\n" +
    "    ? result := \"b\"\n"+
    "    if cond:\n" +
    "        result := \"a\"\n" +
    "    else:\n" +
    "        result := \"b\"\n" +
    "    return result"
    )
    @ExpectOutput("aab")
    public void testIfStatement() throws Exception {
        this.compile();
    }

    @Test
    @Monty(
    "print(\"a\")"
    )
    @ExpectOutput("a")
    public void testPrint() throws Exception {
        this.compile();
    }
}
