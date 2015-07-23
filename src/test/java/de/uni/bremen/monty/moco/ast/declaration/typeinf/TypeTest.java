package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import de.uni.bremen.monty.moco.ast.CoreClasses;

public class TypeTest {

    private final ClassType Object = CoreClasses.objectType().getType().asClass();
    private final ClassType String = CoreClasses.stringType().getType().asClass();
    private final ClassType Int = CoreClasses.intType().getType().asClass();

    private final Product product1 = Product.of(Arrays.asList(this.Int, this.String))
            .createType();
    private final Product product2 = Product.of(Arrays.asList(this.Int, this.Int))
            .createType();

    @Before
    public void setUp() throws Exception {}

    @Test
    public void testProductEquals() throws Exception {
        assertNotEquals(this.product1, this.product2);
        final Product prod = Product.of(Arrays.asList(this.Int, this.Int))
                .createType();
        assertEquals(this.product2, prod);
    }

    @Test
    public void testFunctionEquals() throws Exception {
        final Function top = Function.named("top").returning(this.Object)
                .andParameter(this.Object)
                .andParameter(this.Object)
                .createType();

        final Function fun1 = Function.named("foo").returning(this.String)
                .andParameter(this.Int)
                .andParameter(this.String)
                .quantifiedBy(this.String, this.String)
                .createType();

        final Function fun2 = Function.named("foo").returning(this.String)
                .andParameter(this.Int)
                .andParameter(this.String)
                .quantifiedBy(this.String, this.String)
                .createType();

        assertEquals(fun1, fun2);
        assertTrue(fun1.isA(fun2));

        final Function fun3 = Function.named("foo").returning(this.String)
                .andParameter(this.Int)
                .andParameter(this.String)
                .createType();

        assertFalse(top.isA(fun3));
        assertFalse(top.isA(fun2));
        assertFalse(top.isA(fun1));
        assertTrue(fun1.isA(fun1));
        assertEquals(fun1, fun3);
    }
}
