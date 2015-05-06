/*
 * moco, the Monty Compiler
 * Copyright (c) 2013-2014, Monty's Coconut, All rights reserved.
 *
 * This file is part of moco, the Monty Compiler.
 *
 * moco is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * moco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * Linking this program and/or its accompanying libraries statically or
 * dynamically with other modules is making a combined work based on this
 * program. Thus, the terms and conditions of the GNU General Public License
 * cover the whole combination.
 *
 * As a special exception, the copyright holders of moco give
 * you permission to link this programm and/or its accompanying libraries
 * with independent modules to produce an executable, regardless of the
 * license terms of these independent modules, and to copy and distribute the
 * resulting executable under terms of your choice, provided that you also meet,
 * for each linked independent module, the terms and conditions of the
 * license of that module.
 *
 * An independent module is a module which is not
 * derived from or based on this program and/or its accompanying libraries.
 * If you modify this library, you may extend this exception to your version of
 * the program or library, but you are not obliged to do so. If you do not wish
 * to do so, delete this exception statement from your version.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library.
 */
package de.uni.bremen.monty.moco.ast;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ModuleDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeInstantiation;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.expression.Expression;
import de.uni.bremen.monty.moco.ast.expression.literal.BooleanLiteral;
import de.uni.bremen.monty.moco.ast.expression.literal.StringLiteral;
import de.uni.bremen.monty.moco.ast.statement.BreakStatement;
import de.uni.bremen.monty.moco.ast.statement.ConditionalStatement;
import de.uni.bremen.monty.moco.ast.statement.ContinueStatement;
import de.uni.bremen.monty.moco.ast.statement.ReturnStatement;
import de.uni.bremen.monty.moco.ast.statement.WhileLoop;
import de.uni.bremen.monty.moco.exception.InvalidControlFlowException;
import de.uni.bremen.monty.moco.visitor.ControlFlowVisitor;
import de.uni.bremen.monty.moco.visitor.SetParentVisitor;

public class ControlFlowTest {

	private SetParentVisitor setParentVisitor;
	private ControlFlowVisitor controlFlowVisitor;

	// MODULE
	private Block moduleBlock;
	private List<Import> moduleImports;
	private Package aPackage;
	private ModuleDeclaration moduleDeclaration;

	// MODULE LOOP
	private Expression moduleLoopCondition;
	private Block moduleLoopBlock;
	private WhileLoop moduleLoop;
	private ContinueStatement moduleLoopContinueStatement;
	private BreakStatement moduleLoopBreakStatement;
	private Expression moduleLoopLoopCondition;
	private Block moduleLoopLoopBlock;

	// MODULE LOOP LOOP
	private WhileLoop moduleLoopLoop;
	private ContinueStatement moduleLoopLoopContinueStatement;
	private BreakStatement moduleLoopLoopBreakStatement;
	private Expression moduleLoopLoopConditionalStatementCondition;
	private Block moduleLoopLoopConditionalStatementThenBlock;
	private Block moduleLoopLoopConditionalStatementElseBlock;
	private ConditionalStatement moduleLoopLoopConditionalStatement;
	private Expression moduleLoopLoopThenConditionalStatementCondition;
	private Block moduleLoopLoopThenConditionalStatementThenBlock;
	private Block moduleLoopLoopThenConditionalStatementElseBlock;
	private ConditionalStatement moduleLoopLoopThenConditionalStatement;
	private Expression moduleLoopLoopElseConditionalStatementCondition;
	private Block moduleLoopLoopElseConditionalStatementThenBlock;
	private Block moduleLoopLoopElseConditionalStatementElseBlock;
	private ConditionalStatement moduleLoopLoopElseConditionalStatement;

	// MODULE LOOP CONDITIONAL STATEMENT
	private Expression moduleLoopConditionalStatementCondition;
	private Block moduleLoopConditionalStatementThenBlock;
	private Block moduleLoopConditionalStatementElseBlock;
	private ConditionalStatement moduleLoopConditionalStatement;
	private ContinueStatement moduleLoopConditionalContinueStatement;
	private BreakStatement moduleLoopConditionalBreakStatement;

	// MODULE CONDITIONAL STATEMENT
	private Expression moduleConditionalStatementCondition;
	private Block moduleConditionalStatementThenBlock;
	private Block moduleConditionalStatementElseBlock;
	private ConditionalStatement moduleConditionalStatement;

	// MODULE STATETEMENTS
	private ContinueStatement moduleContinueStatement;
	private BreakStatement moduleBreakStatement;
	private Expression moduleReturnStatementParameter;
	private ReturnStatement moduleReturnStatement;

	// MODULE FUNCTION
	private Block moduleFunctionBlock;
	private List<VariableDeclaration> moduleFunctionParameter;
	private FunctionDeclaration moduleFunctionDeclaration;
	private Expression moduleFunctionReturnStatementParameter;
	private ReturnStatement moduleFunctionReturnStatement;

	// MODULE FUNCTION PROCEDURE
	private Block moduleFunctionProcedureBlock;
	private List<VariableDeclaration> moduleFunctionProcedureParameter;
	private ProcedureDeclaration moduleFunctionProcedureDeclaration;
	private Expression moduleFunctionProcedureReturnStatementParameter;
	private ReturnStatement moduleFunctionProcedureReturnStatement;

