package de.uni.bremen.monty.moco.visitor.typeinf;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.CoreTypes;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;

public class TypeHelperTest {

    private final ClassType object = (ClassType) CoreTypes.get("Object");
    private final ClassType sub = ClassType.named("sub").withSuperClass(this.object).createType();
    private final ClassType sub1 = ClassType.named("sub1").withSuperClass(this.sub).createType();
    private final ClassType sub11 = ClassType.named("sub11").withSuperClass(this.sub1).createType();
    private final ClassType sub12 = ClassType.named("sub12").withSuperClass(this.sub1).createType();
    private final ClassType sub2 = ClassType.named("sub2").withSuperClass(this.object).createType();
    private final ClassType sub22 = ClassType.named("sub22").withSuperClass(this.sub2).createType();

    @Before
    public void setUp() throws Exception {}

    @Test
    public void testObjectIsCommon() throws Exception {
        final Type common = TypeHelper.findCommonSuperType(Arrays.asList(
                this.sub1, this.sub2, this.sub22));
        assertEquals(this.object, common);
    }

    @Test
    public void testSub1IsCommon() throws Exception {
        final Type common = TypeHelper.findCommonSuperType(Arrays.asList(
                this.sub1, this.sub11, this.sub12));
        assertEquals(this.sub1, common);
    }
}
