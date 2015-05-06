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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.uni.bremen.monty.moco.ast.declaration.ClassDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ModuleDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeInstantiation;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.expression.Expression;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;
import de.uni.bremen.monty.moco.ast.expression.MemberAccess;
import de.uni.bremen.monty.moco.ast.expression.VariableAccess;
import de.uni.bremen.monty.moco.ast.expression.literal.BooleanLiteral;
import de.uni.bremen.monty.moco.ast.expression.literal.StringLiteral;
import de.uni.bremen.monty.moco.ast.statement.Assignment;
import de.uni.bremen.monty.moco.ast.statement.BreakStatement;
import de.uni.bremen.monty.moco.ast.statement.ConditionalStatement;
import de.uni.bremen.monty.moco.ast.statement.ContinueStatement;
import de.uni.bremen.monty.moco.ast.statement.ReturnStatement;
import de.uni.bremen.monty.moco.ast.statement.WhileLoop;
import de.uni.bremen.monty.moco.exception.InvalidPlaceToDeclareException;
import de.uni.bremen.monty.moco.exception.RedeclarationException;
import de.uni.bremen.monty.moco.visitor.DeclarationVisitor;
import de.uni.bremen.monty.moco.visitor.SetParentVisitor;

/**
 * Test the DeclarationVisitor.
 * <p>
 * Note the following limitations! This does not test: <br>
 * - Overriding <br>
 * - Overloading <br>
 * - Inheritance with multiple super-classes <br>
 * - Access-modifiers
 */
public class DeclarationTest {

    private SetParentVisitor setParentVisitor;
    private DeclarationVisitor declarationVisitor;
    private int lineCounter = 0;

    // module
    private Import moduleImport01;
    private Import moduleImport02;
    private List<Import> moduleImports;
    private Block moduleBlock;
    private Package aPackage;
    private ModuleDeclaration moduleDeclaration;

    // module class
    private Block classBlock;
    private ClassDeclaration classDeclaration;
    private VariableDeclaration classVariableDeclaration;

    private Block classFunctionBlock;
    private List<VariableDeclaration> classFunctionParams;
    private FunctionDeclaration classFunctionDeclaration;

    private Block classProcedureBlock;
    private List<VariableDeclaration> classProcedureParams;
    private ProcedureDeclaration classProcedureDeclaration;

    // module class extends
    private Block extendedClassBlock;
    private ClassDeclaration extendedClassDeclaration;

    // module function
    private Block moduleFunctionBlock;
    private VariableDeclaration moduleFunctionParamsVariable01;
    private VariableDeclaration moduleFunctionParamsVariable02;
    private List<VariableDeclaration> moduleFunctionParams;
    private FunctionDeclaration moduleFunctionDeclaration;
    private VariableDeclaration moduleFunctionVariableDeclaration;
    private Block moduleFunctionFunctionBlock;
    private List<VariableDeclaration> moduleFunctionFunctionParams;
    private FunctionDeclaration moduleFunctionFunctionDeclaration;
    private Block moduleFunctionProcedureBlock;
    private List<VariableDeclaration> moduleFunctionProcedureParams;
    private ProcedureDeclaration moduleFunctionProcedureDeclaration;

    // module procedure
    private Block moduleProcedureBlock;
    private VariableDeclaration moduleProcedureParamsVariable01;
    private VariableDeclaration moduleProcedureParamsVariable02;
    private List<VariableDeclaration> moduleProcedureParams;
    private ProcedureDeclaration moduleProcedureDeclaration;
    private VariableDeclaration moduleProcedureVariableDeclaration;
    private Block moduleProcedureFunctionBlock;
    private List<VariableDeclaration> moduleProcedureFunctionParams;
    private FunctionDeclaration moduleProcedureFunctionDeclaration;
    private Block moduleProcedureProcedureBlock;
    private List<VariableDeclaration> moduleProcedureProcedureParams;
    private ProcedureDeclaration moduleProcedureProcedureDeclaration;

    // module variable
    private VariableDeclaration moduleVariableDeclaration;

    // module loop
    private Expression moduleLoopCondition;
    private Block moduleLoopBlock;
    private WhileLoop moduleLoop;
    private ContinueStatement moduleLoopContinueStatement;
    private BreakStatement moduleLoopBreakStatement;

    // module conditional
    private Expression moduleConditionalStatementCondition;
    private ConditionalStatement moduleConditionalStatement;
    private Block moduleConditionalStatementThenBlock;
    private Block moduleConditionalStatementElseBlock;

    // module function return statement
    private Expression moduleReturnStatementParameter;
    private ReturnStatement moduleReturnStatement;

    // module procedure call
    private Expression moduleProcedureCallFunctionCallParameter01;
    private Expression moduleProcedureCallFunctionCallParameter02;
    private List<Expression> moduleProcedureCallFunctionCallParameters;
    private FunctionCall moduleProcedureCall;

    // module assignment with function call as left side
    private VariableDeclaration moduleAssignmentMemberAccessVariableDeclaration;
    private VariableAccess moduleAssignmentMemberAccessVariableAccessClass;
    private VariableAccess moduleAssignmentMemberAccessVariableAccessVariable;
    private MemberAccess moduleAssignmentMemberAccess;
    private Expression moduleAssignmentFunctionCallParameter01;
    private Expression moduleAssignmentFunctionCallParameter02;
    private List<Expression> moduleAssignmentFunctionCallParameters;
    private FunctionCall moduleAssignmentFunctionCall;
    private Assignment moduleAssignment;

    // invalid modul
    private Import invalidModuleImport01;
    private Import invalidModuleImport02;
    private List<Import> invalidModuleImports;
    private Block invalidModuleBlock;
    private ModuleDeclaration invalidModuleDeclaration;

