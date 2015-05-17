package de.uni.bremen.monty.moco.visitor.typeinf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.TypeContext;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.TypeVariable;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Typed;

public class TypeHelperTest {

    private final ClassType object = CoreClasses.objectType().getType().asClass();
    private final ClassType sub = ClassType.classNamed("sub").withSuperClass(this.object).createType();
    private final ClassType sub1 = ClassType.classNamed("sub1").withSuperClass(this.sub).createType();
    private final ClassType sub11 = ClassType.classNamed("sub11").withSuperClass(this.sub1).createType();
    private final ClassType sub12 = ClassType.classNamed("sub12").withSuperClass(this.sub1).createType();
    private final ClassType sub2 = ClassType.classNamed("sub2").withSuperClass(this.object).createType();
    private final ClassType sub22 = ClassType.classNamed("sub22").withSuperClass(this.sub2).createType();

    private final TypeContext scope = var -> false;

    @Before
    public void setUp() throws Exception {}

    private TypeContext ofFree(TypeVariable...free) {
        final Set<TypeVariable> freeVars = new HashSet<>(Arrays.asList(free));
        return var -> freeVars.contains(var);
    }

    private Set<Type> setOf(Type... types) {
        return new HashSet<>(Arrays.asList(types));
    }

    private Set<Type> setOf(Typed... decl) {
        return Arrays.stream(decl).map(Typed::getType).collect(Collectors.toSet());
    }

    @Test
    public void testCommon2Voids() throws Exception {
        final Set<Type> voids = setOf(CoreClasses.voidType(), CoreClasses.voidType());
        final Optional<Type> opt = TypeHelper.findCommonType(voids, this.scope);
        assertEquals(CoreClasses.voidType().getType(), opt.get());
    }

    @Test
    public void testEmptyVoidWithObject() throws Exception {
        final Set<Type> types = setOf(CoreClasses.voidType(), CoreClasses.objectType());
        final Optional<Type> opt = TypeHelper.findCommonType(types, this.scope);
        assertFalse(opt.isPresent());
    }

    @Test
    public void testIntObjBool() throws Exception {
        final Set<Type> types = setOf(CoreClasses.intType(), CoreClasses.objectType(),
                CoreClasses.boolType());
        final Optional<Type> opt = TypeHelper.findCommonType(types, this.scope);
        assertEquals(CoreClasses.objectType().getType(), opt.get());
    }

    @Test
    public void testIntBoolInt() throws Exception {
        final Set<Type> types = setOf(CoreClasses.intType(), CoreClasses.boolType(),
                CoreClasses.intType());
        final Optional<Type> opt = TypeHelper.findCommonType(types, this.scope);
        assertFalse(opt.isPresent());
    }

    @Test
    public void testBoundVarWithType() throws Exception {
        final TypeVariable boundVar1 = TypeVariable.named("bound1").createType();
        final Optional<Type> opt = TypeHelper.findCommonType(
                setOf(boundVar1, CoreClasses.intType().getType()), this.scope);
        assertFalse(opt.isPresent());
    }

    @Test
    public void test2BoundVars() throws Exception {
        final TypeVariable boundVar1 = TypeVariable.named("bound1").createType();
        final TypeVariable boundVar2 = TypeVariable.named("bound2").createType();
        final Optional<Type> opt = TypeHelper.findCommonType(setOf(boundVar1, boundVar2),
                this.scope);
        assertFalse(opt.isPresent());
    }

    @Test
    public void test2FreeVars() throws Exception {
        final TypeVariable freeVar1 = TypeVariable.named("free1").createType();
        final TypeVariable freeVar2 = TypeVariable.named("free2").createType();
        final TypeContext scope = ofFree(freeVar1, freeVar2);
        final Optional<Type> opt = TypeHelper.findCommonType(setOf(freeVar1, freeVar2),
                scope);
        assertFalse(opt.isPresent());
    }

    @Test
    public void testFreeAndBoundVar() throws Exception {
        final TypeVariable freeVar = TypeVariable.named("free").createType();
        final TypeVariable boundVar = TypeVariable.named("bound").createType();
        final TypeContext scope = ofFree(freeVar);
        final Optional<Type> opt = TypeHelper.findCommonType(setOf(freeVar, boundVar),
                scope);
        assertFalse(opt.isPresent());
    }
}
