package de.uni.bremen.monty.typeinf;

import org.junit.Test;

import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;
import de.uni.bremen.monty.moco.ast.expression.ParentExpression;
import de.uni.bremen.monty.moco.util.Debug;
import de.uni.bremen.monty.moco.util.ExpectOutput;
import de.uni.bremen.monty.moco.util.Monty;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;
import de.uni.bremen.monty.moco.util.astsearch.SearchAST;

public class ClassTest extends AbstractTypeInferenceTest {

    @Test
    @Monty(
    "print(foo('a'))\n" +
    "<A> Bool foo(A a):\n" +
    "    return a is String"
    )
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
        this.compile();
    }

    @Test
    @Monty(
    "print(foo('a'))\n" +
    "<A> Bool foo(A a):\n" +
    "    return ('c' as Object) is A"
    )
    public void testObjectIsNotATypeVar() throws Exception {
        typeCheckAndExpectFailure("Can not use type variable <A> as target of 'is' expression");
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
        this.compile();

        final ClassType expected = ClassType.classNamed("Foo")
                .withSuperClass(CoreClasses.objectType().getType().asClass())
                .createType();

        final ParentExpression parent = SearchAST.forNode(ParentExpression.class)
                .where(Predicates.onLine(6))
                .in(this.compiler.getAst())
                .get();

        final FunctionCall call = this.compiler.searchFor(FunctionCall.class, Predicates.hasName("bar"));

        assertUniqueTypeIs(CoreClasses.intType().getType(), call);
        assertUniqueTypeIs(expected, parent);
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
    
    @Test
    @Monty(
    "Object o := A<>(\"a\")\n" +
    "print((o as A<String>).b)\n" +
    "class A<B>:\n" +
    "    +B b\n" +
    "    +initializer(B b):\n" +
    "        self.b := b"
    )
    @ExpectOutput("a")
    public void testCastToGeneric() throws Exception {
        compile();
    }
}
