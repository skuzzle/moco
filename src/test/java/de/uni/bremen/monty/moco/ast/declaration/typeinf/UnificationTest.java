package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

public class UnificationTest {

    private final ClassType Object = ClassType.named("Object").createType();
    private final ClassType Int = ClassType.named("Int").withSuperClass(this.Object).createType();
    private final ClassType String = ClassType.named("String").withSuperClass(this.Object).createType();
    private final Type A = TypeVariable.named("A").createType();
    private final Type B = TypeVariable.named("B").createType();

    private final Type objObjToA = Function.named("Object x Object -> A")
            .returning(this.A)
            .andParameters(this.Object, this.Object)
            .createType();

    private final Type intIntToB = Function.named("Int x Int -> B")
            .returning(this.B)
            .andParameters(this.Int, this.Int)
            .createType();

    private final Type aAToString = Function.named("A x A -> String")
            .returning(this.String)
            .andParameters(this.A, this.A)
            .createType();


    @Test
    public void testUnifySubtyping() throws Exception {
        final Unification unification = Unification
                .testIf(this.intIntToB)
                .isA(this.objObjToA);
        assertTrue(unification.isSuccessful());

        final Type substitute = unification.getSubstitute(this.A);
        assertTrue(substitute == this.A || substitute == this.B);
    }

    @Test
    public void testUnifyFunctions() throws Exception {
        final Unification unification = Unification.of(this.intIntToB)
                .with(this.aAToString);
        assertTrue(unification.isSuccessful());
        assertSame(this.Int, unification.getSubstitute(this.A));

        final Type expected = Function.named("expected")
                .returning(this.String)
                .andParameters(this.Int, this.Int)
                .createType();
        Assert.assertEquals(expected, unification.apply(this.intIntToB));
        Assert.assertEquals(expected, unification.apply(this.aAToString));
    }

}
