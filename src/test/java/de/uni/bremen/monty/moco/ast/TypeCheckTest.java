/*
 * moco, the Monty Compiler Copyright (c) 2013-2014, Monty's Coconut, All rights
 * reserved.
 *
 * This file is part of moco, the Monty Compiler.
 *
 * moco is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * moco is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * Linking this program and/or its accompanying libraries statically or
 * dynamically with other modules is making a combined work based on this
 * program. Thus, the terms and conditions of the GNU General Public License
 * cover the whole combination.
 *
 * As a special exception, the copyright holders of moco give you permission to
 * link this programm and/or its accompanying libraries with independent modules
 * to produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting executable
 * under terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that module.
 *
 * An independent module is a module which is not derived from or based on this
 * program and/or its accompanying libraries. If you modify this library, you
 * may extend this exception to your version of the program or library, but you
 * are not obliged to do so. If you do not wish to do so, delete this exception
 * statement from your version.
 *
 * You should have received a copy of the GNU General Public License along with
 * this library.
 */
package de.uni.bremen.monty.moco.ast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.uni.bremen.monty.moco.ast.declaration.ClassDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.Declaration;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ModuleDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeInstantiation;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration.DeclarationType;
import de.uni.bremen.monty.moco.ast.expression.ConditionalExpression;
import de.uni.bremen.monty.moco.ast.expression.Expression;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;
import de.uni.bremen.monty.moco.ast.expression.MemberAccess;
import de.uni.bremen.monty.moco.ast.expression.VariableAccess;
import de.uni.bremen.monty.moco.ast.expression.literal.BooleanLiteral;
import de.uni.bremen.monty.moco.ast.expression.literal.FloatLiteral;
import de.uni.bremen.monty.moco.ast.expression.literal.IntegerLiteral;
import de.uni.bremen.monty.moco.ast.expression.literal.StringLiteral;
import de.uni.bremen.monty.moco.ast.statement.Assignment;
import de.uni.bremen.monty.moco.ast.statement.ConditionalStatement;
import de.uni.bremen.monty.moco.ast.statement.ReturnStatement;
import de.uni.bremen.monty.moco.ast.statement.Statement;
import de.uni.bremen.monty.moco.ast.statement.WhileLoop;
import de.uni.bremen.monty.moco.exception.InvalidExpressionException;
import de.uni.bremen.monty.moco.exception.TypeMismatchException;
import de.uni.bremen.monty.moco.exception.UnknownIdentifierException;
import de.uni.bremen.monty.moco.visitor.DeclarationVisitor;
import de.uni.bremen.monty.moco.visitor.SetParentVisitor;
import de.uni.bremen.monty.moco.visitor.typeinf.QuantumTypeResolver3000;

public class TypeCheckTest {

    private SetParentVisitor SPV;
    private DeclarationVisitor DV;
    private QuantumTypeResolver3000 RV;

    private ModuleDeclaration moduleDeclaration;
    private Package aPackage;
    private Block moduleBlock;

    private ClassDeclaration object;
    private ClassDeclaration classDeclaration;

    private VariableDeclaration intVariable;
    private VariableDeclaration floatVariable;
    private VariableDeclaration strVariable;
    private VariableDeclaration booleanVariable;

    private VariableAccess intVariableAccess;
    private VariableAccess floatVariableAccess;
    private VariableAccess booleanVariableAccess;
    private VariableAccess strVariableAccess;

    private IntegerLiteral intLiteral;
    private FloatLiteral floatLiteral;
    private StringLiteral strLiteral;
    private BooleanLiteral booleanLiteral;

    private FunctionDeclaration functionDeclarationIntReturnInt;
    private FunctionDeclaration functionDeclarationStringReturnString;

    private ClassDeclaration classPerson;
    private ClassDeclaration classStudent;

    private VariableDeclaration classPersonVarDecl;
    private VariableDeclaration classStudentVarDecl;

    private ConditionalExpression ConditionStringString;
    private ConditionalExpression ConditionIntString;
    private ConditionalExpression ConditionIntInt;

    private int lineCounter = 0;

    private Position buildPosition() {
        return new Position("TypeCheckTest", this.lineCounter++, 0);
    }

    // helper
    private int counter = 0;