    // invalid class
    private List<TypeInstantiation> invalidClassSuperClasses;
    private Block invalidClassBlock;
    private ClassDeclaration invalidClassDeclaration;

    private Position buildPosition() {
        return new Position("declarationTest", this.lineCounter++, 0);
    }

    @Before
    public void setUpAST() {
        this.lineCounter = 0;
        this.setParentVisitor = new SetParentVisitor();
        this.setParentVisitor.setStopOnFirstError(true);
        this.declarationVisitor = new DeclarationVisitor();
        this.declarationVisitor.setStopOnFirstError(true);

        // module
        this.moduleImport01 = new Import(buildPosition(), new ResolvableIdentifier("Dummy"));
        this.moduleImport02 = new Import(buildPosition(), new ResolvableIdentifier("Dummy"));
        this.moduleImports = new ArrayList<Import>();
        this.moduleBlock = new Block(buildPosition());
        this.aPackage = new Package(new Identifier(""));
        this.moduleDeclaration =
                new ModuleDeclaration(buildPosition(), new Identifier("moduleDeclaration"), this.moduleBlock, this.moduleImports);
        this.aPackage.addModule(this.moduleDeclaration);

        // module class
        this.classBlock = new Block(buildPosition());
        List<TypeInstantiation> classDeclSuperClasses = new ArrayList<>();
        this.classDeclaration =
                new ClassDeclaration(buildPosition(), new Identifier("classDeclaration"), classDeclSuperClasses,
                        this.classBlock);
        this.classVariableDeclaration =
                new VariableDeclaration(buildPosition(),
                        new Identifier("classVariableDeclaration"),
                        TypeInstantiation.forTypeName("String").create(),
                        VariableDeclaration.DeclarationType.ATTRIBUTE);

        this.classFunctionBlock = new Block(buildPosition());
        this.classFunctionParams = new ArrayList<VariableDeclaration>();
        this.classFunctionDeclaration =
                new FunctionDeclaration(buildPosition(),
                        new Identifier("classFunctionDeclaration"),
                        this.classFunctionBlock, this.classFunctionParams,
                        TypeInstantiation.forTypeName("String").create());

        this.classProcedureBlock = new Block(buildPosition());
        this.classProcedureParams = new ArrayList<VariableDeclaration>();
        this.classProcedureDeclaration =
                new ProcedureDeclaration(buildPosition(), new Identifier("classProcedureDeclaration"),
                        this.classProcedureBlock, this.classProcedureParams);

        // module class extended
        this.extendedClassBlock = new Block(buildPosition());
        List<TypeInstantiation> extendedClassDeclSuperClasses = new ArrayList<>();
        extendedClassDeclSuperClasses.add(TypeInstantiation.forTypeName("classDeclaration").create());
        this.extendedClassDeclaration =
                new ClassDeclaration(buildPosition(), new Identifier("extendedClassDeclaration"),
                        extendedClassDeclSuperClasses, this.extendedClassBlock);

        // module function
        this.moduleFunctionBlock = new Block(buildPosition());
        this.moduleFunctionParamsVariable01 =
                new VariableDeclaration(buildPosition(), new Identifier("moduleFunctionParamsVariable01"),
                        TypeInstantiation.forTypeName("String").create(),
                        VariableDeclaration.DeclarationType.PARAMETER);
        this.moduleFunctionParamsVariable02 =
                new VariableDeclaration(buildPosition(), new Identifier("moduleFunctionParamsVariable02"),
                        TypeInstantiation.forTypeName("String").create(),
                        VariableDeclaration.DeclarationType.PARAMETER);
        this.moduleFunctionParams = new ArrayList<VariableDeclaration>();
        this.moduleFunctionDeclaration =
                new FunctionDeclaration(buildPosition(),
                        new Identifier("moduleFunctionDeclaration"),
                        this.moduleFunctionBlock, this.moduleFunctionParams,
                        TypeInstantiation.forTypeName("String").create());
        this.moduleFunctionVariableDeclaration =
                new VariableDeclaration(buildPosition(),
                        new Identifier("moduleFunctionVariableDeclaration"),
                        TypeInstantiation.forTypeName("String").create(),
                        VariableDeclaration.DeclarationType.VARIABLE);
        this.moduleFunctionFunctionBlock = new Block(buildPosition());
        this.moduleFunctionFunctionParams = new ArrayList<VariableDeclaration>();
        this.moduleFunctionFunctionDeclaration =
                new FunctionDeclaration(buildPosition(),
                        new Identifier("moduleFunctionFunctionDeclaration"),
                        this.moduleFunctionFunctionBlock,
                        this.moduleFunctionFunctionParams,
                        TypeInstantiation.forTypeName("String").create());

        this.moduleFunctionProcedureBlock = new Block(buildPosition());
        this.moduleFunctionProcedureParams = new ArrayList<VariableDeclaration>();
        this.moduleFunctionProcedureDeclaration =
                new ProcedureDeclaration(buildPosition(), new Identifier("moduleFunctionProcedureDeclaration"),
                        this.moduleFunctionProcedureBlock, this.moduleFunctionProcedureParams);

        // module procedure
        this.moduleProcedureBlock = new Block(buildPosition());
        this.moduleProcedureParamsVariable01 =
                new VariableDeclaration(buildPosition(), new Identifier("moduleProcedureParamsVariable01"),
                        TypeInstantiation.forTypeName("String").create(),
                        VariableDeclaration.DeclarationType.PARAMETER);
        this.moduleProcedureParamsVariable02 =
                new VariableDeclaration(buildPosition(), new Identifier("moduleProcedureParamsVariable02"),
                        TypeInstantiation.forTypeName("String").create(),
                        VariableDeclaration.DeclarationType.PARAMETER);
        this.moduleProcedureParams = new ArrayList<VariableDeclaration>();
        this.moduleProcedureDeclaration =
                new ProcedureDeclaration(buildPosition(), new Identifier("moduleProcedureDeclaration"),
                        this.moduleProcedureBlock, this.moduleProcedureParams);
        this.moduleProcedureVariableDeclaration =
                new VariableDeclaration(buildPosition(),
                        new Identifier("moduleProcedureVariableDeclaration"),
                        TypeInstantiation.forTypeName("String").create(),
                        VariableDeclaration.DeclarationType.VARIABLE);
        this.moduleProcedureFunctionBlock = new Block(buildPosition());
        this.moduleProcedureFunctionParams = new ArrayList<VariableDeclaration>();
        this.moduleProcedureFunctionDeclaration =
                new FunctionDeclaration(buildPosition(),
                        new Identifier("moduleProcedureFunctionDeclaration"),
                        this.moduleProcedureFunctionBlock,
                        this.moduleProcedureFunctionParams,
                        TypeInstantiation.forTypeName("String").create());

        this.moduleProcedureProcedureBlock = new Block(buildPosition());
        this.moduleProcedureProcedureParams = new ArrayList<VariableDeclaration>();
        this.moduleProcedureProcedureDeclaration =
                new ProcedureDeclaration(buildPosition(), new Identifier("moduleProcedureProcedureDeclaration"),
                        this.moduleProcedureProcedureBlock, this.moduleProcedureProcedureParams);

        // module variable
        this.moduleVariableDeclaration =
                new VariableDeclaration(buildPosition(),
                        new Identifier("moduleVariableDeclaration"),
                        TypeInstantiation.forTypeName("String").create(),
                        VariableDeclaration.DeclarationType.VARIABLE);

        // module loop
        this.moduleLoopCondition = new BooleanLiteral(buildPosition(), true);
        this.moduleLoopBlock = new Block(buildPosition());
        this.moduleLoop = new WhileLoop(buildPosition(), this.moduleLoopCondition, this.moduleLoopBlock);
        this.moduleLoopContinueStatement = new ContinueStatement(buildPosition());
        this.moduleLoopBreakStatement = new BreakStatement(buildPosition());

        // module conditional
        this.moduleConditionalStatementCondition = new BooleanLiteral(buildPosition(), true);
        this.moduleConditionalStatementThenBlock = new Block(buildPosition());
        this.moduleConditionalStatementElseBlock = new Block(buildPosition());
        this.moduleConditionalStatement =
                new ConditionalStatement(buildPosition(), this.moduleConditionalStatementCondition,
                        this.moduleConditionalStatementThenBlock, this.moduleConditionalStatementElseBlock);

        // module function return statement
        this.moduleReturnStatementParameter = new StringLiteral(buildPosition(), "42");
        this.moduleReturnStatement = new ReturnStatement(buildPosition(), this.moduleReturnStatementParameter);

        // module procedure call
        this.moduleProcedureCallFunctionCallParameter01 = new StringLiteral(buildPosition(), "42");
        this.moduleProcedureCallFunctionCallParameter02 = new StringLiteral(buildPosition(), "42");
        this.moduleProcedureCallFunctionCallParameters = new ArrayList<Expression>();

        this.moduleProcedureCall =
                new FunctionCall(buildPosition(), new ResolvableIdentifier("moduleProcedureDeclaration"),
                        this.moduleProcedureCallFunctionCallParameters);

        // module assignment with function call as left side
        this.moduleAssignmentMemberAccessVariableDeclaration =
                new VariableDeclaration(buildPosition(), new Identifier(
                        "moduleAssignmentMemberAccessVariableDeclaration"),
                        TypeInstantiation.forTypeName("classDeclaration").create(),
                        VariableDeclaration.DeclarationType.VARIABLE);
        this.moduleAssignmentMemberAccessVariableAccessClass =
                new VariableAccess(buildPosition(), new ResolvableIdentifier(
                        "moduleAssignmentMemberAccessVariableDeclaration"));
        this.moduleAssignmentMemberAccessVariableAccessVariable =
                new VariableAccess(buildPosition(), new ResolvableIdentifier("classVariableDeclaration"));
        this.moduleAssignmentMemberAccess =
                new MemberAccess(buildPosition(), this.moduleAssignmentMemberAccessVariableAccessClass,
                        this.moduleAssignmentMemberAccessVariableAccessVariable);
        this.moduleAssignmentFunctionCallParameter01 = new StringLiteral(buildPosition(), "42");
        this.moduleAssignmentFunctionCallParameter02 = new StringLiteral(buildPosition(), "42");
        this.moduleAssignmentFunctionCallParameters = new ArrayList<Expression>();
        this.moduleAssignmentFunctionCall =
                new FunctionCall(buildPosition(), new ResolvableIdentifier("moduleFunctionDeclaration"),
                        this.moduleAssignmentFunctionCallParameters);
        this.moduleAssignment = new Assignment(buildPosition(), this.moduleAssignmentMemberAccess, this.moduleAssignmentFunctionCall);

        // invalid modul
        this.invalidModuleImport01 = new Import(buildPosition(), new ResolvableIdentifier("Dummy"));
        this.invalidModuleImport02 = new Import(buildPosition(), new ResolvableIdentifier("Dummy"));
        this.invalidModuleImports = new ArrayList<Import>();
        this.invalidModuleBlock = new Block(buildPosition());
        this.invalidModuleDeclaration =
                new ModuleDeclaration(buildPosition(), new Identifier("invalidModuleDeclaration"), this.invalidModuleBlock,
                        this.invalidModuleImports);

        // invalid class
        this.invalidClassBlock = new Block(buildPosition());
        this.invalidClassSuperClasses = new ArrayList<TypeInstantiation>();
        this.invalidClassDeclaration =
                new ClassDeclaration(buildPosition(), new Identifier("invalidClass"), this.invalidClassSuperClasses,
                        this.invalidClassBlock);

    }