	// MODULE FUNCTION CONDITIONAL STATEMENT
	private Expression moduleFunctionConditionalStatementCondition;
	private Block moduleFunctionConditionalStatementThenBlock;
	private Block moduleFunctionConditionalStatementElseBlock;
	private ConditionalStatement moduleFunctionConditionalStatement;
	private Expression moduleFunctionConditionalReturnThenStatementParameter;
	private ReturnStatement moduleFunctionConditionalReturnThenStatement;
	private Expression moduleFunctionConditionalReturnElseStatementParameter;
	private ReturnStatement moduleFunctionConditionalReturnElseStatement;
	private Expression moduleFunctionConditionalConditionalStatementCondition;
	private Block moduleFunctionConditionalConditionalStatementThenBlock;
	private Block moduleFunctionConditionalConditionalStatementElseBlock;
	private ConditionalStatement moduleFunctionConditionalConditionalStatement;
	private Expression moduleFunctionConditionalConditionalReturnStatementParameter;
	private ReturnStatement moduleFunctionConditionalConditionalReturnStatement;
	private Expression moduleFunctionConditionalConditionalConditionalStatementCondition;
	private Block moduleFunctionConditionalConditionalConditionalStatementThenBlock;
	private Block moduleFunctionConditionalConditionalConditionalStatementElseBlock;
	private ConditionalStatement moduleFunctionConditionalConditionalConditionalStatement;
	private Expression moduleFunctionConditionalConditionalConditionalThenReturnStatementParameter;
	private ReturnStatement moduleFunctionConditionalConditionalConditionalThenReturnStatement;
	private Expression moduleFunctionConditionalConditionalConditionalElseReturnStatementParameter;
	private ReturnStatement moduleFunctionConditionalConditionalConditionalElseReturnStatement;

	// MODULE FUNCTION LOOP
	private Expression moduleFunctionLoopCondition;
	private Block moduleFunctionLoopBlock;
	private WhileLoop moduleFunctionLoop;
	private Expression moduleFunctionLoopReturnStatementParameter;
	private ReturnStatement moduleFunctionLoopReturnStatement;
	private Expression moduleFunctionLoopLoopCondition;
	private Block moduleFunctionLoopLoopBlock;
	private WhileLoop moduleFunctionLoopLoop;
	private Expression moduleFunctionLoopLoopLoopCondition;
	private Block moduleFunctionLoopLoopLoopBlock;
	private WhileLoop moduleFunctionLoopLoopLoop;
	private Expression moduleFunctionLoopLoopLoopReturnStatementParameter;
	private ReturnStatement moduleFunctionLoopLoopLoopReturnStatement;

	// helper
	private int counter = 0;

	public Position nextPosition() {
		return new Position("TestFile", this.counter++, 1);
	}

