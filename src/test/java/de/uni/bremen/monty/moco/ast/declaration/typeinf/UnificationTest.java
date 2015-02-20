package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

public class UnificationTest {

    private final ClassType Object = ClassType.named("Object").createType();
    private final ClassType Number = ClassType.named("Number").withSuperClass(this.Object).createType();
    private final ClassType Int = ClassType.named("Int").withSuperClass(this.Number).createType();
    private final ClassType String = ClassType.named("String").withSuperClass(this.Object).createType();
    private final Type A = TypeVariable.named("A").createType();
    private final Type B = TypeVariable.named("B").createType();
    private final ClassType listDecl = ClassType.named("List").withSuperClass(this.Object).addTypeParameter(this.A).createType();
    private final ClassType intListInst = ClassType.named("List").withSuperClass(this.Object).addTypeParameter(this.Int).createType();
    private final ClassType stringListInst = ClassType.named("List").withSuperClass(this.Object).addTypeParameter(this.String).createType();
    private final ClassType numberListInst = ClassType.named("List").withSuperClass(this.Object).addTypeParameter(this.Number).createType();
    private final ClassType mySubType = ClassType.named("MyList").withSuperClass(this.intListInst).createType();

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
    public void testUnifySubtypingWithIncompatibleParameters() throws Exception {
        final Unification unification = Unification
                .testIf(this.numberListInst)
                .isA(this.stringListInst);
        assertFalse(unification.isSuccessful());
    }

    @Test
    public void testUnifySubtypingInParameterInheritance() throws Exception {
        final Unification unification = Unification
                .testIf(this.mySubType)
                .isA(this.numberListInst);
        assertTrue(unification.isSuccessful());
    }

    @Test
    public void testUnifySubtypingInParameterInheritanceInferType() throws Exception {
        final Unification unification = Unification
                .testIf(this.mySubType)
                .isA(this.listDecl);
        assertTrue(unification.isSuccessful());
        assertEquals(this.Int, unification.getSubstitute(this.A));
    }

    @Test
    public void testUnifySubtypingInParameters() throws Exception {
        final Unification unification = Unification
                .testIf(this.intListInst)
                .isA(this.numberListInst);
        assertTrue(unification.isSuccessful());
    }

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
    public void testUnifySubtypingWithParameters() throws Exception {
        final Unification unification = Unification
                .testIf(this.intListInst)
                .isA(this.listDecl);
        assertTrue(unification.isSuccessful());
        assertEquals(this.Int, unification.getSubstitute(this.A));
    }

    @Test
    public void testUnifyFunctions() throws Exception {
        final Unification unification = Unification.testIf(this.intIntToB)
                .isA(this.aAToString);
        assertTrue(unification.isSuccessful());
        assertSame(this.Int, unification.getSubstitute(this.A));

        final Type expected = Function.named("expected")
                .returning(this.String)
                .andParameters(this.Int, this.Int)
                .createType();
        Assert.assertEquals(expected, unification.apply(this.intIntToB));
        Assert.assertEquals(expected, unification.apply(this.aAToString));
    }

    @Test
    public void testTopIsATop() throws Exception {
        final Unification unification = Unification
                .testIf(CoreTypes.TOP)
                .isA(CoreTypes.TOP);
        assertTrue(unification.isSuccessful());
    }

    @Test
    public void testBotIsABot() throws Exception {
        final Unification unification = Unification
                .testIf(CoreTypes.BOT)
                .isA(CoreTypes.BOT);
        assertTrue(unification.isSuccessful());
    }

    @Test
    public void testBotIsATop() throws Exception {
        final Unification unification = Unification
                .testIf(CoreTypes.BOT)
                .isA(CoreTypes.TOP);
        assertTrue(unification.isSuccessful());
    }

    @Test
    public void testVoidIsAVoid() throws Exception {
        final Unification unification = Unification
                .testIf(CoreTypes.VOID)
                .isA(CoreTypes.VOID);
        assertTrue(unification.isSuccessful());
    }
}