    private void fillASTModule() {
        this.moduleImports.add(this.moduleImport01);
        this.moduleImports.add(this.moduleImport02);
    }

    private void fillASTClass() {
        this.moduleBlock.addDeclaration(this.classDeclaration);
    }

    private void fillASTClassVariable() {
        this.classBlock.addDeclaration(this.classVariableDeclaration);
    }

    private void fillASTClassFunction() {
        this.classBlock.addDeclaration(this.classFunctionDeclaration);
    }

    private void fillASTClassProcedure() {
        this.classBlock.addDeclaration(this.classProcedureDeclaration);
    }

    private void fillASTExtendedClass() {
        this.moduleBlock.addDeclaration(this.extendedClassDeclaration);
    }

    private void fillASTModuleFunction() {
        this.moduleBlock.addDeclaration(this.moduleFunctionDeclaration);
    }

    private void fillASTModuleFunctionVariable() {
        this.moduleFunctionParams.add(this.moduleFunctionParamsVariable01);
        this.moduleFunctionParams.add(this.moduleFunctionParamsVariable02);
        this.moduleFunctionBlock.addDeclaration(this.moduleFunctionVariableDeclaration);
    }

    private void fillASTModuleFunctionFunction() {
        this.moduleFunctionBlock.addDeclaration(this.moduleFunctionFunctionDeclaration);
    }

