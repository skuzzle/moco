package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.Position;

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
    
    @Test(expected = IllegalArgumentException.class)
    public void testClassNamedNull() throws Exception {
        ClassType.classNamed((String) null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testClassNamedNull2() throws Exception {
        ClassType.classNamed((Identifier) null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testClassNullLocation() throws Exception {
        ClassType.classNamed("A").atLocation(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testClassNullSuperType() throws Exception {
        ClassType.classNamed("B").withSuperClass((ClassType) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testClassNullSuperType2() throws Exception {
        ClassType.classNamed("B").withSuperClasses((List<ClassType>) null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testTypeParameterNull() throws Exception {
        ClassType.classNamed("A").addTypeParameter((Type[]) null);
    }
    
    @Test
    public void testProductEquals() throws Exception {
        assertNotEquals(this.product1, this.product2);
        final Product prod = Product.of(Arrays.asList(this.Int, this.Int))
                .createType();
        assertEquals(this.product2, prod);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTypeNameNull() throws Exception {
        new Type(null, Position.UNKNOWN_POSITION) {
            
            @Override
            public boolean isA(Type other) {
                return false;
            }
            
            @Override
            public int hashCode() {
                return 0;
            }
            
            @Override
            public boolean equals(Object obj) {
                return false;
            }
            
            @Override
            public int distanceToObject() {
                return 0;
            }
            
            @Override
            Type apply(Unification unification) {
                return null;
            }
        };
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testTypePositionNull() throws Exception {
        new Type(Identifier.of("A"), null) {
            
            @Override
            public boolean isA(Type other) {
                return false;
            }
            
            @Override
            public int hashCode() {
                return 0;
            }
            
            @Override
            public boolean equals(Object obj) {
                return false;
            }
            
            @Override
            public int distanceToObject() {
                return 0;
            }
            
            @Override
            Type apply(Unification unification) {
                return null;
            }
        };
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