    public Position nextPosition() {
        return new Position("TestFile", this.counter++, 1);
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUpAST() {
        // reset counter
        this.counter = 0;

        this.SPV = new SetParentVisitor();
        this.SPV.setStopOnFirstError(true);

        this.DV = new DeclarationVisitor();
        this.DV.setStopOnFirstError(true);

        this.RV = new QuantumTypeResolver3000();
        this.RV.setStopOnFirstError(true);

        Block classDeclarationBlock = new Block(buildPosition());
        this.classDeclaration =
                new ClassDeclaration(buildPosition(), new Identifier("classDeclaration"),
                        new ArrayList<TypeInstantiation>(), classDeclarationBlock);
        classDeclarationBlock.addDeclaration(new VariableDeclaration(buildPosition(), new Identifier(
                "classVariableDeclaration"), TypeInstantiation.forTypeName("String").create(),
                VariableDeclaration.DeclarationType.ATTRIBUTE));
        classDeclarationBlock.addDeclaration(new ProcedureDeclaration(buildPosition(), new Identifier(
                "classProcedureDeclaration"), new Block(buildPosition()), new ArrayList<VariableDeclaration>()));

        this.intVariable =
                new VariableDeclaration(buildPosition(), new Identifier("intVariable"),
                        TypeInstantiation.forTypeName("Int").create(), VariableDeclaration.DeclarationType.VARIABLE);
        this.floatVariable =
                new VariableDeclaration(buildPosition(), new Identifier("floatVariable"),
                        TypeInstantiation.forTypeName("Float").create(), VariableDeclaration.DeclarationType.VARIABLE);
        this.booleanVariable =
                new VariableDeclaration(buildPosition(), new Identifier("booleanVariable"),
                        TypeInstantiation.forTypeName("Bool").create(), VariableDeclaration.DeclarationType.VARIABLE);
        this.strVariable =
                new VariableDeclaration(buildPosition(), new Identifier("strVariable"),
                        TypeInstantiation.forTypeName("String").create(), VariableDeclaration.DeclarationType.VARIABLE);

        this.intVariableAccess = new VariableAccess(buildPosition(), new ResolvableIdentifier("intVariable"));
        this.floatVariableAccess = new VariableAccess(buildPosition(), new ResolvableIdentifier("floatVariable"));
        this.booleanVariableAccess = new VariableAccess(buildPosition(), new ResolvableIdentifier("booleanVariable"));
        this.strVariableAccess = new VariableAccess(buildPosition(), new ResolvableIdentifier("strVariable"));

        this.intLiteral = new IntegerLiteral(buildPosition(), 1);
        this.floatLiteral = new FloatLiteral(buildPosition(), 0f);
        this.strLiteral = new StringLiteral(buildPosition(), "42");
        this.booleanLiteral = new BooleanLiteral(buildPosition(), false);

        // set up test AST
        this.moduleBlock = new Block(new Position("TESTFILE", 2, 10));
        this.moduleDeclaration =
                new ModuleDeclaration(new Position("TESTFILE", 1, 10), new Identifier("Main"), this.moduleBlock,
                        new ArrayList<Import>());
        this.aPackage = new Package(new Identifier(""));
        this.aPackage.addModule(this.moduleDeclaration);
        Package corePackage = new Package(new Identifier("core"));
        Block block = new Block(new Position());
        for (ClassDeclaration classDeclaration : CoreClasses.getAllCoreClasses()) {
            block.addDeclaration(classDeclaration);
        }

        corePackage.addModule(new ModuleDeclaration(new Position(), new Identifier("CoreClasses"), block,
                Collections.<Import> emptyList()));
        this.aPackage.addSubPackage(corePackage);

        // Function Block Declaration
        Block functionblock = new Block(buildPosition());
        functionblock.addStatement(new ReturnStatement(buildPosition(), new StringLiteral(buildPosition(),
                "StringReturnString Result")));
        ArrayList<VariableDeclaration> params = new ArrayList<VariableDeclaration>();
        params.add(new VariableDeclaration(buildPosition(), new Identifier("a"),
                TypeInstantiation.forTypeName("String").create(),
                DeclarationType.PARAMETER));
        this.functionDeclarationStringReturnString =
                new FunctionDeclaration(buildPosition(), new Identifier("StringReturnString"), functionblock, params,
                        TypeInstantiation.forTypeName("String").create());

        Block functionblock2 = new Block(buildPosition());
        functionblock2.addStatement(new ReturnStatement(buildPosition(), new IntegerLiteral(buildPosition(), 1337)));
        ArrayList<VariableDeclaration> params2 = new ArrayList<VariableDeclaration>();
        params2.add(new VariableDeclaration(buildPosition(), new Identifier("a"),
                TypeInstantiation.forTypeName("Int").create(),
                DeclarationType.PARAMETER));
        this.functionDeclarationIntReturnInt =
                new FunctionDeclaration(buildPosition(), new Identifier("IntReturnInt"), functionblock2, params2,
                        TypeInstantiation.forTypeName("Int").create());

        // Class Declaration Person
        Block declList = new Block(buildPosition());
        declList.addDeclaration(new VariableDeclaration(buildPosition(), new Identifier("name"),
                TypeInstantiation.forTypeName("String").create(), DeclarationType.ATTRIBUTE));
        declList.addDeclaration(new VariableDeclaration(buildPosition(), new Identifier("age"),
                TypeInstantiation.forTypeName("String").create(), DeclarationType.ATTRIBUTE));
        this.classPerson =
                new ClassDeclaration(buildPosition(), new Identifier("Person"), new ArrayList<TypeInstantiation>(),
                        declList);

        // Class Declaration Person
        Block declList2 = new Block(buildPosition());
        declList2.addDeclaration(new VariableDeclaration(buildPosition(), new Identifier("name"),
                TypeInstantiation.forTypeName("String").create(), DeclarationType.ATTRIBUTE));
        declList2.addDeclaration(new VariableDeclaration(buildPosition(), new Identifier("age"),
                TypeInstantiation.forTypeName("String").create(), DeclarationType.ATTRIBUTE));
        this.classStudent =
                new ClassDeclaration(buildPosition(), new Identifier("Student"), new ArrayList<TypeInstantiation>(),
                        declList2);

        this.classPersonVarDecl =
                new VariableDeclaration(buildPosition(), new Identifier("myPerson"),
                        TypeInstantiation.forTypeName("Person").create(), DeclarationType.VARIABLE);
        this.classStudentVarDecl =
                new VariableDeclaration(buildPosition(), new Identifier("myStudent"),
                        TypeInstantiation.forTypeName("Student").create(), DeclarationType.VARIABLE);

        this.ConditionStringString = new ConditionalExpression(buildPosition(), this.booleanLiteral, this.strLiteral, this.strLiteral);

        this.ConditionIntString = new ConditionalExpression(buildPosition(), this.booleanLiteral, this.intLiteral, this.strLiteral);

        this.ConditionIntInt = new ConditionalExpression(buildPosition(), this.booleanLiteral, this.intLiteral, this.intLiteral);

    }

    @Test
    public void setUpASTTest() {
        assertNotNull("SPV is null", this.SPV);
        assertNotNull("DV is null", this.DV);
        assertNotNull("RV is null", this.RV);

        assertNotNull("package is null", this.aPackage);
        assertNotNull("moduleDeclaration is null", this.moduleDeclaration);
        assertNotNull("moduleBlock is null", this.moduleBlock);

        assertNotNull("intVariable is null", this.intVariable);
        assertNotNull("floatVariable is null", this.floatVariable);
        assertNotNull("strVariable is null", this.strVariable);
        assertNotNull("booleanVariable is null", this.booleanVariable);

        assertNotNull("intVariableAccess is null", this.intVariableAccess);
        assertNotNull("floatVariableAccess is null", this.floatVariableAccess);
        assertNotNull("booleanVariableAccess is null", this.booleanVariableAccess);
        assertNotNull("strVariableAccess is null", this.strVariableAccess);

        assertNotNull("intLiteral is null", this.intLiteral);
        assertNotNull("floatLiteral is null", this.floatLiteral);
        assertNotNull("strLiteral is null", this.strLiteral);
        assertNotNull("booleanLiteral is null", this.booleanLiteral);
    }

    // TYPE COMPATIBILITY

    @Test
    public void typeEqualityTest() {
        for (TypeDeclaration type1 : CoreClasses.getAllCoreClasses()) {
            for (TypeDeclaration type2 : CoreClasses.getAllCoreClasses()) {
                if (type2 != CoreClasses.objectType()) {
                    if (type1 == type2 || type2 == CoreClasses.objectType()) {
                        assertTrue(String.format(
                                "%s does not match %s",
                                type1.getIdentifier().getSymbol(),
                                type2.getIdentifier().getSymbol()), type1.matchesType(type2));
                    } else {
                        assertFalse(String.format(
                                "%s does match %s",
                                type1.getIdentifier().getSymbol(),
                                type2.getIdentifier().getSymbol()), type1.matchesType(type2));
                    }
                }
            }
        }
    }

    // LITERAL COMPATIBILITY

    @Test
    public void literalCompatibilityTest() {
        Assignment intAssignment = new Assignment(buildPosition(), this.intVariableAccess, this.intLiteral);
        Assignment floatAssignment = new Assignment(buildPosition(), this.floatVariableAccess, this.floatLiteral);
        Assignment booleanAssignment = new Assignment(buildPosition(), this.booleanVariableAccess, this.booleanLiteral);
        Assignment strAssignment = new Assignment(buildPosition(), this.strVariableAccess, this.strLiteral);

        this.moduleBlock.addDeclaration(this.intVariable);
        this.moduleBlock.addDeclaration(this.floatVariable);
        this.moduleBlock.addDeclaration(this.booleanVariable);
        this.moduleBlock.addDeclaration(this.strVariable);
        this.moduleBlock.addStatement(intAssignment);
        this.moduleBlock.addStatement(floatAssignment);
        this.moduleBlock.addStatement(booleanAssignment);
        this.moduleBlock.addStatement(strAssignment);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    // LITERAL INCOMPATIBILITY

    @Test(expected = TypeMismatchException.class)
    public void literalIncompatibilityTestIntFloat() {
        Assignment intAssignment = new Assignment(buildPosition(), this.intVariableAccess, this.floatLiteral);

        this.moduleBlock.addDeclaration(this.intVariable);
        this.moduleBlock.addStatement(intAssignment);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void literalIncompatibilityTestIntBool() {
        Assignment intAssignment = new Assignment(buildPosition(), this.intVariableAccess, this.booleanLiteral);

        this.moduleBlock.addDeclaration(this.intVariable);
        this.moduleBlock.addStatement(intAssignment);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void literalIncompatibilityTestIntStr() {
        Assignment intAssignment = new Assignment(buildPosition(), this.intVariableAccess, this.strLiteral);

        this.moduleBlock.addDeclaration(this.intVariable);
        this.moduleBlock.addStatement(intAssignment);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void literalIncompatibilityTestFloatInt() {
        Assignment floatAssignment = new Assignment(buildPosition(), this.floatVariableAccess, this.intLiteral);

        this.moduleBlock.addDeclaration(this.floatVariable);
        this.moduleBlock.addStatement(floatAssignment);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void literalIncompatibilityTestFloatBool() {
        Assignment floatAssignment = new Assignment(buildPosition(), this.floatVariableAccess, this.booleanLiteral);

        this.moduleBlock.addDeclaration(this.floatVariable);
        this.moduleBlock.addStatement(floatAssignment);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void literalIncompatibilityTestFloatStr() {
        Assignment floatAssignment = new Assignment(buildPosition(), this.floatVariableAccess, this.strLiteral);

        this.moduleBlock.addDeclaration(this.floatVariable);
        this.moduleBlock.addStatement(floatAssignment);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void literalIncompatibilityTestBoolInt() {
        Assignment booleanAssignment = new Assignment(buildPosition(), this.booleanVariableAccess, this.intLiteral);

        this.moduleBlock.addDeclaration(this.booleanVariable);
        this.moduleBlock.addStatement(booleanAssignment);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void literalIncompatibilityTestBoolFloat() {
        Assignment booleanAssignment = new Assignment(buildPosition(), this.booleanVariableAccess, this.floatLiteral);

        this.moduleBlock.addDeclaration(this.booleanVariable);
        this.moduleBlock.addStatement(booleanAssignment);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void literalIncompatibilityTestBoolStr() {
        Assignment booleanAssignment = new Assignment(buildPosition(), this.booleanVariableAccess, this.strLiteral);

        this.moduleBlock.addDeclaration(this.booleanVariable);
        this.moduleBlock.addStatement(booleanAssignment);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void literalIncompatibilityTestStrInt() {
        Assignment strAssignment = new Assignment(buildPosition(), this.strVariableAccess, this.intLiteral);

        this.moduleBlock.addDeclaration(this.strVariable);
        this.moduleBlock.addStatement(strAssignment);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void literalIncompatibilityTestStrFloat() {
        Assignment strAssignment = new Assignment(buildPosition(), this.strVariableAccess, this.floatLiteral);

        this.moduleBlock.addDeclaration(this.strVariable);
        this.moduleBlock.addStatement(strAssignment);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void literalIncompatibilityTestStrBool() {
        Assignment strAssignment = new Assignment(buildPosition(), this.strVariableAccess, this.booleanLiteral);

        this.moduleBlock.addDeclaration(this.strVariable);
        this.moduleBlock.addStatement(strAssignment);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    // ASSIGNMENTS
    // => Each testcase has to include a well-typed scenario (with a correctly
    // typed left and right side), as well as an ill-typed scenario.

    private Object[] makeVarAccessVarAccessAssignment(String LeftResolvableSymbol, String RightResolvableSymbol) {
		/*
		 * Makes a the declarations and VariableAccess objects needed for an variable access = variable access
		 * AssignmentTest01 and appends it to the Block
		 */
		VariableDeclaration v1 =
		        new VariableDeclaration(buildPosition(), new Identifier("var1"),
		                TypeInstantiation.forTypeName(LeftResolvableSymbol).create(),
		                DeclarationType.VARIABLE);
		this.moduleBlock.addDeclaration(v1);
		VariableDeclaration v2 =
		        new VariableDeclaration(buildPosition(), new Identifier("var2"),
		                TypeInstantiation.forTypeName(RightResolvableSymbol).create(),
		                DeclarationType.VARIABLE);
		this.moduleBlock.addDeclaration(v2);
		VariableAccess val = new VariableAccess(buildPosition(), new ResolvableIdentifier("var1"));

		VariableAccess var = new VariableAccess(buildPosition(), new ResolvableIdentifier("var2"));

		Assignment a = new Assignment(buildPosition(), val, var);
		this.moduleBlock.addStatement(a);
		Object[] result = { v1, v2, a };
		return result;
	}

    @Test
    public void assignmentTest01_1() {
        // variable access = variable access

        Object[] generated = makeVarAccessVarAccessAssignment("String", "String");
        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);

        Scope bs = this.moduleBlock.getScope();
        assertNotNull("Scope of module is null, but should be anything else!", this.moduleDeclaration.getScope());
        assertNotNull("Scope of moduleBlock is null, but should be anything else!", bs);

        assertEquals(
                "Declaration doesn't match with the orginal one.",
                this.moduleBlock.getDeclarations().get(0),
                generated[0]);
        assertEquals(
                "Declaration doesn't match with the orginal one.",
                this.moduleBlock.getDeclarations().get(1),
                generated[1]);

        for (Declaration dc : this.moduleBlock.getDeclarations()) {
            Identifier obj_id = dc.getIdentifier();
            Scope obj_s = dc.getScope();
            assertNotNull("Identifiert shouldn't be null.", obj_id);
            assertNotNull("Scope shouldn't be null.", obj_s);
            assertEquals("One var declaration is not in the scope of the moduleBlock.", obj_s, bs);
        }

        ArrayList<Statement> statements = (ArrayList<Statement>) this.moduleBlock.getStatements();

        Assignment assignment = null;
        if (statements.get(0) instanceof Assignment) {
            assignment = (Assignment) statements.get(0);
        } else {
            fail("Statement is not an AssignmentStatement.");
        }

        assertEquals("Statement doesn't match with the orginal one.", statements.get(0), generated[2]);

        if (!(assignment.getLeft() instanceof VariableAccess) &&
            !(assignment.getRight() instanceof VariableAccess)) {
            fail("Left or Right of assignment are not from type VariableAccess");
        }
        VariableAccess left = (VariableAccess) assignment.getLeft();
        VariableAccess right = (VariableAccess) assignment.getRight();

        assertEquals("Assignment is not in the moduleBlock scope", assignment.getScope(), bs);
        assertNotNull("Type of left Expression is null, but should be string.", left.getType());
        assertNotNull("Type of right Expression is null, but should be string.", right.getType());
        assertEquals("Type of left Expression is not String", left.getType(), CoreClasses.stringType());
        assertEquals("Type of right Expression is not String", right.getType(), CoreClasses.stringType());
    }

    @Test
    public void assignmentTest01_2() {
        // variable access = variable access
        // Ill-Type Test #1
        this.exception.expect(TypeMismatchException.class);
        makeVarAccessVarAccessAssignment("String", "Int");
        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test
    public void assignmentTest01_3() {
        // variable access = variable access
        // Ill-Type Test #2
        this.exception.expect(TypeMismatchException.class);
        makeVarAccessVarAccessAssignment("Float", "String");
        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test
    public void assignmentTest01_4() {
        // variable access = variable access
        // Ill-Type Test #3
        this.exception.expect(TypeMismatchException.class);
        makeVarAccessVarAccessAssignment("Int", "Float");
        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    private void makeVarAccessMemberAccessAssignment(String LeftResolvableSymbol,
            boolean otherWay) {
        /*
         * Makes a the declarations and Access objects needed for an variable access = member access AssignmentTest02
         * and appends it to the Blockclass
         */

        // Declaration of a class with a member as attribute.

        this.moduleBlock.addDeclaration(this.classPerson);

        // Declaration of a variable
        VariableDeclaration v1 =
                new VariableDeclaration(buildPosition(), new Identifier("var1"),
                        TypeInstantiation.forTypeName(LeftResolvableSymbol).create(),
                        DeclarationType.VARIABLE);
        this.moduleBlock.addDeclaration(v1);

        VariableAccess val = new VariableAccess(buildPosition(), new ResolvableIdentifier("var1"));
        VariableAccess classvar = new VariableAccess(buildPosition(), new ResolvableIdentifier("Person"));
        VariableAccess classaccess = new VariableAccess(buildPosition(), new ResolvableIdentifier("name"));

        MemberAccess mar = new MemberAccess(buildPosition(), classvar, classaccess);
        Assignment a;
        if (otherWay) {
            a = new Assignment(buildPosition(), mar, val);
        } else {
            a = new Assignment(buildPosition(), val, mar);
        }
        this.moduleBlock.addStatement(a);
    }

    @Test
    public void assignmentTest02_2() {
        // variable access = member access
        // Ill-Type Test #1
        this.exception.expect(TypeMismatchException.class);
        makeVarAccessMemberAccessAssignment("Float", false);
        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test
    public void assignmentTest02_3() {
        // variable access = member access
        // Ill-Type Test #2
        this.exception.expect(TypeMismatchException.class);
        makeVarAccessMemberAccessAssignment("Int", false);
        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test
    public void assignmentTest02_4() {
        // variable access = member access
        // Ill-Type Test #3
        this.exception.expect(TypeMismatchException.class);
        makeVarAccessMemberAccessAssignment("Bool", false);
        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    private void variableAccessConditionalExpression_helper(boolean otherway) {
        this.moduleBlock.addDeclaration(this.strVariable);
        Assignment a =
                (otherway)
                        ? new Assignment(buildPosition(), this.strVariableAccess, this.ConditionStringString)
                        : new Assignment(
                                buildPosition(), this.ConditionStringString, this.strVariableAccess);
        this.moduleBlock.addStatement(a);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);

        Statement s = this.moduleBlock.getStatements().get(0);

        assertEquals(
                "Statement doesn't match with the orginal one!",
                this.moduleBlock.getStatements().get(0),
                s);

        Scope mbs = this.moduleBlock.getScope();
        assertEquals(
                "ConditionalExpression scope doesn't match with block scope!",
                mbs,
                this.ConditionStringString.getScope());
        assertEquals("Assignment scope doesn't match with block scope!", mbs, a.getScope());
        assertEquals("booleanLiteral scope doesn't match with block scope!", mbs, this.booleanLiteral.getScope());
        assertEquals("strLiteral scope doesn't match with block scope!", mbs, this.strLiteral.getScope());
        assertEquals("strVariableAccess scope doesn't match with block scope!", mbs, this.strVariableAccess.getScope());
        assertEquals("strVariable scope doesn't match with block scope!", mbs, this.strVariable.getScope());
        VariableAccess v = (VariableAccess) a.getLeft();
        assertTrue("VariableAccess Left Expression is not marked as L-Value", v.getLValue());
        assertTrue(
                "Type of Left Expression from VariableAccess is not string!",
                v.getType() == CoreClasses.stringType().getType());

    }

    @Test
    public void assignmentTest03_1() {
        // variable access = conditional expression
        variableAccessConditionalExpression_helper(true);
    }

    @Test
    public void assignmentTest03_2() {
        // variable access = conditional expression
        // Ill-Type Test #1
        this.exception.expect(TypeMismatchException.class);
        this.moduleBlock.addDeclaration(this.strVariable);
        Assignment a = new Assignment(buildPosition(), this.strVariableAccess, this.ConditionIntInt);
        this.moduleBlock.addStatement(a);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test
    public void assignmentTest03_3() {
        // variable access = conditional expression
        // Ill-Type Test #2
        this.exception.expect(TypeMismatchException.class);
        ConditionalExpression CE =
                new ConditionalExpression(buildPosition(), this.booleanLiteral, this.floatLiteral, this.floatLiteral);
        this.moduleBlock.addDeclaration(this.strVariable);
        Assignment a = new Assignment(buildPosition(), this.strVariableAccess, CE);
        this.moduleBlock.addStatement(a);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test
    public void assignmentTest03_4() {
        // conditional expression = variable access (is not allowed)
        this.exception.expect(InvalidExpressionException.class);
        variableAccessConditionalExpression_helper(false);
    }

    private FunctionCall VariableAccessFunctionCallHelper() {
        // build function with two parameters ( both int) and return type int:
        Block functionblock = new Block(buildPosition());
        functionblock.addStatement(new ReturnStatement(buildPosition(), new IntegerLiteral(buildPosition(), 1337)));
        ArrayList<VariableDeclaration> params = new ArrayList<VariableDeclaration>();
        params.add(new VariableDeclaration(buildPosition(), new Identifier("a"), TypeInstantiation.forTypeName("Int").create(),
                DeclarationType.PARAMETER));
        params.add(new VariableDeclaration(buildPosition(), new Identifier("b"), TypeInstantiation.forTypeName("Int").create(),
                DeclarationType.PARAMETER));
        FunctionDeclaration fd =
                new FunctionDeclaration(buildPosition(), new Identifier("testfunction"), functionblock, params,
                        TypeInstantiation.forTypeName("Int").create());
        this.moduleBlock.addDeclaration(fd);
        // Make a list with given parameters:
        ArrayList<Expression> values = new ArrayList<Expression>();
        values.add(this.intLiteral);
        values.add(this.intLiteral);
        return new FunctionCall(buildPosition(), new ResolvableIdentifier("testfunction"), values);
    }

    @Test
    public void assignmentTest04_1() {
        // variable access = function call
        this.moduleBlock.addDeclaration(this.intVariable);
        Assignment a = new Assignment(buildPosition(), this.intVariableAccess, VariableAccessFunctionCallHelper());
        this.moduleBlock.addStatement(a);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);

    }

    @Test
    public void assignmentTest04_2() {
        // variable access = function call
        // Ill-Type Test #1 (Int return on a string variable)
        this.exception.expect(TypeMismatchException.class);
        this.moduleBlock.addDeclaration(this.strVariable);
        Assignment a = new Assignment(buildPosition(), this.strVariableAccess, VariableAccessFunctionCallHelper());
        this.moduleBlock.addStatement(a);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test
    public void assignmentTest04_3() {
        // variable access = function call
        // (Int return on a float variable) is not allowed
        this.exception.expect(TypeMismatchException.class);
        this.moduleBlock.addDeclaration(this.floatVariable);
        Assignment a = new Assignment(buildPosition(), this.floatVariableAccess, VariableAccessFunctionCallHelper());
        this.moduleBlock.addStatement(a);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);

    }

    @Test
    public void assignmentTest04_4() {
        // variable access = function call
        // Ill-Type Test #3(Int return on a boolean variable)
        this.exception.expect(TypeMismatchException.class);
        this.moduleBlock.addDeclaration(this.booleanVariable);
        Assignment a = new Assignment(buildPosition(), this.booleanVariableAccess, VariableAccessFunctionCallHelper());
        this.moduleBlock.addStatement(a);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test
    public void assignmentTest05_2() {
        // member access = variable access
        // Ill-Type Test #1
        this.exception.expect(TypeMismatchException.class);
        makeVarAccessMemberAccessAssignment("Float", true);
        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test
    public void assignmentTest05_3() {
        // member access = variable access
        // Ill-Type Test #2
        this.exception.expect(TypeMismatchException.class);
        makeVarAccessMemberAccessAssignment("Int", true);
        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test
    public void assignmentTest05_4() {
        // member access = variable access
        // Ill-Type Test #3
        this.exception.expect(TypeMismatchException.class);
        makeVarAccessMemberAccessAssignment("Bool", true);
        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    private void makeMemberAccessMemberAccessAssignment() {
        /*
         * Makes a the declarations and Access objects needed for an member access = member access AssignmentTest06 and
         * appends it to the Block
         */

        this.moduleBlock.addDeclaration(this.classStudent);
        this.moduleBlock.addDeclaration(this.classPerson);

        // Declaration of variables myPerson and myStudent
        this.moduleBlock.addDeclaration(this.classPersonVarDecl);
        this.moduleBlock.addDeclaration(this.classStudentVarDecl);

        // Create VariableAccess objects.
        VariableAccess classvar = new VariableAccess(buildPosition(), new ResolvableIdentifier("myPerson"));
        VariableAccess classvar2 = new VariableAccess(buildPosition(), new ResolvableIdentifier("myStudent"));

        // Make the Assignment with from two MemberAccesses.
        MemberAccess ma1 =
                new MemberAccess(buildPosition(), classvar, new VariableAccess(buildPosition(),
                        new ResolvableIdentifier("name")));
        MemberAccess ma2 =
                new MemberAccess(buildPosition(), classvar2, new VariableAccess(buildPosition(),
                        new ResolvableIdentifier("name")));
        Assignment a = new Assignment(buildPosition(), ma1, ma2);
        this.moduleBlock.addStatement(a);

    }

    @Test
    public void assignmentTest06() {
        // member access = member access
        makeMemberAccessMemberAccessAssignment();

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test
    public void assignmentTest07() {
        // member access = conditional expression
        this.moduleBlock.addDeclaration(this.classStudent);
        this.moduleBlock.addDeclaration(this.classStudentVarDecl);

        VariableAccess classvar = new VariableAccess(buildPosition(), new ResolvableIdentifier("myStudent"));

        MemberAccess ma =
                new MemberAccess(buildPosition(), classvar, new VariableAccess(buildPosition(),
                        new ResolvableIdentifier("name")));
        Assignment a = new Assignment(buildPosition(), ma, this.ConditionStringString);
        this.moduleBlock.addStatement(a);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);

        Scope sc = a.getScope();
        assertNotNull(sc);
        assertEquals(sc, this.moduleBlock.getScope());
        assertTrue(a.getRight() instanceof ConditionalExpression);
        assertTrue(a.getLeft() instanceof MemberAccess);
        assertTrue(a.getLeft().getType().equals(a.getRight().getType()));
    }

    @Test
    public void assignmentTest08() {
        // member access = function call

        this.moduleBlock.addDeclaration(this.classStudent);
        this.moduleBlock.addDeclaration(this.classStudentVarDecl);
        this.moduleBlock.addDeclaration(this.functionDeclarationStringReturnString);

        VariableAccess classvar = new VariableAccess(buildPosition(), new ResolvableIdentifier("myStudent"));

        MemberAccess ma =
                new MemberAccess(buildPosition(), classvar, new VariableAccess(buildPosition(),
                        new ResolvableIdentifier("name")));

        ArrayList<Expression> values = new ArrayList<Expression>();
        values.add(this.strLiteral);
        FunctionCall fc = new FunctionCall(buildPosition(), new ResolvableIdentifier("StringReturnString"), values);

        Assignment a = new Assignment(buildPosition(), ma, fc);
        this.moduleBlock.addStatement(a);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);

        Scope sc = a.getScope();
        assertNotNull(sc);
        assertEquals(sc, this.moduleBlock.getScope());
        assertTrue(a.getLeft() instanceof MemberAccess);
        assertTrue(a.getRight() instanceof FunctionCall);
        assertTrue(a.getLeft().getType().equals(a.getRight().getType()));
    }

    @Test
    public void assignmentTest09() {
        // function call = variable access
        this.exception.expect(InvalidExpressionException.class);
        this.moduleBlock.addDeclaration(this.intVariable);
        Assignment a = new Assignment(buildPosition(), VariableAccessFunctionCallHelper(), this.intVariableAccess);
        this.moduleBlock.addStatement(a);
        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    // MEMBER ACCESS

    @Test(expected = UnknownIdentifierException.class)
    public void memberAccessTest01() {
        // x 1: left ist keine classDeclaration
        FunctionDeclaration functionDeclaration =
                new FunctionDeclaration(buildPosition(), new Identifier("functionDeclaration"), new Block(
                        buildPosition()), new ArrayList<VariableDeclaration>(),
                        TypeInstantiation.forTypeName("String").create());

        VariableAccess varAccess = new VariableAccess(buildPosition(), new ResolvableIdentifier("functionDeclaration"));

        MemberAccess memberAccess = new MemberAccess(buildPosition(), varAccess, this.intVariableAccess);

        WhileLoop loop = new WhileLoop(buildPosition(), memberAccess, new Block(buildPosition()));

        this.moduleBlock.addDeclaration(this.intVariable);
        this.moduleBlock.addDeclaration(functionDeclaration);
        this.moduleBlock.addStatement(loop);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void memberAccessTest02() {
        // x 2: left löst nach classDeclaration auf, right ist conditional
        ConditionalExpression conditionalExpression =
                new ConditionalExpression(buildPosition(), this.booleanLiteral, this.strLiteral, this.strLiteral);

        VariableAccess varAccess = new VariableAccess(buildPosition(), new ResolvableIdentifier("classDeclaration"));

        MemberAccess memberAccess = new MemberAccess(buildPosition(), varAccess, conditionalExpression);

        WhileLoop loop = new WhileLoop(buildPosition(), memberAccess, new Block(buildPosition()));

        this.moduleBlock.addDeclaration(this.classDeclaration);
        this.moduleBlock.addStatement(loop);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void memberAccessTest03() {
        // x 3: left löst nach classDeclaration auf, right ist boolean
        VariableAccess varAccess = new VariableAccess(buildPosition(), new ResolvableIdentifier("classDeclaration"));

        MemberAccess memberAccess = new MemberAccess(buildPosition(), varAccess, this.booleanLiteral);

        WhileLoop loop = new WhileLoop(buildPosition(), memberAccess, new Block(buildPosition()));

        this.moduleBlock.addDeclaration(this.classDeclaration);
        this.moduleBlock.addStatement(loop);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void memberAccessTest04() {
        // x 4: left löst nach classDeclaration auf, right ist string
        VariableAccess varAccess = new VariableAccess(buildPosition(), new ResolvableIdentifier("classDeclaration"));

        MemberAccess memberAccess = new MemberAccess(buildPosition(), varAccess, this.strLiteral);

        WhileLoop loop = new WhileLoop(buildPosition(), memberAccess, new Block(buildPosition()));

        this.moduleBlock.addDeclaration(this.classDeclaration);
        this.moduleBlock.addStatement(loop);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void memberAccessTest05() {
        // x 5: left löst nach classDeclaration auf, right ist float
        VariableAccess varAccess = new VariableAccess(buildPosition(), new ResolvableIdentifier("classDeclaration"));

        MemberAccess memberAccess = new MemberAccess(buildPosition(), varAccess, this.floatLiteral);

        WhileLoop loop = new WhileLoop(buildPosition(), memberAccess, new Block(buildPosition()));

        this.moduleBlock.addDeclaration(this.classDeclaration);
        this.moduleBlock.addStatement(loop);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void memberAccessTest06() {
        // x 6: left löst nach classDeclaration auf, right ist int
        VariableAccess varAccess = new VariableAccess(buildPosition(), new ResolvableIdentifier("classDeclaration"));

        MemberAccess memberAccess = new MemberAccess(buildPosition(), varAccess, this.intLiteral);

        WhileLoop loop = new WhileLoop(buildPosition(), memberAccess, new Block(buildPosition()));

        this.moduleBlock.addDeclaration(this.classDeclaration);
        this.moduleBlock.addStatement(loop);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = UnknownIdentifierException.class)
    public void memberAccessTest07() {
        // x 7: left löst nach classDeclaration auf, right ist nicht vorhandener
        // ProcedureCall
        VariableAccess varAccess = new VariableAccess(buildPosition(), new ResolvableIdentifier("classDeclaration"));
        FunctionCall functionCall =
                new FunctionCall(buildPosition(), new ResolvableIdentifier("invalid"), new ArrayList<Expression>());

        MemberAccess memberAccess = new MemberAccess(buildPosition(), varAccess, functionCall);

        WhileLoop loop = new WhileLoop(buildPosition(), memberAccess, new Block(buildPosition()));

        this.moduleBlock.addDeclaration(this.classDeclaration);
        this.moduleBlock.addStatement(loop);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = UnknownIdentifierException.class)
    public void memberAccessTest08() {
        // x 8: left löst nach classDeclaration auf, right ist nicht vorhandener
        // variableAccess
        VariableAccess varAccess = new VariableAccess(buildPosition(), new ResolvableIdentifier("classDeclaration"));
        VariableAccess rightVarAccess = new VariableAccess(buildPosition(), new ResolvableIdentifier("invalid"));

        MemberAccess memberAccess = new MemberAccess(buildPosition(), varAccess, rightVarAccess);

        WhileLoop loop = new WhileLoop(buildPosition(), memberAccess, new Block(buildPosition()));

        this.moduleBlock.addDeclaration(this.classDeclaration);
        this.moduleBlock.addStatement(loop);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void memberAccessTest09() {
        // y 9: left löst nach classDeclaration auf, right ist vorhandener
        // ProcedureCall
        VariableAccess varAccess = new VariableAccess(buildPosition(), new ResolvableIdentifier("classDeclaration"));
        FunctionCall functionCall =
                new FunctionCall(buildPosition(), new ResolvableIdentifier("classProcedureDeclaration"),
                        new ArrayList<Expression>());

        MemberAccess memberAccess = new MemberAccess(buildPosition(), varAccess, functionCall);

        WhileLoop loop = new WhileLoop(buildPosition(), memberAccess, new Block(buildPosition()));

        this.moduleBlock.addDeclaration(this.classDeclaration);
        this.moduleBlock.addStatement(loop);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void memberAccessTest10() {
        // y 10: left löst nach classDeclaration auf, right ist vorhandener
        // variableAccess
        VariableAccess varAccess = new VariableAccess(buildPosition(), new ResolvableIdentifier("classDeclaration"));
        VariableAccess rightVarAccess =
                new VariableAccess(buildPosition(), new ResolvableIdentifier("classVariableDeclaration"));

        MemberAccess memberAccess = new MemberAccess(buildPosition(), varAccess, rightVarAccess);

        WhileLoop loop = new WhileLoop(buildPosition(), memberAccess, new Block(buildPosition()));

        this.moduleBlock.addDeclaration(this.classDeclaration);
        this.moduleBlock.addStatement(loop);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    // FUNCTION CALL
    @Test
    public void functionCallTest() {
        // FunctionCall with float returnType
        Block functionBlock = new Block(nextPosition());
        functionBlock.addStatement(new ReturnStatement(nextPosition(), this.floatLiteral));
        FunctionDeclaration functionDeclaration =
                new FunctionDeclaration(nextPosition(), new Identifier("functionWithReturn"), functionBlock,
                        new ArrayList<VariableDeclaration>(),
                        TypeInstantiation.forTypeName("Float").create());
        FunctionCall procedureCall =
                new FunctionCall(nextPosition(), new ResolvableIdentifier("functionWithReturn"),
                        new ArrayList<Expression>());

        this.moduleBlock.addDeclaration(functionDeclaration);
        this.moduleBlock.addStatement(procedureCall);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test
    public void functionCallWithParamsTest() {
        // Functioncall with int Returntype and matching int Paramters
        List<VariableDeclaration> variables = new ArrayList<VariableDeclaration>();
        variables.add(this.intVariable);
        List<Expression> parameters = new ArrayList<Expression>();
        parameters.add(this.intLiteral);
        Block functionBlock = new Block(nextPosition());
        functionBlock.addStatement(new ReturnStatement(nextPosition(), this.intLiteral));
        FunctionDeclaration functionDeclaration =
                new FunctionDeclaration(nextPosition(), new Identifier("functionWithParameters"), functionBlock,
                        variables, TypeInstantiation.forTypeName("Int").create());
        FunctionCall procedureCall =
                new FunctionCall(nextPosition(), new ResolvableIdentifier("functionWithParameters"), parameters);

        this.moduleBlock.addDeclaration(functionDeclaration);
        this.moduleBlock.addStatement(procedureCall);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void functionCallWithParamsTest02() {
        // Parameters from Declaration and Call doesn't match
        List<VariableDeclaration> variables = new ArrayList<VariableDeclaration>();
        variables.add(this.intVariable);
        List<Expression> parameters = new ArrayList<Expression>();
        parameters.add(this.strLiteral);
        Block functionBlock = new Block(nextPosition());
        functionBlock.addStatement(new ReturnStatement(nextPosition(), this.intLiteral));

        FunctionDeclaration functionDeclaration =
                new FunctionDeclaration(nextPosition(), new Identifier("functionWithParameters02"), functionBlock,
                        variables, TypeInstantiation.forTypeName("Int").create());
        FunctionCall procedureCall =
                new FunctionCall(nextPosition(), new ResolvableIdentifier("functionWithParameters02"), parameters);

        this.moduleBlock.addDeclaration(functionDeclaration);
        this.moduleBlock.addStatement(procedureCall);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void functionCallWithParamsTest03() {
        // Count of paramters in the FunctionCall does not match the declaration
        List<VariableDeclaration> variables = new ArrayList<VariableDeclaration>();
        variables.add(this.intVariable);
        variables.add(this.floatVariable);
        List<Expression> parameters = new ArrayList<Expression>();
        parameters.add(this.intLiteral);
        Block functionBlock = new Block(nextPosition());
        functionBlock.addStatement(new ReturnStatement(nextPosition(), this.intLiteral));

        FunctionDeclaration functionDeclaration =
                new FunctionDeclaration(nextPosition(), new Identifier("functionWithParameters03"), functionBlock,
                        variables, TypeInstantiation.forTypeName("Int").create());
        FunctionCall procedureCall =
                new FunctionCall(nextPosition(), new ResolvableIdentifier("functionWithParameters03"), parameters);

        this.moduleBlock.addDeclaration(functionDeclaration);
        this.moduleBlock.addStatement(procedureCall);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void functionCallWithParamsTest04() {
        // Count of paramters in the FunctionCall does not match the declaration
        List<VariableDeclaration> variables = new ArrayList<VariableDeclaration>();
        variables.add(this.floatVariable);
        List<Expression> parameters = new ArrayList<Expression>();
        parameters.add(this.floatLiteral);
        parameters.add(this.strLiteral);
        Block functionBlock = new Block(nextPosition());
        functionBlock.addStatement(new ReturnStatement(nextPosition(), this.intLiteral));

        FunctionDeclaration functionDeclaration =
                new FunctionDeclaration(nextPosition(), new Identifier("functionWithParameters04"), functionBlock,
                        variables, TypeInstantiation.forTypeName("Int").create());
        FunctionCall procedureCall =
                new FunctionCall(nextPosition(), new ResolvableIdentifier("functionWithParameters04"), parameters);

        this.moduleBlock.addDeclaration(functionDeclaration);
        this.moduleBlock.addStatement(procedureCall);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void functionCallWithParamsTest05() {
        // Count of paramters in the FunctionCall does not match the declaration
        List<VariableDeclaration> variables = new ArrayList<VariableDeclaration>();
        List<Expression> parameters = new ArrayList<Expression>();
        parameters.add(this.floatLiteral);
        parameters.add(this.strLiteral);
        Block functionBlock = new Block(nextPosition());
        functionBlock.addStatement(new ReturnStatement(nextPosition(), this.floatLiteral));

        FunctionDeclaration functionDeclaration =
                new FunctionDeclaration(nextPosition(), new Identifier("functionWithParameters05"), functionBlock,
                        variables, TypeInstantiation.forTypeName("Float").create());
        FunctionCall procedureCall =
                new FunctionCall(nextPosition(), new ResolvableIdentifier("functionWithParameters05"), parameters);

        this.moduleBlock.addDeclaration(functionDeclaration);
        this.moduleBlock.addStatement(procedureCall);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void functionCallWithParamsTest06() {
        // Count of paramters in the FunctionCall does not match the declaration
        List<VariableDeclaration> variables = new ArrayList<VariableDeclaration>();
        List<Expression> parameters = new ArrayList<Expression>();
        parameters.add(this.floatLiteral);
        parameters.add(this.strLiteral);
        Block functionBlock = new Block(nextPosition());
        functionBlock.addStatement(new ReturnStatement(nextPosition(), this.strLiteral));

        FunctionDeclaration functionDeclaration =
                new FunctionDeclaration(nextPosition(), new Identifier("functionWithParameters06"), functionBlock,
                        variables, TypeInstantiation.forTypeName("String").create());
        FunctionCall procedureCall =
                new FunctionCall(nextPosition(), new ResolvableIdentifier("functionWithParameters06"), parameters);

        this.moduleBlock.addDeclaration(functionDeclaration);
        this.moduleBlock.addStatement(procedureCall);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void functionCallWithParamsTest07() {
        // ReturnStatement type doesn't match Return Type in Declaration
        Block functionBlock = new Block(nextPosition());
        functionBlock.addStatement(new ReturnStatement(nextPosition(), this.strLiteral));

        FunctionDeclaration functionDeclaration =
                new FunctionDeclaration(nextPosition(), new Identifier("functionWithParameters06"), functionBlock,
                        new ArrayList<VariableDeclaration>(), TypeInstantiation.forTypeName("Int").create());
        FunctionCall procedureCall =
                new FunctionCall(nextPosition(), new ResolvableIdentifier("functionWithParameters06"),
                        new ArrayList<Expression>());

        this.moduleBlock.addDeclaration(functionDeclaration);
        this.moduleBlock.addStatement(procedureCall);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    // PROCEDURE CALL

    @Test
    public void procedureCallTest() {
        // ProcedureCall with matching Declaration
        ProcedureDeclaration procedureDeclaration =
                new ProcedureDeclaration(nextPosition(), new Identifier("procedure"), new Block(nextPosition()),
                        new ArrayList<VariableDeclaration>());
        FunctionCall procedureCall =
                new FunctionCall(nextPosition(), new ResolvableIdentifier("procedure"), new ArrayList<Expression>());

        this.moduleBlock.addDeclaration(procedureDeclaration);
        this.moduleBlock.addStatement(procedureCall);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test
    public void procedureCallWithParamsTest() {
        // ProcedureCall and Declaration with matching types
        List<VariableDeclaration> variables = new ArrayList<VariableDeclaration>();
        variables.add(this.strVariable);
        List<Expression> parameters = new ArrayList<Expression>();
        parameters.add(this.strLiteral);

        ProcedureDeclaration procedureDeclaration =
                new ProcedureDeclaration(nextPosition(), new Identifier("procedureWithParams"), new Block(
                        nextPosition()), variables);
        FunctionCall procedureCall =
                new FunctionCall(nextPosition(), new ResolvableIdentifier("procedureWithParams"), parameters);

        this.moduleBlock.addDeclaration(procedureDeclaration);
        this.moduleBlock.addStatement(procedureCall);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void procedureCallWithParamsTest02() {
        // Parameters from Declaration and Call doesn't match
        List<VariableDeclaration> variables = new ArrayList<VariableDeclaration>();
        variables.add(this.strVariable);
        List<Expression> parameters = new ArrayList<Expression>();
        parameters.add(this.floatLiteral);

        ProcedureDeclaration procedureDeclaration =
                new ProcedureDeclaration(nextPosition(), new Identifier("procedureWithParams02"), new Block(
                        nextPosition()), variables);
        FunctionCall procedureCall =
                new FunctionCall(nextPosition(), new ResolvableIdentifier("procedureWithParams02"), parameters);

        this.moduleBlock.addDeclaration(procedureDeclaration);
        this.moduleBlock.addStatement(procedureCall);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void procedureCallWithParamsTest03() {
        // Count of paramters in the ProcedureCall does not match the
        // declaration
        List<VariableDeclaration> variables = new ArrayList<VariableDeclaration>();
        variables.add(this.strVariable);
        variables.add(this.intVariable);
        List<Expression> parameters = new ArrayList<Expression>();
        parameters.add(this.strLiteral);

        ProcedureDeclaration procedureDeclaration =
                new ProcedureDeclaration(nextPosition(), new Identifier("procedureWithParams03"), new Block(
                        nextPosition()), variables);
        FunctionCall procedureCall =
                new FunctionCall(nextPosition(), new ResolvableIdentifier("procedureWithParams03"), parameters);

        this.moduleBlock.addDeclaration(procedureDeclaration);
        this.moduleBlock.addStatement(procedureCall);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void procedureCallWithParamsTest04() {
        // Count of paramters in the ProcedureCall does not match the
        // declaration
        List<VariableDeclaration> variables = new ArrayList<VariableDeclaration>();
        variables.add(this.strVariable);
        List<Expression> parameters = new ArrayList<Expression>();
        parameters.add(this.strLiteral);
        parameters.add(this.intLiteral);

        ProcedureDeclaration procedureDeclaration =
                new ProcedureDeclaration(nextPosition(), new Identifier("procedureWithParams04"), new Block(
                        nextPosition()), variables);
        FunctionCall procedureCall =
                new FunctionCall(nextPosition(), new ResolvableIdentifier("procedureWithParams04"), parameters);

        this.moduleBlock.addDeclaration(procedureDeclaration);
        this.moduleBlock.addStatement(procedureCall);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void procedureCallWithParamsTest05() {
        // Count of paramters in the ProcedureCall does not match the
        // declaration
        List<VariableDeclaration> variables = new ArrayList<VariableDeclaration>();
        variables.add(this.strVariable);
        List<Expression> parameters = new ArrayList<Expression>();

        ProcedureDeclaration procedureDeclaration =
                new ProcedureDeclaration(nextPosition(), new Identifier("procedureWithParams05"), new Block(
                        nextPosition()), variables);
        FunctionCall procedureCall =
                new FunctionCall(nextPosition(), new ResolvableIdentifier("procedureWithParams05"), parameters);

        this.moduleBlock.addDeclaration(procedureDeclaration);
        this.moduleBlock.addStatement(procedureCall);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void procedureCallWithParamsTest06() {
        // Count of paramters in the ProcedureCall does not match the
        // declaration
        List<VariableDeclaration> variables = new ArrayList<VariableDeclaration>();
        List<Expression> parameters = new ArrayList<Expression>();
        parameters.add(this.intLiteral);

        ProcedureDeclaration procedureDeclaration =
                new ProcedureDeclaration(nextPosition(), new Identifier("procedureWithParams06"), new Block(
                        nextPosition()), variables);
        FunctionCall procedureCall =
                new FunctionCall(nextPosition(), new ResolvableIdentifier("procedureWithParams06"), parameters);

        this.moduleBlock.addDeclaration(procedureDeclaration);
        this.moduleBlock.addStatement(procedureCall);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    // CONDITIONAL STATEMENT
    @Test
    public void conditionalTestBooleanCondition() {
        ConditionalStatement conditionalStatement =
                new ConditionalStatement(buildPosition(), this.booleanLiteral, new Block(buildPosition()), new Block(
                        buildPosition()));

        this.moduleBlock.addStatement(conditionalStatement);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void conditionalTestNotBooleanCondition() {
        ConditionalStatement conditionalStatement =
                new ConditionalStatement(buildPosition(), this.strLiteral, new Block(buildPosition()), new Block(
                        buildPosition()));

        this.moduleBlock.addStatement(conditionalStatement);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    // CONDITIONAL EXPRESSION

    @Test
    public void conditionalExpressionTestCorrectConditionalExpression() {
        // thenExpression matches elseExpression
        ConditionalExpression conditionalExpression =
                new ConditionalExpression(buildPosition(), this.booleanLiteral, this.strLiteral, this.strLiteral);
        Assignment conditionalAssignment = new Assignment(buildPosition(), this.strVariableAccess, conditionalExpression);

        this.moduleBlock.addDeclaration(this.strVariable);
        this.moduleBlock.addStatement(conditionalAssignment);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void conditionalExpressionTestNotBooleanCondition() {
        ConditionalExpression conditionalExpression =
                new ConditionalExpression(buildPosition(), this.intLiteral, this.strLiteral, this.strLiteral);
        Assignment conditionalAssignment = new Assignment(buildPosition(), this.strVariableAccess, conditionalExpression);

        this.moduleBlock.addDeclaration(this.strVariable);
        this.moduleBlock.addStatement(conditionalAssignment);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }

    @Test(expected = TypeMismatchException.class)
    public void conditionalExpressionTestThenElseMismatches() {
        // thenExpression mismatches elseExpression
        ConditionalExpression conditionalExpression =
                new ConditionalExpression(buildPosition(), this.booleanLiteral, this.strLiteral, this.intLiteral);
        Assignment conditionalAssignment = new Assignment(buildPosition(), this.strVariableAccess, conditionalExpression);

        this.moduleBlock.addDeclaration(this.strVariable);
        this.moduleBlock.addStatement(conditionalAssignment);

        this.SPV.visit(this.aPackage);
        this.DV.visit(this.aPackage);
        this.RV.visit(this.aPackage);
    }
}