	@Before
	public void setUpAST() {

		// reset counter
		this.counter = 0;

		// VISITORS ---------------------------
		this.setParentVisitor = new SetParentVisitor();
		this.setParentVisitor.setStopOnFirstError(true);
		this.controlFlowVisitor = new ControlFlowVisitor();
		this.controlFlowVisitor.setStopOnFirstError(true);

		// AST --------------------------------

		// MODULE
		this.moduleBlock = new Block(nextPosition());
		this.moduleImports = new ArrayList<>(); // empty list is fine here ...
		this.moduleDeclaration =
		        new ModuleDeclaration(nextPosition(), new Identifier("TestModule"), this.moduleBlock, this.moduleImports);

		this.aPackage = new Package(new Identifier(""));
		this.aPackage.addModule(this.moduleDeclaration);

		// MODULE LOOP
		this.moduleLoopCondition = new BooleanLiteral(nextPosition(), true);
		this.moduleLoopBlock = new Block(nextPosition());
		this.moduleLoop = new WhileLoop(nextPosition(), this.moduleLoopCondition, this.moduleLoopBlock);
		this.moduleLoopContinueStatement = new ContinueStatement(nextPosition());
		this.moduleLoopBreakStatement = new BreakStatement(nextPosition());
		// add module loop to module
		this.moduleBlock.addStatement(this.moduleLoop);

		// MODULE LOOP LOOP
		this.moduleLoopLoopCondition = new BooleanLiteral(nextPosition(), true);
		this.moduleLoopLoopBlock = new Block(nextPosition());
		this.moduleLoopLoop = new WhileLoop(nextPosition(), this.moduleLoopLoopCondition, this.moduleLoopLoopBlock);
		this.moduleLoopLoopContinueStatement = new ContinueStatement(nextPosition());
		this.moduleLoopLoopBreakStatement = new BreakStatement(nextPosition());
		// add module loop loop to module loop
		this.moduleLoopBlock.addStatement(this.moduleLoopLoop);
		this.moduleLoopLoopConditionalStatementCondition = new BooleanLiteral(nextPosition(), true);
		this.moduleLoopLoopConditionalStatementThenBlock = new Block(nextPosition());
		this.moduleLoopLoopConditionalStatementElseBlock = new Block(nextPosition());
		this.moduleLoopLoopConditionalStatement =
		        new ConditionalStatement(nextPosition(), this.moduleLoopLoopConditionalStatementCondition,
		                this.moduleLoopLoopConditionalStatementThenBlock, this.moduleLoopLoopConditionalStatementElseBlock);
		this.moduleLoopLoopThenConditionalStatementCondition = new BooleanLiteral(nextPosition(), true);
		this.moduleLoopLoopThenConditionalStatementThenBlock = new Block(nextPosition());
		this.moduleLoopLoopThenConditionalStatementElseBlock = new Block(nextPosition());
		this.moduleLoopLoopThenConditionalStatement =
		        new ConditionalStatement(nextPosition(), this.moduleLoopLoopThenConditionalStatementCondition,
		                this.moduleLoopLoopThenConditionalStatementThenBlock,
		                this.moduleLoopLoopThenConditionalStatementElseBlock);
		this.moduleLoopLoopElseConditionalStatementCondition = new BooleanLiteral(nextPosition(), true);
		this.moduleLoopLoopElseConditionalStatementThenBlock = new Block(nextPosition());
		this.moduleLoopLoopElseConditionalStatementElseBlock = new Block(nextPosition());
		this.moduleLoopLoopElseConditionalStatement =
		        new ConditionalStatement(nextPosition(), this.moduleLoopLoopElseConditionalStatementCondition,
		                this.moduleLoopLoopElseConditionalStatementThenBlock,
		                this.moduleLoopLoopElseConditionalStatementElseBlock);

		// MODULE LOOP CONDITIONAL STATEMENT
		this.moduleLoopConditionalStatementCondition = new BooleanLiteral(nextPosition(), true);
		this.moduleLoopConditionalStatementThenBlock = new Block(nextPosition());
		this.moduleLoopConditionalStatementElseBlock = new Block(nextPosition());
		this.moduleLoopConditionalStatement =
		        new ConditionalStatement(nextPosition(), this.moduleLoopConditionalStatementCondition,
		                this.moduleLoopConditionalStatementThenBlock, this.moduleLoopConditionalStatementElseBlock);
		this.moduleLoopConditionalContinueStatement = new ContinueStatement(nextPosition());
		this.moduleLoopConditionalBreakStatement = new BreakStatement(nextPosition());
		// add module loop conditional statement to module loop
		this.moduleLoopBlock.addStatement(this.moduleLoopConditionalStatement);

		// MODULE CONDITIONAL STATEMENT
		this.moduleConditionalStatementCondition = new BooleanLiteral(nextPosition(), true);
		this.moduleConditionalStatementThenBlock = new Block(nextPosition());
		this.moduleConditionalStatementElseBlock = new Block(nextPosition());
		this.moduleConditionalStatement =
		        new ConditionalStatement(nextPosition(), this.moduleConditionalStatementCondition,
		                this.moduleConditionalStatementThenBlock, this.moduleConditionalStatementElseBlock);
		// add statements to module conditional statement (then)
		// add module conditional statement to module
		this.moduleBlock.addStatement(this.moduleConditionalStatement);

		// MODULE STATETEMENTS
		this.moduleContinueStatement = new ContinueStatement(nextPosition());
		this.moduleBreakStatement = new BreakStatement(nextPosition());
		this.moduleReturnStatementParameter = new StringLiteral(nextPosition(), "return");
		this.moduleReturnStatement = new ReturnStatement(nextPosition(), this.moduleReturnStatementParameter);
		// add statements to module
		// moduleBlock.addStatement(moduleContinueStatement);
		// moduleBlock.addStatement(moduleBreakStatement);
		// moduleBlock.addStatement(moduleReturnStatement);

		// MODULE FUNCTION
		this.moduleFunctionBlock = new Block(nextPosition());
		this.moduleFunctionParameter = new ArrayList<>();

		final TypeInstantiation returnType = TypeInstantiation.forTypeName("String").create();
		this.moduleFunctionDeclaration = new FunctionDeclaration(nextPosition(),
		        new Identifier("ModuleFunction"), this.moduleFunctionBlock,
		                this.moduleFunctionParameter, returnType);
		this.moduleFunctionReturnStatementParameter = new StringLiteral(nextPosition(), "return");
		this.moduleFunctionReturnStatement = new ReturnStatement(nextPosition(), this.moduleFunctionReturnStatementParameter);

		// MODULE FUNCTION PROCEDURE
		this.moduleFunctionProcedureBlock = new Block(nextPosition());
		this.moduleFunctionProcedureParameter = new ArrayList<>();
		this.moduleFunctionProcedureDeclaration =
		        new ProcedureDeclaration(nextPosition(), new Identifier("ModuleFunctionProcedure"),
		                this.moduleFunctionProcedureBlock, this.moduleFunctionProcedureParameter);
		this.moduleFunctionProcedureReturnStatementParameter = new StringLiteral(nextPosition(), "return");
		this.moduleFunctionProcedureReturnStatement =
		        new ReturnStatement(nextPosition(), this.moduleFunctionProcedureReturnStatementParameter);
		// add module function procedure to module function
		// moduleFunctionBlock.addDeclaration(moduleFunctionProcedureDeclaration);

		// MODULE FUNCTION CONDITIONAL STATEMENT
		this.moduleFunctionConditionalStatementCondition = new BooleanLiteral(nextPosition(), false);
		this.moduleFunctionConditionalStatementThenBlock = new Block(nextPosition());
		this.moduleFunctionConditionalStatementElseBlock = new Block(nextPosition());
		this.moduleFunctionConditionalStatement =
		        new ConditionalStatement(nextPosition(), this.moduleFunctionConditionalStatementCondition,
		                this.moduleFunctionConditionalStatementThenBlock, this.moduleFunctionConditionalStatementElseBlock);
		// add module function conditional statement to module function
		this.moduleFunctionBlock.addStatement(this.moduleFunctionConditionalStatement);
		this.moduleFunctionConditionalReturnThenStatementParameter = new StringLiteral(nextPosition(), "return");
		this.moduleFunctionConditionalReturnThenStatement =
		        new ReturnStatement(nextPosition(), this.moduleFunctionConditionalReturnThenStatementParameter);
		this.moduleFunctionConditionalReturnElseStatementParameter = new StringLiteral(nextPosition(), "return");
		this.moduleFunctionConditionalReturnElseStatement =
		        new ReturnStatement(nextPosition(), this.moduleFunctionConditionalReturnElseStatementParameter);
		this.moduleFunctionConditionalConditionalStatementCondition = new BooleanLiteral(nextPosition(), true);
		this.moduleFunctionConditionalConditionalStatementThenBlock = new Block(nextPosition());
		this.moduleFunctionConditionalConditionalStatementElseBlock = new Block(nextPosition());
		this.moduleFunctionConditionalConditionalStatement =
		        new ConditionalStatement(nextPosition(), this.moduleFunctionConditionalConditionalStatementCondition,
		                this.moduleFunctionConditionalConditionalStatementThenBlock,
		                this.moduleFunctionConditionalConditionalStatementElseBlock);
		// add module function conditional conditional statement to module
		// function conditional else block
		this.moduleFunctionConditionalStatementElseBlock.addStatement(this.moduleFunctionConditionalConditionalStatement);
		this.moduleFunctionConditionalConditionalReturnStatementParameter = new StringLiteral(nextPosition(), "return");
		this.moduleFunctionConditionalConditionalReturnStatement =
		        new ReturnStatement(nextPosition(), this.moduleFunctionConditionalConditionalReturnStatementParameter);
		this.moduleFunctionConditionalConditionalConditionalStatementCondition =
		        new BooleanLiteral(new Position("TestFile", 37, 1), false);
		this.moduleFunctionConditionalConditionalConditionalStatementThenBlock = new Block(nextPosition());
		this.moduleFunctionConditionalConditionalConditionalStatementElseBlock = new Block(nextPosition());
		this.moduleFunctionConditionalConditionalConditionalStatement =
		        new ConditionalStatement(nextPosition(),
		                this.moduleFunctionConditionalConditionalConditionalStatementCondition,
		                this.moduleFunctionConditionalConditionalConditionalStatementThenBlock,
		                this.moduleFunctionConditionalConditionalConditionalStatementElseBlock);
		// add module function conditional conditional conditional statement to
		// module function conditional conditional then block
		this.moduleFunctionConditionalConditionalStatementThenBlock.addStatement(this.moduleFunctionConditionalConditionalConditionalStatement);
		this.moduleFunctionConditionalConditionalConditionalThenReturnStatementParameter =
		        new StringLiteral(nextPosition(), "return");
		this.moduleFunctionConditionalConditionalConditionalThenReturnStatement =
		        new ReturnStatement(nextPosition(),
		                this.moduleFunctionConditionalConditionalConditionalThenReturnStatementParameter);
		this.moduleFunctionConditionalConditionalConditionalElseReturnStatementParameter =
		        new StringLiteral(nextPosition(), "return");
		this.moduleFunctionConditionalConditionalConditionalElseReturnStatement =
		        new ReturnStatement(nextPosition(),
		                this.moduleFunctionConditionalConditionalConditionalElseReturnStatementParameter);

		// MODULE FUNCTION LOOP
		this.moduleFunctionLoopCondition = new BooleanLiteral(nextPosition(), true);
		this.moduleFunctionLoopBlock = new Block(nextPosition());
		this.moduleFunctionLoop = new WhileLoop(nextPosition(), this.moduleFunctionLoopCondition, this.moduleFunctionLoopBlock);
		// add module function loop to module function
		this.moduleFunctionBlock.addStatement(this.moduleFunctionLoop);

		this.moduleFunctionLoopReturnStatementParameter = new StringLiteral(nextPosition(), "return");
		this.moduleFunctionLoopReturnStatement =
		        new ReturnStatement(nextPosition(), this.moduleFunctionLoopReturnStatementParameter);

		this.moduleFunctionLoopLoopCondition = new BooleanLiteral(nextPosition(), true);
		this.moduleFunctionLoopLoopBlock = new Block(nextPosition());
		this.moduleFunctionLoopLoop =
		        new WhileLoop(nextPosition(), this.moduleFunctionLoopLoopCondition, this.moduleFunctionLoopLoopBlock);
		// ass module function loop loop to module function loop block
		this.moduleFunctionLoopBlock.addStatement(this.moduleFunctionLoopLoop);

		this.moduleFunctionLoopLoopLoopCondition = new BooleanLiteral(nextPosition(), true);
		this.moduleFunctionLoopLoopLoopBlock = new Block(nextPosition());
		this.moduleFunctionLoopLoopLoop =
		        new WhileLoop(nextPosition(), this.moduleFunctionLoopLoopLoopCondition, this.moduleFunctionLoopLoopLoopBlock);
		// add module function loop loop loop to module function loop loop block
		this.moduleFunctionLoopLoopBlock.addStatement(this.moduleFunctionLoopLoopLoop);

		this.moduleFunctionLoopLoopLoopReturnStatementParameter = new StringLiteral(nextPosition(), "return");
		this.moduleFunctionLoopLoopLoopReturnStatement =
		        new ReturnStatement(nextPosition(), this.moduleFunctionLoopLoopLoopReturnStatementParameter);

	}