    private void fillASTModuleFunctionProcedure() {
        this.moduleFunctionBlock.addDeclaration(this.moduleFunctionProcedureDeclaration);
    }

    private void fillASTModuleProcedure() {
        this.moduleBlock.addDeclaration(this.moduleProcedureDeclaration);
    }

    private void fillASTModuleProcedureVariable() {
        this.moduleProcedureParams.add(this.moduleProcedureParamsVariable01);
        this.moduleProcedureParams.add(this.moduleProcedureParamsVariable02);
        this.moduleProcedureBlock.addDeclaration(this.moduleProcedureVariableDeclaration);
    }

    private void fillASTModuleProcedureFunction() {
        this.moduleProcedureBlock.addDeclaration(this.moduleProcedureFunctionDeclaration);
    }

    private void fillASTModuleProcedureProcedure() {
        this.moduleProcedureBlock.addDeclaration(this.moduleProcedureProcedureDeclaration);
    }

    private void fillASTModuleVariable() {
        this.moduleBlock.addDeclaration(this.moduleVariableDeclaration);
    }

    private void fillASTModuleStatements() {
        this.moduleLoopBlock.addStatement(this.moduleLoopContinueStatement);
        this.moduleLoopBlock.addStatement(this.moduleLoopBreakStatement);

        this.moduleBlock.addDeclaration(this.moduleAssignmentMemberAccessVariableDeclaration);
        this.moduleBlock.addStatement(this.moduleLoop);
        this.moduleBlock.addStatement(this.moduleConditionalStatement);
        this.moduleBlock.addStatement(this.moduleReturnStatement);

        this.moduleProcedureCallFunctionCallParameters.add(this.moduleProcedureCallFunctionCallParameter01);
        this.moduleProcedureCallFunctionCallParameters.add(this.moduleProcedureCallFunctionCallParameter02);
        this.moduleBlock.addStatement(this.moduleProcedureCall);

        this.moduleAssignmentFunctionCallParameters.add(this.moduleAssignmentFunctionCallParameter01);
        this.moduleAssignmentFunctionCallParameters.add(this.moduleAssignmentFunctionCallParameter02);
        this.moduleBlock.addStatement(this.moduleAssignment);
    }

    private void fillASTInvalidModule() {
        this.invalidModuleImports.add(this.invalidModuleImport01);
        this.invalidModuleImports.add(this.invalidModuleImport02);
    }

