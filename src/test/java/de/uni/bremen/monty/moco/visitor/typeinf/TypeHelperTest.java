package de.uni.bremen.monty.moco.visitor.typeinf;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;

public class TypeHelperTest {

    private final ClassType object = CoreClasses.objectType().getType().asClass();
    private final ClassType sub = ClassType.classNamed("sub").withSuperClass(this.object).createType();
    private final ClassType sub1 = ClassType.classNamed("sub1").withSuperClass(this.sub).createType();
    private final ClassType sub11 = ClassType.classNamed("sub11").withSuperClass(this.sub1).createType();
    private final ClassType sub12 = ClassType.classNamed("sub12").withSuperClass(this.sub1).createType();
    private final ClassType sub2 = ClassType.classNamed("sub2").withSuperClass(this.object).createType();
    private final ClassType sub22 = ClassType.classNamed("sub22").withSuperClass(this.sub2).createType();

    @Before
    public void setUp() throws Exception {}

    @Test
    public void testObjectIsCommon() throws Exception {
        final Type common = TypeHelper.findLeastCommonSuperType(Arrays.asList(
                this.sub1, this.sub2, this.sub22)).get();
        assertEquals(this.object, common);
    }

    @Test
    public void testSub1IsCommon() throws Exception {
        final Type common = TypeHelper.findLeastCommonSuperType(Arrays.asList(
                this.sub1, this.sub11, this.sub12)).get();
        assertEquals(this.sub1, common);
    }

    @Test
    public void testSingleVoid() throws Exception {
        final Type common = TypeHelper.findLeastCommonSuperType(
                Arrays.asList(CoreClasses.voidType().getType())).get();
        assertEquals(CoreClasses.voidType().getType(), common);
    }

    @Test
    public void testMultipleVoid() throws Exception {
        final Type common = TypeHelper.findLeastCommonSuperType(Arrays.asList(
                CoreClasses.voidType().getType(), CoreClasses.voidType().getType())).get();
        assertEquals(CoreClasses.voidType().getType(), common);
    }
}