	@Test
	public void setUpASTTest() {
		assertNotNull("setParentVisitor is null", this.setParentVisitor);
		assertNotNull("controlFlowVisitor is null", this.controlFlowVisitor);

		// MODULE
		assertNotNull("moduleDeclaration is null", this.moduleDeclaration);
		assertNotNull("moduleBlock is null", this.moduleBlock);
		assertNotNull("moduleImports is null", this.moduleImports);
		assertNotNull("package is null", this.aPackage);

		// MODULE LOOP
		assertNotNull("moduleLoopCondition is null", this.moduleLoopCondition);
		assertNotNull("moduleLoopBlock is null", this.moduleLoopBlock);
		assertNotNull("moduleLoop is null", this.moduleLoop);
		assertNotNull("moduleLoopContinueStatement is null", this.moduleLoopContinueStatement);
		assertNotNull("moduleLoopBreakStatement is null", this.moduleLoopBreakStatement);
		assertNotNull("moduleLoopLoopCondition is null", this.moduleLoopLoopCondition);
		assertNotNull("moduleLoopLoopBlock is null", this.moduleLoopLoopBlock);

		// MODULE LOOP LOOP
		assertNotNull("moduleLoopLoop is null", this.moduleLoopLoop);
		assertNotNull("moduleLoopLoopContinueStatement is null", this.moduleLoopLoopContinueStatement);
		assertNotNull("moduleLoopLoopBreakStatement is null", this.moduleLoopLoopBreakStatement);
		assertNotNull(
		        "moduleLoopLoopConditionalStatementCondition is null",
		        this.moduleLoopLoopConditionalStatementCondition);
		assertNotNull(
		        "moduleLoopLoopConditionalStatementThenBlock is null",
		        this.moduleLoopLoopConditionalStatementThenBlock);
		assertNotNull(
		        "moduleLoopLoopConditionalStatementElseBlock is null",
		        this.moduleLoopLoopConditionalStatementElseBlock);
		assertNotNull("moduleLoopLoopConditionalStatement is null", this.moduleLoopLoopConditionalStatement);
		assertNotNull(
		        "moduleLoopLoopThenConditionalStatementCondition is null",
		        this.moduleLoopLoopThenConditionalStatementCondition);
		assertNotNull(
		        "moduleLoopLoopThenConditionalStatementThenBlock is null",
		        this.moduleLoopLoopThenConditionalStatementThenBlock);
		assertNotNull(
		        "moduleLoopLoopThenConditionalStatementElseBlock is null",
		        this.moduleLoopLoopThenConditionalStatementElseBlock);
		assertNotNull("moduleLoopLoopThenConditionalStatement is null", this.moduleLoopLoopThenConditionalStatement);
		assertNotNull(
		        "moduleLoopLoopElseConditionalStatementCondition is null",
		        this.moduleLoopLoopElseConditionalStatementCondition);
		assertNotNull(
		        "moduleLoopLoopElseConditionalStatementThenBlock is null",
		        this.moduleLoopLoopElseConditionalStatementThenBlock);
		assertNotNull(
		        "moduleLoopLoopElseConditionalStatementElseBlock is null",
		        this.moduleLoopLoopElseConditionalStatementElseBlock);
		assertNotNull("moduleLoopLoopElseConditionalStatement is null", this.moduleLoopLoopElseConditionalStatement);

		// MODULE LOOP CONDITIONAL STATEMENT
		assertNotNull("moduleLoopConditionalStatementCondition is null", this.moduleLoopConditionalStatementCondition);
		assertNotNull("moduleLoopConditionalStatementThenBlock is null", this.moduleLoopConditionalStatementThenBlock);
		assertNotNull("moduleLoopConditionalStatementElseBlock is null", this.moduleLoopConditionalStatementElseBlock);
		assertNotNull("moduleLoopConditionalStatement is null", this.moduleLoopConditionalStatement);
		assertNotNull("moduleLoopConditionalContinueStatement is null", this.moduleLoopConditionalContinueStatement);
		assertNotNull("moduleLoopConditionalBreakStatement is null", this.moduleLoopConditionalBreakStatement);

		// MODULE CONDITIONAL STATEMENT
		assertNotNull("moduleConditionalStatementCondition is null", this.moduleConditionalStatementCondition);
		assertNotNull("moduleConditionalStatementThenBlock is null", this.moduleConditionalStatementThenBlock);
		assertNotNull("moduleConditionalStatementElseBlock is null", this.moduleConditionalStatementElseBlock);
		assertNotNull("moduleConditionalStatement is null", this.moduleConditionalStatement);

		// MODULE STATETEMENTS
		assertNotNull("moduleContinueStatement is null", this.moduleContinueStatement);
		assertNotNull("moduleBreakStatement is null", this.moduleBreakStatement);
		assertNotNull("moduleReturnStatementParameter is null", this.moduleReturnStatementParameter);
		assertNotNull("moduleReturnStatement is null", this.moduleReturnStatement);

		// MODULE FUNCTION
		assertNotNull("moduleFunctionBlock is null", this.moduleFunctionBlock);
		assertNotNull("moduleFunctionParameter is null", this.moduleFunctionParameter);
		assertNotNull("moduleFunctionDeclaration is null", this.moduleFunctionDeclaration);
		assertNotNull("moduleFunctionReturnStatementParameter is null", this.moduleFunctionReturnStatementParameter);
		assertNotNull("moduleFunctionReturnStatement is null", this.moduleFunctionReturnStatement);

		// MODULE FUNCTION PROCEDURE
		assertNotNull("moduleFunctionProcedureBlock is null", this.moduleFunctionProcedureBlock);
		assertNotNull("moduleFunctionProcedureParameter is null", this.moduleFunctionProcedureParameter);
		assertNotNull("moduleFunctionProcedureDeclaration is null", this.moduleFunctionProcedureDeclaration);
		assertNotNull(
		        "moduleFunctionProcedureReturnStatementParameter is null",
		        this.moduleFunctionProcedureReturnStatementParameter);
		assertNotNull("moduleFunctionProcedureReturnStatement is null", this.moduleFunctionProcedureReturnStatement);

		// MODULE FUNCTION CONDITIONAL STATEMENT
		assertNotNull(
		        "moduleFunctionConditionalStatementCondition is null",
		        this.moduleFunctionConditionalStatementCondition);
		assertNotNull(
		        "moduleFunctionConditionalStatementThenBlock is null",
		        this.moduleFunctionConditionalStatementThenBlock);
		assertNotNull(
		        "moduleFunctionConditionalStatementElseBlock is null",
		        this.moduleFunctionConditionalStatementElseBlock);
		assertNotNull("moduleFunctionConditionalStatement is null", this.moduleFunctionConditionalStatement);
		assertNotNull(
		        "moduleFunctionConditionalReturnThenStatementParameter is null",
		        this.moduleFunctionConditionalReturnThenStatementParameter);
		assertNotNull(
		        "moduleFunctionConditionalReturnThenStatement is null",
		        this.moduleFunctionConditionalReturnThenStatement);
		assertNotNull(
		        "moduleFunctionConditionalReturnElseStatementParameter is null",
		        this.moduleFunctionConditionalReturnElseStatementParameter);
		assertNotNull(
		        "moduleFunctionConditionalReturnElseStatement is null",
		        this.moduleFunctionConditionalReturnElseStatement);
		assertNotNull(
		        "moduleFunctionConditionalConditionalStatementCondition is null",
		        this.moduleFunctionConditionalConditionalStatementCondition);
		assertNotNull(
		        "moduleFunctionConditionalConditionalStatementThenBlock is null",
		        this.moduleFunctionConditionalConditionalStatementThenBlock);
		assertNotNull(
		        "moduleFunctionConditionalConditionalStatementElseBlock is null",
		        this.moduleFunctionConditionalConditionalStatementElseBlock);
		assertNotNull(
		        "moduleFunctionConditionalConditionalStatement is null",
		        this.moduleFunctionConditionalConditionalStatement);
		assertNotNull(
		        "moduleFunctionConditionalConditionalReturnStatementParameter is null",
		        this.moduleFunctionConditionalConditionalReturnStatementParameter);
		assertNotNull(
		        "moduleFunctionConditionalConditionalReturnStatement is null",
		        this.moduleFunctionConditionalConditionalReturnStatement);
		assertNotNull(
		        "moduleFunctionConditionalConditionalConditionalStatementCondition is null",
		        this.moduleFunctionConditionalConditionalConditionalStatementCondition);
		assertNotNull(
		        "moduleFunctionConditionalConditionalConditionalStatementThenBlock is null",
		        this.moduleFunctionConditionalConditionalConditionalStatementThenBlock);
		assertNotNull(
		        "moduleFunctionConditionalConditionalConditionalStatementElseBlock is null",
		        this.moduleFunctionConditionalConditionalConditionalStatementElseBlock);
		assertNotNull(
		        "moduleFunctionConditionalConditionalConditionalStatement is null",
		        this.moduleFunctionConditionalConditionalConditionalStatement);
		assertNotNull(
		        "moduleFunctionConditionalConditionalConditionalThenReturnStatementParameter is null",
		        this.moduleFunctionConditionalConditionalConditionalThenReturnStatementParameter);
		assertNotNull(
		        "moduleFunctionConditionalConditionalConditionalThenReturnStatement is null",
		        this.moduleFunctionConditionalConditionalConditionalThenReturnStatement);
		assertNotNull(
		        "moduleFunctionConditionalConditionalConditionalElseReturnStatementParameter is null",
		        this.moduleFunctionConditionalConditionalConditionalElseReturnStatementParameter);
		assertNotNull(
		        "moduleFunctionConditionalConditionalConditionalElseReturnStatement is null",
		        this.moduleFunctionConditionalConditionalConditionalElseReturnStatement);

		// MODULE FUNCTION LOOP
		assertNotNull("moduleFunctionLoopCondition is null", this.moduleFunctionLoopCondition);
		assertNotNull("moduleFunctionLoopBlock is null", this.moduleFunctionLoopBlock);
		assertNotNull("moduleFunctionLoop is null", this.moduleFunctionLoop);
		assertNotNull("moduleFunctionLoopReturnStatementParameter is null", this.moduleFunctionLoopReturnStatementParameter);
		assertNotNull("moduleFunctionLoopReturnStatement is null", this.moduleFunctionLoopReturnStatement);
		assertNotNull("moduleFunctionLoopLoopCondition is null", this.moduleFunctionLoopLoopCondition);
		assertNotNull("moduleFunctionLoopLoopBlock is null", this.moduleFunctionLoopLoopBlock);
		assertNotNull("moduleFunctionLoopLoop is null", this.moduleFunctionLoopLoop);
		assertNotNull("moduleFunctionLoopLoopLoopCondition is null", this.moduleFunctionLoopLoopLoopCondition);
		assertNotNull("moduleFunctionLoopLoopLoopBlock is null", this.moduleFunctionLoopLoopLoopBlock);
		assertNotNull("moduleFunctionLoopLoopLoop is null", this.moduleFunctionLoopLoopLoop);
		assertNotNull(
		        "moduleFunctionLoopLoopLoopReturnStatementParameter is null",
		        this.moduleFunctionLoopLoopLoopReturnStatementParameter);
		assertNotNull("moduleFunctionLoopLoopLoopReturnStatement is null", this.moduleFunctionLoopLoopLoopReturnStatement);
	}