    @Test
    public void setUpASTTest() {
        assertNotNull("setParentVisitor is null", this.setParentVisitor);
        assertNotNull("declarationVisitor is null", this.declarationVisitor);

        assertNotNull("moduleImport01 is null", this.moduleImport01);
        assertNotNull("moduleImport02 is null", this.moduleImport02);
        assertNotNull("moduleImports is null", this.moduleImports);
        assertNotNull("moduleBlock is null", this.moduleBlock);
        assertNotNull("moduleDeclaration is null", this.moduleDeclaration);
        assertNotNull("package is null", this.aPackage);

        assertNotNull("classDeclarationDeclarations is null", this.classBlock);
        assertNotNull("classDeclaration is null", this.classDeclaration);
        assertNotNull("classVariableDeclaration is null", this.classVariableDeclaration);

        assertNotNull("classFunctionBlock is null", this.classFunctionBlock);
        assertNotNull("classFunctionParams is null", this.classFunctionParams);
        assertNotNull("classFunctionDeclaration is null", this.classFunctionDeclaration);

        assertNotNull("classProcedureBlock is null", this.classProcedureBlock);
        assertNotNull("classProcedureParams is null", this.classProcedureParams);
        assertNotNull("classProcedureDeclaration is null", this.classProcedureDeclaration);

        assertNotNull("extendedClassDeclarationDeclarations is null", this.extendedClassBlock);
        assertNotNull("extendedClassDeclaration is null", this.extendedClassDeclaration);

        assertNotNull("moduleFunctionBlock is null", this.moduleFunctionBlock);
        assertNotNull("moduleFunctionParamsVariable01 is null", this.moduleFunctionParamsVariable01);
        assertNotNull("moduleFunctionParamsVariable02 is null", this.moduleFunctionParamsVariable02);
        assertNotNull("moduleFunctionParams is null", this.moduleFunctionParams);
        assertNotNull("moduleFunctionDeclaration is null", this.moduleFunctionDeclaration);
        assertNotNull("moduleFunctionVariableDeclaration is null", this.moduleFunctionVariableDeclaration);
        assertNotNull("moduleFunctionFunctionBlock is null", this.moduleFunctionFunctionBlock);
        assertNotNull("moduleFunctionFunctionParams is null", this.moduleFunctionFunctionParams);
        assertNotNull("moduleFunctionFunctionDeclaration is null", this.moduleFunctionFunctionDeclaration);
        assertNotNull("moduleFunctionProcedureBlock is null", this.moduleFunctionProcedureBlock);
        assertNotNull("moduleFunctionProcedureParams is null", this.moduleFunctionProcedureParams);
        assertNotNull("moduleFunctionProcedureDeclaration is null", this.moduleFunctionProcedureDeclaration);

        assertNotNull("moduleProcedureBlock is null", this.moduleProcedureBlock);
        assertNotNull("moduleProcedureParamsVariable01 is null", this.moduleProcedureParamsVariable01);
        assertNotNull("moduleProcedureParamsVariable02 is null", this.moduleProcedureParamsVariable02);
        assertNotNull("moduleProcedureParams is null", this.moduleProcedureParams);
        assertNotNull("moduleProcedureDeclaration is null", this.moduleProcedureDeclaration);
        assertNotNull("moduleProcedureVariableDeclaration is null", this.moduleProcedureVariableDeclaration);
        assertNotNull("moduleProcedureFunctionBlock is null", this.moduleProcedureFunctionBlock);
        assertNotNull("moduleProcedureFunctionParams is null", this.moduleProcedureFunctionParams);
        assertNotNull("moduleProcedureFunctionDeclaration is null", this.moduleProcedureFunctionDeclaration);
        assertNotNull("moduleProcedureProcedureBlock is null", this.moduleProcedureProcedureBlock);
        assertNotNull("moduleProcedureProcedureParams is null", this.moduleProcedureProcedureParams);
        assertNotNull("moduleProcedureProcedureDeclaration is null", this.moduleProcedureProcedureDeclaration);

        assertNotNull("moduleVariableDeclaration is null", this.moduleVariableDeclaration);

        assertNotNull("moduleLoopCondition is null", this.moduleLoopCondition);
        assertNotNull("moduleLoopBlock is null", this.moduleLoopBlock);
        assertNotNull("moduleLoop is null", this.moduleLoop);
        assertNotNull("moduleLoopContinueStatement is null", this.moduleLoopContinueStatement);
        assertNotNull("moduleLoopBreakStatement is null", this.moduleLoopBreakStatement);

        assertNotNull("moduleConditionalStatementCondition is null", this.moduleConditionalStatementCondition);
        assertNotNull("moduleConditionalStatement is null", this.moduleConditionalStatement);
        assertNotNull("moduleConditionalStatementThenBlock is null", this.moduleConditionalStatementThenBlock);
        assertNotNull("moduleConditionalStatementElseBlock is null", this.moduleConditionalStatementElseBlock);

        assertNotNull("moduleReturnStatementParameter is null", this.moduleReturnStatementParameter);
        assertNotNull("moduleReturnStatement is null", this.moduleReturnStatement);

        assertNotNull("moduleProcedureCallFunctionCallParameter01 is null", this.moduleProcedureCallFunctionCallParameter01);
        assertNotNull("moduleProcedureCallFunctionCallParameter02 is null", this.moduleProcedureCallFunctionCallParameter02);
        assertNotNull("moduleProcedureCallFunctionCallParameters is null", this.moduleProcedureCallFunctionCallParameters);
        assertNotNull("moduleProcedureCall is null", this.moduleProcedureCall);

        assertNotNull(
                "moduleAssignmentMemberAccessVariableDeclaration is null",
                this.moduleAssignmentMemberAccessVariableDeclaration);
        assertNotNull(
                "moduleAssignmentMemberAccessVariableAccessClass is null",
                this.moduleAssignmentMemberAccessVariableAccessClass);
        assertNotNull(
                "moduleAssignmentMemberAccessVariableAccessVariable is null",
                this.moduleAssignmentMemberAccessVariableAccessVariable);
        assertNotNull("moduleAssignmentMemberAccess is null", this.moduleAssignmentMemberAccess);
        assertNotNull("moduleAssignmentFunctionCallParameter01 is null", this.moduleAssignmentFunctionCallParameter01);
        assertNotNull("moduleAssignmentFunctionCallParameter02 is null", this.moduleAssignmentFunctionCallParameter02);
        assertNotNull("moduleAssignmentFunctionCallParameters is null", this.moduleAssignmentFunctionCallParameters);
        assertNotNull("moduleAssignmentFunctionCall is null", this.moduleAssignmentFunctionCall);
        assertNotNull("moduleAssignment is null", this.moduleAssignment);

        assertNotNull("invalidModuleImport01 is null", this.invalidModuleImport01);
        assertNotNull("invalidModuleImport02 is null", this.invalidModuleImport02);
        assertNotNull("invalidModuleImports is null", this.invalidModuleImports);
        assertNotNull("invalidModuleBlock is null", this.invalidModuleBlock);
        assertNotNull("invalidModuleDeclaration is null", this.invalidModuleDeclaration);

        assertNotNull("invalidClassSuperClasses is null", this.invalidClassSuperClasses);
        assertNotNull("invalidClassDeclarations is null", this.invalidClassBlock);
        assertNotNull("invalidClassDeclaration is null", this.invalidClassDeclaration);
    }

