package de.uni.bremen.monty.typeinf;

import org.junit.Test;

import de.uni.bremen.monty.moco.util.Debug;
import de.uni.bremen.monty.moco.util.ExpectOutput;
import de.uni.bremen.monty.moco.util.Monty;

public class ClassTest extends AbstractTypeInferenceTest {

    @Test
    @Monty(
    "print(foo('a'))\n" +
    "<A> Bool foo(A a):\n" +
    "    return a is String"
    )
    @ExpectOutput("1")
    public void testTypeVarIsAString() throws Exception {
        typeCheckAndExpectFailure("Impossible cast");
    }

    @Test
    @Monty(
    "print(foo('a'))\n" +
    "<A> Bool foo(A a):\n" +
    "    return a is Object"
    )
    @ExpectOutput("1")
    public void testTypeVarIsAObject() throws Exception {
        this.compiler.compile();
    }

    @Test
    @Monty(
    "class A<B> inherits B:\n" +
    "    pass"
    )
    public void testInheritFromVariable() throws Exception {
        typeCheckAndExpectFailure("Can not inherit from type variable");
    }

    @Test
    @Monty(
    "class A inherits B:\n" +
    "   pass\n" +
    "class B inherits A:\n" +
    "    pass"
    )
    public void testCyclicInheritance() throws Exception {
        typeCheckAndExpectFailure("Detected inheritance cycle");
    }

    @Test
    @Monty(
    "foo():\n" +
    "    Object a := self"
    )
    public void testSelfNoClassParent() throws Exception {
        typeCheckAndExpectFailure("No nested class declaration found");
    }

    @Test
    @Monty(
    "foo():\n" +
    "    Object a := parent(Foo)"
    )
    public void testParentNoClassParent() throws Exception {
        typeCheckAndExpectFailure("No nested class declaration found");
    }

    @Test
    @Monty(
    "class A:\n" +
    "    +foo():\n" +
    "        Object a := parent(Foo)"
    )
    public void testParentNotASuperClass() throws Exception {
        typeCheckAndExpectFailure("<Foo> is not a super class of <A>");
    }

    @Test
    @Monty(
    "class Foo:\n" +
    "    +Int bar():\n" +
    "        return 1\n"+
    "class A inherits Foo:\n" +
    "    +foo():\n" +
    "        Foo f := parent(Foo)\n" +
    "        Object a := parent(Foo).bar()"
    )
    public void testParent() throws Exception {
        this.compiler.compile();
    }

    @Test
    @Monty(
    "class Bar inherits Foo<?>:\n"+
    "    pass\n" +
    "class Foo<A>:\n"+
    "    pass"
    )
    @Debug
    public void testQuantifyAnonymously() throws Exception {
        typeCheckAndExpectFailure("Type can not be quantified with anonymous type variable");
    }

    @Test
    @Monty(
    "class Bar inherits Foo<Int>:\n"+
    "    pass\n" +
    "class Foo<A, B>:\n"+
    "    pass"
    )
    public void testTypeParameterCountMismatch() throws Exception {
        typeCheckAndExpectFailure("Type parameter count mismatch");
    }
}
