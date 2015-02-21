package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ClassTypeTest {

    private final ClassType object = ClassType.classNamed("Object").createType();
    private final ClassType number = ClassType.classNamed("Number").withSuperClass(this.object).createType();
    private final ClassType Int = ClassType.classNamed("Int").withSuperClass(this.number).createType();
    private final ClassType foo = ClassType.classNamed("foo").withSuperClasses(this.object, this.number).createType();

    @Before
    public void setUp() throws Exception {}

    @Test
    public void testDistanceToObject() throws Exception {
        assertEquals(0, this.object.distanceToObject());
        assertEquals(2, this.Int.distanceToObject());
        assertEquals(1, this.foo.distanceToObject());
    }
}