    // DECLARATION AND SCOPE

    // ONE CASE TO TEST 'EM ALL

    @Test
    public void nodeInteractionTest() {
        // Es muss einen Test geben, in dem alle AST-Nodes vorkommen. Nach
        // Durchlauf des DeclarationVisitors müssen alle AST-Nodes einen Scope
        // haben. Jede AST-Node, die mehr als ein Kind haben kann, muss für
        // jeden möglichen Kind-Typ mehr als 1 Kind besitzen. (z.B. Modulblock
        // mit 2 Statements und 2 Declarations)

        fillASTModule();
        fillASTExtendedClass();
        fillASTClass();
        fillASTClassVariable();
        fillASTClassFunction();
        fillASTClassProcedure();
        fillASTModuleFunction();
        fillASTModuleFunctionVariable();
        fillASTModuleFunctionFunction();
        fillASTModuleFunctionProcedure();
        fillASTModuleProcedure();
        fillASTModuleProcedureVariable();
        fillASTModuleProcedureFunction();
        fillASTModuleProcedureProcedure();
        fillASTModuleVariable();
        fillASTModuleStatements();

        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);

        ASTNode[] allASTNodes =
                new ASTNode[] { this.moduleImport01, this.moduleImport02,
                        this.moduleBlock, this.aPackage, this.classDeclaration,
                        this.classVariableDeclaration, this.classFunctionBlock,
                        this.classFunctionDeclaration, this.classProcedureBlock,
                        this.classProcedureDeclaration, this.extendedClassDeclaration,
                        this.moduleFunctionBlock,
                        this.moduleFunctionParamsVariable01,
                        this.moduleFunctionParamsVariable02,
                        this.moduleFunctionDeclaration,
                        this.moduleFunctionVariableDeclaration,
                        this.moduleFunctionFunctionBlock,
                        this.moduleFunctionFunctionDeclaration,
                        this.moduleFunctionProcedureBlock,
                        this.moduleFunctionProcedureDeclaration,
                        this.moduleProcedureBlock, this.moduleProcedureParamsVariable01,
                        this.moduleProcedureParamsVariable02,
                        this.moduleProcedureDeclaration,
                        this.moduleProcedureVariableDeclaration,
                        this.moduleProcedureFunctionBlock,
                        this.moduleProcedureFunctionDeclaration,
                        this.moduleProcedureProcedureBlock,
                        this.moduleProcedureProcedureDeclaration,
                        this.moduleVariableDeclaration, this.moduleLoopCondition,
                        this.moduleLoopBlock, this.moduleLoop,
                        this.moduleLoopContinueStatement, this.moduleLoopBreakStatement,
                        this.moduleConditionalStatementCondition,
                        this.moduleConditionalStatement,
                        this.moduleConditionalStatementThenBlock,
                        this.moduleConditionalStatementElseBlock,
                        this.moduleReturnStatementParameter, this.moduleReturnStatement,
                        this.moduleProcedureCallFunctionCallParameter01,
                        this.moduleProcedureCallFunctionCallParameter02,
                        this.moduleProcedureCall,
                        this.moduleAssignmentMemberAccessVariableDeclaration,
                        this.moduleAssignmentMemberAccessVariableAccessClass,
                        this.moduleAssignmentMemberAccessVariableAccessVariable,
                        this.moduleAssignmentMemberAccess,
                        this.moduleAssignmentFunctionCallParameter01,
                        this.moduleAssignmentFunctionCallParameter02,
                        this.moduleAssignmentFunctionCall, this.moduleAssignment,
                        this.moduleDeclaration };