	/** Creates a continue statement inside of a while loop. */
	@Test
	public void continueStatementTest01() {
		// add previously defined statement to module loop
		this.moduleLoopBlock.addStatement(this.moduleLoopContinueStatement);

		// control flow visitor should not complain about it, since the
		// structure is
		// valid
		this.setParentVisitor.visit(this.aPackage);
		this.controlFlowVisitor.visit(this.aPackage);
	}

	/** Creates a continue statement inside of a nested while loop. */
	@Test
	public void continueStatementTest02() {
		// add previously defined statement to module loop
		this.moduleLoopLoopBlock.addStatement(this.moduleLoopLoopContinueStatement);

		// control flow visitor should not complain about it, since the
		// structure is
		// valid
		this.setParentVisitor.visit(this.aPackage);
		this.controlFlowVisitor.visit(this.aPackage);
	}

	/** Creates a continue statement inside of a conditional statement within a while loop */
	@Test
	public void continueStatementTest03() {
		// add statements to module loop conditional statement (then)
		this.moduleLoopConditionalStatementThenBlock.addStatement(this.moduleLoopConditionalContinueStatement);

		// control flow visitor should not complain about it, since the
		// structure is
		// valid
		this.setParentVisitor.visit(this.aPackage);
		this.controlFlowVisitor.visit(this.aPackage);
	}

