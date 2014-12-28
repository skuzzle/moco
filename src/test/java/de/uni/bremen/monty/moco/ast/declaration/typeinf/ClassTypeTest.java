package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ClassTypeTest {

    private final ClassType object = ClassType.named("Object").createType();
    private final ClassType number = ClassType.named("Number").withSuperClass(this.object).createType();
    private final ClassType Int = ClassType.named("Int").withSuperClass(this.number).createType();
    private final ClassType foo = ClassType.named("foo").withSuperClasses(this.object, this.number).createType();

    @Before
    public void setUp() throws Exception {}

    @Test
    public void testDistanceToObject() throws Exception {
        assertEquals(0, this.object.distanceToObject());
        assertEquals(2, this.Int.distanceToObject());
        assertEquals(1, this.foo.distanceToObject());
    }

    @Test
    public void testIsA() throws Exception {
        assertTrue(this.object.isA(this.object));
        assertTrue(this.number.isA(this.object));
        assertTrue(this.Int.isA(this.object));
        assertTrue(this.Int.isA(this.number));
    }
}