        for (ASTNode node : allASTNodes) {
            assertNotNull("Scope not set for class: " + node.getClass().getSimpleName(), node.getScope());

            if (node instanceof ClassDeclaration) {
                assertTrue(
                        "Scope for ClassDeclaration is no ClassScope: " +
                            node.getClass().getSimpleName(),
                        node.getScope() instanceof ClassScope);
            }

            if (!(node instanceof Package)) {
                assertNotNull("Parent not set for class: " +
                    node.getClass().getSimpleName(), node.getParentNode());
            } else {
                assertNull(
                        "Parent set for class (but should be null): " +
                            node.getClass().getSimpleName(),
                        node.getParentNode());
            }
        }
    }

    // Module Declaration Tests

    @Test
    public void moduleTestModuleDeclaration() {
        fillASTModule();

        // there is no way of validating the correct declaration but to fail
        // if an exception occurs.
        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
    }

    @Test(expected = InvalidPlaceToDeclareException.class)
    public void moduleTestModuleDeclarationInModule() {
        fillASTModule();
        fillASTInvalidModule();

        this.moduleBlock.addDeclaration(this.invalidModuleDeclaration);
        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
    }

    @Test(expected = InvalidPlaceToDeclareException.class)
    public void moduleTestModuleDeclarationInClass() {
        fillASTModule();
        fillASTClass();
        fillASTInvalidModule();

        this.classBlock.addDeclaration(this.invalidModuleDeclaration);
        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
    }

    @Test(expected = InvalidPlaceToDeclareException.class)
    public void moduleTestModuleDeclarationInFunction() {
        fillASTModule();
        fillASTModuleFunction();
        fillASTInvalidModule();

        this.moduleFunctionBlock.addDeclaration(this.invalidModuleDeclaration);
        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
    }

    @Test(expected = InvalidPlaceToDeclareException.class)
    public void moduleTestModuleDeclarationInProcedure() {
        fillASTModule();
        fillASTModuleProcedure();
        fillASTInvalidModule();

        this.moduleProcedureBlock.addDeclaration(this.invalidModuleDeclaration);
        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
    }

    // Class Declaration Tests

    @Test
    public void classTestClassDeclarationInModule() {
        fillASTModule();
        fillASTClass();

        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
        assertSame(this.classDeclaration, this.moduleDeclaration.getScope()
                .resolve(Location.UNKNOWN_LOCATION, new ResolvableIdentifier("classDeclaration")));
    }

    @Test(expected = InvalidPlaceToDeclareException.class)
    public void classTestClassDeclarationInClass() {
        fillASTModule();
        fillASTClass();

        this.classBlock.addDeclaration(this.invalidClassDeclaration);
        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
    }

    @Test(expected = InvalidPlaceToDeclareException.class)
    public void classTestClassDeclarationInFunction() {
        fillASTModule();
        fillASTModuleFunction();

        this.moduleFunctionBlock.addDeclaration(this.invalidClassDeclaration);
        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
    }

    @Test(expected = InvalidPlaceToDeclareException.class)
    public void classTestClassDeclarationInProcedure() {
        fillASTModule();
        fillASTModuleProcedure();

        this.moduleProcedureBlock.addDeclaration(this.invalidClassDeclaration);
        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
    }

    // Function Declaration Tests

    @Test
    public void functionTestFunctionDeclarationInModule() {
        fillASTModule();
        fillASTModuleFunction();

        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
        assertTrue(this.moduleDeclaration.getScope().resolveProcedure(Location.UNKNOWN_LOCATION, new ResolvableIdentifier("moduleFunctionDeclaration")).contains(
                this.moduleFunctionDeclaration));
    }

    @Test
    public void functionTestFunctionDeclarationInClass() {
        fillASTModule();
        fillASTClass();
        fillASTClassFunction();

        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
        assertTrue(this.classDeclaration.getScope().resolveProcedure(Location.UNKNOWN_LOCATION, new ResolvableIdentifier("classFunctionDeclaration")).contains(
                this.classFunctionDeclaration));
    }

    @Test
    public void functionTestFunctionDeclarationInFunction() {
        fillASTModule();
        fillASTModuleFunction();
        fillASTModuleFunctionFunction();

        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
        assertTrue(this.moduleFunctionDeclaration.getScope().resolveProcedure(Location.UNKNOWN_LOCATION,
                new ResolvableIdentifier("moduleFunctionFunctionDeclaration")).contains(
                this.moduleFunctionFunctionDeclaration));
    }

    @Test
    public void functionTestFunctionDeclarationInProcedure() {
        fillASTModule();
        fillASTModuleProcedure();
        fillASTModuleProcedureFunction();

        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
        assertTrue(this.moduleProcedureDeclaration.getScope().resolveProcedure(Location.UNKNOWN_LOCATION,
                new ResolvableIdentifier("moduleProcedureFunctionDeclaration")).contains(
                this.moduleProcedureFunctionDeclaration));
    }

    // Procedure Declaration Tests

    @Test
    public void procedureTestProcedureDeclarationInModule() {
        fillASTModule();
        fillASTModuleProcedure();

        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
        assertTrue(this.moduleDeclaration.getScope().resolveProcedure(Location.UNKNOWN_LOCATION, new ResolvableIdentifier("moduleProcedureDeclaration")).contains(
                this.moduleProcedureDeclaration));
    }

    @Test
    public void procedureTestProcedureDeclarationInClass() {
        fillASTModule();
        fillASTClass();
        fillASTClassProcedure();

        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
        assertTrue(this.classDeclaration.getScope().resolveProcedure(Location.UNKNOWN_LOCATION, new ResolvableIdentifier("classProcedureDeclaration")).contains(
                this.classProcedureDeclaration));
    }

    @Test
    public void procedureTestProcedureDeclarationInFunction() {
        fillASTModule();
        fillASTModuleFunction();
        fillASTModuleFunctionProcedure();

        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
        assertTrue(this.moduleFunctionDeclaration.getScope().resolveProcedure(Location.UNKNOWN_LOCATION,
                new ResolvableIdentifier("moduleFunctionProcedureDeclaration")).contains(
                this.moduleFunctionProcedureDeclaration));
    }

    @Test
    public void procedureTestProcedureDeclarationInProcedure() {
        fillASTModule();
        fillASTModuleProcedure();
        fillASTModuleProcedureProcedure();

        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
        assertTrue(this.moduleProcedureDeclaration.getScope().resolveProcedure(Location.UNKNOWN_LOCATION,
                new ResolvableIdentifier("moduleProcedureProcedureDeclaration")).contains(
                this.moduleProcedureProcedureDeclaration));
    }

    // Variable Declaration Tests

    @Test
    public void variableTestVariableDeclarationInModule() {
        fillASTModule();
        fillASTModuleVariable();

        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
        assertSame(
                this.moduleVariableDeclaration,
                this.moduleDeclaration.getScope().resolve(Location.UNKNOWN_LOCATION, new ResolvableIdentifier("moduleVariableDeclaration")));
    }

    @Test
    public void variableTestVariableDeclarationInClass() {
        fillASTModule();
        fillASTClass();
        fillASTClassVariable();

        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
        assertSame(
                this.classVariableDeclaration,
                this.classDeclaration.getScope().resolve(Location.UNKNOWN_LOCATION, new ResolvableIdentifier("classVariableDeclaration")));
    }

    @Test
    public void variableTestVariableDeclarationInFunction() {
        fillASTModule();
        fillASTModuleFunction();
        fillASTModuleFunctionVariable();

        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
        Scope scope = this.moduleFunctionDeclaration.getScope();

        assertSame(
                this.moduleFunctionParamsVariable01,
                scope.resolve(Location.UNKNOWN_LOCATION, new ResolvableIdentifier("moduleFunctionParamsVariable01")));
        assertSame(
                this.moduleFunctionParamsVariable02,
                scope.resolve(Location.UNKNOWN_LOCATION, new ResolvableIdentifier("moduleFunctionParamsVariable02")));
        assertSame(
                this.moduleFunctionVariableDeclaration,
                scope.resolve(Location.UNKNOWN_LOCATION, new ResolvableIdentifier("moduleFunctionVariableDeclaration")));
    }

    @Test
    public void variableTestVariableDeclarationInProcedure() {
        fillASTModule();
        fillASTModuleProcedure();
        fillASTModuleProcedureVariable();

        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
        Scope scope = this.moduleProcedureDeclaration.getScope();

        assertSame(
                this.moduleProcedureParamsVariable01,
                scope.resolve(Location.UNKNOWN_LOCATION, new ResolvableIdentifier("moduleProcedureParamsVariable01")));
        assertSame(
                this.moduleProcedureParamsVariable02,
                scope.resolve(Location.UNKNOWN_LOCATION, new ResolvableIdentifier("moduleProcedureParamsVariable02")));
        assertSame(
                this.moduleProcedureVariableDeclaration,
                scope.resolve(Location.UNKNOWN_LOCATION, new ResolvableIdentifier("moduleProcedureVariableDeclaration")));
    }

    @Test
    public void variableTestResolveVariableInNestedScope() {
        fillASTModule();
        fillASTModuleVariable();
        fillASTClass();
        fillASTClassVariable();
        fillASTClassFunction();

        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
        Scope scope = this.classFunctionDeclaration.getScope();
        assertSame(this.classVariableDeclaration, scope.resolve(Location.UNKNOWN_LOCATION, new ResolvableIdentifier("classVariableDeclaration")));
        assertSame(this.moduleVariableDeclaration, scope.resolve(Location.UNKNOWN_LOCATION, new ResolvableIdentifier("moduleVariableDeclaration")));
    }

    // REDECLARATION

    @Test(expected = RedeclarationException.class)
    public void redeclarationTestRedeclarationInSameScopeTest() {
        fillASTModule();
        fillASTModuleVariable();

        VariableDeclaration variableDeclaration =
                new VariableDeclaration(buildPosition(),
                        new Identifier("moduleVariableDeclaration"),
                        TypeInstantiation.forTypeName("String").create(),
                        VariableDeclaration.DeclarationType.VARIABLE);
        this.moduleBlock.addDeclaration(variableDeclaration);

        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
    }

    @Test(expected = RedeclarationException.class)
    public void redeclarationTestRedeclarationInSameClassScopeTest() {
        fillASTModule();
        fillASTClass();
        fillASTClassVariable();

        VariableDeclaration variableDeclaration =
                new VariableDeclaration(buildPosition(),
                        new Identifier("classVariableDeclaration"),
                        TypeInstantiation.forTypeName("String").create(),
                        VariableDeclaration.DeclarationType.ATTRIBUTE);
        this.classBlock.addDeclaration(variableDeclaration);

        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
    }

    @Test
    public void redeclarationTestRedeclarationInDifferentScopes() {
        fillASTModule();
        fillASTModuleVariable();
        fillASTModuleFunction();

        VariableDeclaration variableDeclaration =
                new VariableDeclaration(buildPosition(),
                        new Identifier("moduleVariableDeclaration"),
                        TypeInstantiation.forTypeName("String").create(),
                        VariableDeclaration.DeclarationType.VARIABLE);
        this.moduleFunctionBlock.addDeclaration(variableDeclaration);

        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
        assertSame(
                variableDeclaration,
                this.moduleFunctionDeclaration.getScope().resolve(Location.UNKNOWN_LOCATION, new ResolvableIdentifier("moduleVariableDeclaration")));
        assertNotSame(
                variableDeclaration,
                this.moduleDeclaration.getScope().resolve(Location.UNKNOWN_LOCATION, new ResolvableIdentifier("moduleVariableDeclaration")));
    }

    @Test
    public void redeclarationTestRedeclarationInExtendedClassScope() {
        fillASTModule();
        fillASTExtendedClass();
        fillASTClass();
        fillASTClassVariable();

        VariableDeclaration variableDeclaration =
                new VariableDeclaration(buildPosition(),
                        new Identifier("classVariableDeclaration"),
                        TypeInstantiation.forTypeName("String").create(),
                        VariableDeclaration.DeclarationType.ATTRIBUTE);
        this.extendedClassBlock.addDeclaration(variableDeclaration);

        this.setParentVisitor.visit(this.aPackage);
        this.declarationVisitor.visit(this.aPackage);
        assertSame(
                variableDeclaration,
                this.extendedClassDeclaration.getScope().resolve(Location.UNKNOWN_LOCATION, new ResolvableIdentifier("classVariableDeclaration")));
        assertNotSame(
                variableDeclaration,
                this.classDeclaration.getScope().resolve(Location.UNKNOWN_LOCATION, new ResolvableIdentifier("classVariableDeclaration")));
    }
}