	/** Create continue statement outside of a while loop */
	@Test(expected = InvalidControlFlowException.class)
	public void continueStatementTest04() {
		this.moduleBlock.addStatement(this.moduleContinueStatement);

		// control flow visitor should throw an exception now, since the
		// structure is invalid
		this.setParentVisitor.visit(this.aPackage);
		this.controlFlowVisitor.visit(this.aPackage);
	}

	/** Create break statement outside of a while loop */
	@Test(expected = InvalidControlFlowException.class)
	public void breakStatementTest04() {
		this.moduleBlock.addStatement(this.moduleBreakStatement);

		// control flow visitor should throw an exception now, since the
		// structure is invalid
		this.setParentVisitor.visit(this.aPackage);
		this.controlFlowVisitor.visit(this.aPackage);
	}

	/** Creates return statement outside of a function block */
	@Test(expected = InvalidControlFlowException.class)
	public void returnStatementTest01() {
		this.moduleBlock.addStatement(this.moduleReturnStatement);

		// control flow visitor should throw an exception now, since the
		// structure is invalid
		this.setParentVisitor.visit(this.aPackage);
		this.controlFlowVisitor.visit(this.aPackage);
	}

	/** Creates a return statement inside of a function block */
	@Test
	public void returnStatementTest02() {
		// add function to module
		this.moduleBlock.addDeclaration(this.moduleFunctionDeclaration);
		// ad a return statement to module function block
		this.moduleFunctionBlock.addStatement(this.moduleFunctionReturnStatement);

		// control flow visitor should not complain about it, since the
		// structure is
		// valid
		this.setParentVisitor.visit(this.aPackage);
		this.controlFlowVisitor.visit(this.aPackage);
	}

	/** Creates a return statement inside of a nested procedure declaration within a function block */
	@Test
	public void returnStatementTest03() {
		// add function to module
		this.moduleBlock.addDeclaration(this.moduleFunctionDeclaration);
		// add return statement to procedure
		this.moduleFunctionProcedureBlock.addStatement(this.moduleFunctionProcedureReturnStatement);
		// add procedure to function
		this.moduleFunctionBlock.addDeclaration(this.moduleFunctionProcedureDeclaration);

		// start visitors and expect InvalidControlFlowException, since there's
		// no guarantee, that the function always executes a return statement
		boolean invalidControlFlow = false;
		try {
			this.setParentVisitor.visit(this.aPackage);
			this.controlFlowVisitor.visit(this.aPackage);
		} catch (InvalidControlFlowException icfe) {
			invalidControlFlow = true;
		}
		// there should have been an InvalidControlFlowException during the
		// visit ...
		assertTrue("ReturnStatements needed inside the moduleFunction", invalidControlFlow);

		// now, add another return statement to the surrounding function
		this.moduleFunctionBlock.addStatement(this.moduleFunctionReturnStatement);

		// control flow visitor should not complain anymore, since the
		// structure is now valid
		this.setParentVisitor.visit(this.aPackage);
		this.controlFlowVisitor.visit(this.aPackage);
	}

	/** Creates a return statement inside of a conditional statement within a function block */
	@Test
	public void returnStatementTest04() {
		// add function to module
		this.moduleBlock.addDeclaration(this.moduleFunctionDeclaration);
		// add module function conditional return statement to module function
		// conditional statement
		this.moduleFunctionConditionalStatementElseBlock.addStatement(this.moduleFunctionConditionalReturnElseStatement);
		// add module function conditional statement to module function block
		this.moduleFunctionBlock.addStatement(this.moduleFunctionConditionalStatement);

		// control flow visitor should definitely complain about it, since there
		// is only a return statement in the else block, but neither in the then
		// block, nor in the function itself.
		boolean invalidControlFlow = false;
		try {
			this.setParentVisitor.visit(this.aPackage);
			this.controlFlowVisitor.visit(this.aPackage);
		} catch (InvalidControlFlowException icfe) {
			invalidControlFlow = true;
		}
		// there should have been an InvalidControlFlowException during the
		// visit ...
		assertTrue("ReturnStatements does not need to be found inside both conditional branches", invalidControlFlow);

		// now, add another return statement to the surrounding function
		this.moduleFunctionBlock.addStatement(this.moduleFunctionReturnStatement);

		// run visitors again
		this.setParentVisitor.visit(this.aPackage);
		this.controlFlowVisitor.visit(this.aPackage);

		// The controlFlowVisitor should not complain about anything anymore,
		// since the structure is now valid.
	}

	/** Creates a return statement inside of a conditional statement within a function block again, but resolve errors by
	 * adding another conditional return statement */
	@Test
	public void returnStatementTest05() {
		// add function to module
		this.moduleBlock.addDeclaration(this.moduleFunctionDeclaration);
		// add module function conditional return statement to module function
		// conditional statement
		this.moduleFunctionConditionalStatementElseBlock.addStatement(this.moduleFunctionConditionalReturnElseStatement);
		// add module function conditional statement to module function block
		this.moduleFunctionBlock.addStatement(this.moduleFunctionConditionalStatement);

		// control flow visitor should definitely complain about it, since there
		// is only a return statement in the else block, but neither in the then
		// block, nor in the function itself.
		boolean invalidControlFlow = false;
		try {
			this.setParentVisitor.visit(this.aPackage);
			this.controlFlowVisitor.visit(this.aPackage);
		} catch (InvalidControlFlowException icfe) {
			invalidControlFlow = true;
		}
		// there should have been an InvalidControlFlowException during the
		// visit ...
		assertTrue("ReturnStatements does not need to be found inside both conditional branches", invalidControlFlow);

		// add return statement to conditional statements then block
		this.moduleFunctionConditionalStatementThenBlock.addStatement(this.moduleFunctionConditionalReturnThenStatement);

		// run visitors again
		this.setParentVisitor.visit(this.aPackage);
		this.controlFlowVisitor.visit(this.aPackage);

		// The controlFlowVisitor should not complain about anything, since the
		// structure is valid.
	}

	/** Creates return statement inside of a 3-times nested conditional statement within a function block */
	@Test
	public void returnStatementTest06() {
		// add function to module
		this.moduleBlock.addDeclaration(this.moduleFunctionDeclaration);
		// add module function conditional conditional conditional return
		// statement to module function conditional conditional conditional then
		// block
		this.moduleFunctionConditionalConditionalConditionalStatementElseBlock.addStatement(this.moduleFunctionConditionalConditionalConditionalElseReturnStatement);

		// control flow should be invalid now, since there's no global return
		// statement
		boolean invalidControlFlow = false;
		try {
			this.setParentVisitor.visit(this.aPackage);
			this.controlFlowVisitor.visit(this.aPackage);
		} catch (InvalidControlFlowException icfe) {
			invalidControlFlow = true;
		}
		// there should have been an InvalidControlFlowException during the
		// visit ...
		assertTrue("ReturnStatements does not need to be found inside both conditional branches", invalidControlFlow);

		// resolve these errors by adding another return statement to each
		// conditional branch
		this.moduleFunctionConditionalConditionalConditionalStatementThenBlock.addStatement(this.moduleFunctionConditionalConditionalConditionalThenReturnStatement);
		this.moduleFunctionConditionalConditionalStatementElseBlock.addStatement(this.moduleFunctionConditionalConditionalReturnStatement);
		this.moduleFunctionConditionalStatementThenBlock.addStatement(this.moduleFunctionConditionalReturnThenStatement);

		// everything should be just fine now
		this.setParentVisitor.visit(this.aPackage);
		this.controlFlowVisitor.visit(this.aPackage);
	}

	/** Creates return statement inside a while loop within a function block */
	@Test
	public void returnStatementTest07() {
		// add function to module
		this.moduleBlock.addDeclaration(this.moduleFunctionDeclaration);
		// add module function loop return statement to module function loop
		// block
		this.moduleFunctionLoopBlock.addStatement(this.moduleFunctionLoopReturnStatement);

		// control flow should be invalid now, since there's no global return
		// statement
		boolean invalidControlFlow = false;
		try {
			this.setParentVisitor.visit(this.aPackage);
			this.controlFlowVisitor.visit(this.aPackage);
		} catch (InvalidControlFlowException icfe) {
			invalidControlFlow = true;
		}
		// there should have been an InvalidControlFlowException during the
		// visit ...
		assertTrue("ReturnStatements does not need to be found inside both conditional branches", invalidControlFlow);

		// resolve errors by adding a return statement to module function
		this.moduleFunctionBlock.addStatement(this.moduleFunctionReturnStatement);

		// everything should be just fine now
		this.setParentVisitor.visit(this.aPackage);
		this.controlFlowVisitor.visit(this.aPackage);
	}

	/** Create return statement inside of a 3-times nested while loop within a function block */
	@Test
	public void returnStatementTest08() {
		// add function to module
		this.moduleBlock.addDeclaration(this.moduleFunctionDeclaration);
		// add module function loop loop loop return statement to module
		// function loop loop loop block
		this.moduleFunctionLoopLoopLoopBlock.addStatement(this.moduleFunctionLoopLoopLoopReturnStatement);

		// control flow should be invalid now, since there's no global return
		// statement
		boolean invalidControlFlow = false;
		try {
			this.setParentVisitor.visit(this.aPackage);
			this.controlFlowVisitor.visit(this.aPackage);
		} catch (InvalidControlFlowException icfe) {
			invalidControlFlow = true;
		}
		// there should have been an InvalidControlFlowException during the
		// visit ...
		assertTrue("ReturnStatements does not need to be found inside both conditional branches", invalidControlFlow);

		// resolve errors by adding a return statement to module function
		this.moduleFunctionBlock.addStatement(this.moduleFunctionReturnStatement);

		// everything should be just fine now
		this.setParentVisitor.visit(this.aPackage);
		this.controlFlowVisitor.visit(this.aPackage);
	}

	/** Creates a conditional statement inside a while loop */
	@Test
	public void conditionalStatementTest01() {
		this.moduleLoopBlock.addStatement(this.moduleLoopConditionalStatement);

		// control flow visitor should not complain about it, since the
		// structure is valid
		this.setParentVisitor.visit(this.aPackage);
		this.controlFlowVisitor.visit(this.aPackage);
	}

	/** Creates a conditional statement inside a conditional statements then block */
	@Test
	public void conditionalStatementTest02() {
		this.moduleConditionalStatementThenBlock.addStatement(this.moduleLoopConditionalStatement);

		// control flow visitor should not complain about it, since the
		// structure is valid
		this.setParentVisitor.visit(this.aPackage);
		this.controlFlowVisitor.visit(this.aPackage);
	}

	/** Creates a conditional statement inside a conditional statements else block */
	@Test
	public void conditionalStatementTest03() {
		this.moduleConditionalStatementElseBlock.addStatement(this.moduleLoopConditionalStatement);

		// control flow visitor should not complain about it, since the
		// structure is valid
		this.setParentVisitor.visit(this.aPackage);
		this.controlFlowVisitor.visit(this.aPackage);
	}

	/** Creates a conditional statement inside of a nested while loop */
	@Test
	public void conditionalStatementTest04() {
		this.moduleLoopLoopBlock.addStatement(this.moduleLoopLoopConditionalStatement);

		// everything should be just fine
		this.setParentVisitor.visit(this.aPackage);
		this.controlFlowVisitor.visit(this.aPackage);
	}

	/** Creates conditional statement inside of a nested conditional statement within a while loop */
	@Test
	public void conditionalStatementTest05() {
		// add a conditional statement inside a nested while loop
		this.moduleLoopLoopBlock.addStatement(this.moduleLoopLoopConditionalStatement);
		// add some more conditional statements to the one above
		this.moduleLoopLoopConditionalStatementThenBlock.addStatement(this.moduleLoopLoopThenConditionalStatement);
		this.moduleLoopLoopConditionalStatementElseBlock.addStatement(this.moduleLoopLoopElseConditionalStatement);

		// everything should be just fine
		this.setParentVisitor.visit(this.aPackage);
		this.controlFlowVisitor.visit(this.aPackage);
	}
}
