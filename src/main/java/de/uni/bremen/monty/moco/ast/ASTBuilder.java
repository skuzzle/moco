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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.io.FilenameUtils;

import de.uni.bremen.monty.moco.antlr.MontyBaseVisitor;
import de.uni.bremen.monty.moco.antlr.MontyParser;
import de.uni.bremen.monty.moco.antlr.MontyParser.AssignmentContext;
import de.uni.bremen.monty.moco.antlr.MontyParser.BreakStmContext;
import de.uni.bremen.monty.moco.antlr.MontyParser.ClassDeclarationContext;
import de.uni.bremen.monty.moco.antlr.MontyParser.CompoundAssignmentContext;
import de.uni.bremen.monty.moco.antlr.MontyParser.DefaultParameterContext;
import de.uni.bremen.monty.moco.antlr.MontyParser.ElifContext;
import de.uni.bremen.monty.moco.antlr.MontyParser.ExpressionContext;
import de.uni.bremen.monty.moco.antlr.MontyParser.FunctionCallContext;
import de.uni.bremen.monty.moco.antlr.MontyParser.FunctionDeclarationContext;
import de.uni.bremen.monty.moco.antlr.MontyParser.IfStatementContext;
import de.uni.bremen.monty.moco.antlr.MontyParser.IndependentDeclarationContext;
import de.uni.bremen.monty.moco.antlr.MontyParser.LiteralContext;
import de.uni.bremen.monty.moco.antlr.MontyParser.MemberAccessStmtContext;
import de.uni.bremen.monty.moco.antlr.MontyParser.MemberDeclarationContext;
import de.uni.bremen.monty.moco.antlr.MontyParser.ParameterListContext;
import de.uni.bremen.monty.moco.antlr.MontyParser.PrimaryContext;
import de.uni.bremen.monty.moco.antlr.MontyParser.ProcedureDeclarationContext;
import de.uni.bremen.monty.moco.antlr.MontyParser.RaiseStmContext;
import de.uni.bremen.monty.moco.antlr.MontyParser.ReturnStmContext;
import de.uni.bremen.monty.moco.antlr.MontyParser.SkipStmContext;
import de.uni.bremen.monty.moco.antlr.MontyParser.StatementBlockContext;
import de.uni.bremen.monty.moco.antlr.MontyParser.StatementContext;
import de.uni.bremen.monty.moco.antlr.MontyParser.TryStatementContext;
import de.uni.bremen.monty.moco.antlr.MontyParser.TypeContext;
import de.uni.bremen.monty.moco.antlr.MontyParser.VariableDeclarationContext;
import de.uni.bremen.monty.moco.antlr.MontyParser.WhileStatementContext;
import de.uni.bremen.monty.moco.ast.declaration.ClassDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.Declaration;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ModuleDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration.DeclarationType;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.expression.CastExpression;
import de.uni.bremen.monty.moco.ast.expression.ConditionalExpression;
import de.uni.bremen.monty.moco.ast.expression.Expression;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;
import de.uni.bremen.monty.moco.ast.expression.IsExpression;
import de.uni.bremen.monty.moco.ast.expression.MemberAccess;
import de.uni.bremen.monty.moco.ast.expression.ParentExpression;
import de.uni.bremen.monty.moco.ast.expression.SelfExpression;
import de.uni.bremen.monty.moco.ast.expression.VariableAccess;
import de.uni.bremen.monty.moco.ast.expression.literal.ArrayLiteral;
import de.uni.bremen.monty.moco.ast.expression.literal.BooleanLiteral;
import de.uni.bremen.monty.moco.ast.expression.literal.CharacterLiteral;
import de.uni.bremen.monty.moco.ast.expression.literal.FloatLiteral;
import de.uni.bremen.monty.moco.ast.expression.literal.IntegerLiteral;
import de.uni.bremen.monty.moco.ast.expression.literal.StringLiteral;
import de.uni.bremen.monty.moco.ast.statement.Assignment;
import de.uni.bremen.monty.moco.ast.statement.BreakStatement;
import de.uni.bremen.monty.moco.ast.statement.ConditionalStatement;
import de.uni.bremen.monty.moco.ast.statement.RaiseStatement;
import de.uni.bremen.monty.moco.ast.statement.ReturnStatement;
import de.uni.bremen.monty.moco.ast.statement.SkipStatement;
import de.uni.bremen.monty.moco.ast.statement.Statement;
import de.uni.bremen.monty.moco.ast.statement.TryStatement;
import de.uni.bremen.monty.moco.ast.statement.WhileLoop;

public class ASTBuilder extends MontyBaseVisitor<ASTNode> {
	private final String fileName;
	private final Stack<Block> currentBlocks;
	private VariableDeclaration.DeclarationType currentVariableContext;
	private ProcedureDeclaration.DeclarationType currentProcedureContext;

	public ASTBuilder(String fileName) {
		this.fileName = fileName;
		this.currentBlocks = new Stack<>();
	}

	private Position position(Token idSymbol) {
		return new Position(this.fileName, idSymbol.getLine(), idSymbol.getCharPositionInLine());
	}

	private String getText(TerminalNode identifier) {
		return identifier.getSymbol().getText();
	}

	@Override
	public ASTNode visitModuleDeclaration(@NotNull MontyParser.ModuleDeclarationContext ctx) {
		Block block = new Block(position(ctx.getStart()));
		ModuleDeclaration module =
		        new ModuleDeclaration(position(ctx.getStart()),
		                new Identifier(FilenameUtils.getBaseName(this.fileName)), block, new ArrayList<Import>());
		this.currentBlocks.push(block);

		for (MontyParser.ImportLineContext imp : ctx.importLine()) {

			module.getImports().add(
			        new Import(position(imp.getStart()), new ResolvableIdentifier(getText(imp.Identifier()))));
		}

		for (ClassDeclarationContext classDeclarationContext : ctx.classDeclaration()) {
			ClassDeclaration classDecl = (ClassDeclaration) visit(classDeclarationContext);
			block.addDeclaration(classDecl);
		}
		addStatementsToBlock(block, ctx.statement());
		this.currentBlocks.pop();
		return module;
	}

	@Override
	public ASTNode visitAssignment(@NotNull AssignmentContext ctx) {
		Assignment assignment =
		        new Assignment(position(ctx.getStart()), (Expression) visit(ctx.left), (Expression) visit(ctx.right));
		return assignment;
	}

	@Override
	public ASTNode visitCompoundAssignment(CompoundAssignmentContext ctx) {
		Expression expr =
		        binaryExpression(
		                position(ctx.getStart()),
		                ctx.compoundSymbol().operator.getText().substring(0, 1),
		                ctx.left,
		                ctx.right);

		return new Assignment(position(ctx.getStart()), (Expression) visit(ctx.left), expr);
	}

	@Override
	public ASTNode visitVariableDeclaration(@NotNull VariableDeclarationContext ctx) {
		final String typeName;
		if (ctx.type().ClassIdentifier() == null) {
            typeName = "?";
		} else {
			typeName = ctx.type().ClassIdentifier().toString();
		}
		ResolvableIdentifier type = new ResolvableIdentifier(typeName);
        final VariableDeclaration decl = new VariableDeclaration(
                position(ctx.getStart()), new Identifier(getText(ctx.Identifier())), type,
		        this.currentVariableContext);

        if (ctx.type().typeList() != null) {
            for (final TypeContext t : ctx.type().typeList().type()) {
                final ResolvableIdentifier ri = ResolvableIdentifier.of(t.getText());
                decl.addActualTypeArgument(ri);
            }

        }
        return decl;
	}

	@Override
	public ASTNode visitFunctionCall(FunctionCallContext ctx) {
		ArrayList<Expression> arguments = new ArrayList<>();
		String identifier;
		if (ctx.Identifier() == null) {
			identifier = ctx.ClassIdentifier().getText();
		} else {
			identifier = ctx.Identifier().getText();
		}
		FunctionCall func = new FunctionCall(position(ctx.getStart()), new ResolvableIdentifier(identifier), arguments);
		if (ctx.expressionList() != null) {
			for (ExpressionContext exprC : ctx.expressionList().expression()) {

				ASTNode expr = visit(exprC);
				if (expr instanceof Expression) {

					arguments.add((Expression) expr);
				}
			}
		}
		return func;
	}

	private void buildDefaultProcedures(boolean functionDeclaration, List<DefaultParameterContext> defaultParameter,
	        List<VariableDeclaration> allVariableDeclarations, List<VariableDeclaration> params,
	        List<Expression> defaultExpression, List<VariableDeclaration> defaultVariableDeclaration,
	        Identifier identifier, Token token, String returnTypeName, DeclarationType declarationTypeCopy) {

		for (int defaultParameterIdx = 0; defaultParameterIdx < defaultParameter.size(); defaultParameterIdx++) {
			Block block = new Block(position(token));
			List<Expression> l = new ArrayList<>();
			for (int variableDeclarationIdy = 0; variableDeclarationIdy < allVariableDeclarations.size(); variableDeclarationIdy++) {
				if (variableDeclarationIdy >= params.size() + defaultParameterIdx) {
					l.add(defaultExpression.get(variableDeclarationIdy - params.size()));
				} else if (variableDeclarationIdy < params.size()) {
					l.add(new VariableAccess(position(token), new ResolvableIdentifier(params.get(
					        variableDeclarationIdy).getIdentifier().getSymbol())));
				} else {
					VariableDeclaration variableDeclaration =
					        defaultVariableDeclaration.get(variableDeclarationIdy - params.size());
					l.add(new VariableAccess(position(token), new ResolvableIdentifier(
					        variableDeclaration.getIdentifier().getSymbol())));
				}
			}

			List<VariableDeclaration> subParams =
			        allVariableDeclarations.subList(0, params.size() + defaultParameterIdx);

			Expression expression =
			        new FunctionCall(position(token), new ResolvableIdentifier(identifier.getSymbol()), l);

			if (declarationTypeCopy == ProcedureDeclaration.DeclarationType.METHOD) {
				expression = new MemberAccess(position(token), new SelfExpression(position(token)), expression);
			}

			ProcedureDeclaration procDecl1;
			if (functionDeclaration) {
				block.addStatement(new ReturnStatement(new Position(), expression));

				procDecl1 =
				        new FunctionDeclaration(position(token), identifier, block, subParams, declarationTypeCopy,
				                new ResolvableIdentifier(returnTypeName));
			} else {
				block.addStatement((Statement) expression);
				block.addStatement(new ReturnStatement(new Position(), null));
				procDecl1 =
				        new ProcedureDeclaration(position(token), identifier, block, subParams, declarationTypeCopy);
			}
			this.currentBlocks.peek().addDeclaration(procDecl1);
		}
	}

	private ProcedureDeclaration buildProcedures(boolean functionDeclaration,
	        ParameterListContext parameterListContext, Token token, TypeContext typeContext,
	        StatementBlockContext statementBlockContext, Identifier identifier) {

		ProcedureDeclaration.DeclarationType declarationTypeCopy = this.currentProcedureContext;
		List<VariableDeclaration> params = parameterListToVarDeclList(parameterListContext);
		List<DefaultParameterContext> defaultParameter = defaultParameterListToVarDeclList(parameterListContext);

		List<VariableDeclaration> defaultVariableDeclaration = new ArrayList<>();
		List<Expression> defaultExpression = new ArrayList<>();
		for (DefaultParameterContext context : defaultParameter) {
			defaultVariableDeclaration.add((VariableDeclaration) visit(context.variableDeclaration()));
			defaultExpression.add((Expression) visit(context.expression()));
		}

		List<VariableDeclaration> allVariableDeclarations = new ArrayList<>();
		allVariableDeclarations.addAll(params);
		allVariableDeclarations.addAll(defaultVariableDeclaration);

		final String returnTypeName;
		if (typeContext == null) {
			// procedure
			returnTypeName = null;
		} else if (typeContext.ClassIdentifier() != null) {
			// explicit type
			returnTypeName = typeContext.ClassIdentifier().getText();
		} else {
			// type var
            returnTypeName = "?";
		}

		buildDefaultProcedures(
		        functionDeclaration,
		        defaultParameter,
		        allVariableDeclarations,
		        params,
		        defaultExpression,
		        defaultVariableDeclaration,
		        identifier,
		        token,
		        returnTypeName,
		        declarationTypeCopy);

		ProcedureDeclaration procDecl2;

		if (functionDeclaration) {
			procDecl2 =
			        new FunctionDeclaration(position(token), identifier, (Block) visit(statementBlockContext),
			                allVariableDeclarations, declarationTypeCopy, new ResolvableIdentifier(returnTypeName));
		} else {
			procDecl2 =
			        new ProcedureDeclaration(position(token), identifier, (Block) visit(statementBlockContext),
			                allVariableDeclarations, declarationTypeCopy);
		}
		return procDecl2;
	}

	@Override
	public ASTNode visitFunctionDeclaration(FunctionDeclarationContext ctx) {
		Identifier identifier;
		if (ctx.binaryOperation() == null) {
			identifier = new Identifier(getText(ctx.Identifier()));
		} else {
			identifier = new Identifier("operator" + ctx.binaryOperation().getText());
		}

		return buildProcedures(true, ctx.parameterList(), ctx.getStart(), ctx.type(), ctx.statementBlock(), identifier);
	}

	@Override
	public ASTNode visitProcedureDeclaration(ProcedureDeclarationContext ctx) {
		ProcedureDeclaration proc =
		        buildProcedures(false, ctx.parameterList(), ctx.start, null, ctx.statementBlock(), new Identifier(
		                getText(ctx.Identifier())));

		List<Statement> list = proc.getBody().getStatements();
		if ((list.isEmpty()) || !(list.get(list.size() - 1) instanceof ReturnStatement)) {
			list.add(new ReturnStatement(new Position(), null));
		}
		return proc;
	}

	@Override
	public ASTNode visitClassDeclaration(ClassDeclarationContext ctx) {
		List<ResolvableIdentifier> superClasses = new ArrayList<>();
		if (ctx.typeList() != null) {
			for (TypeContext type : ctx.typeList().type()) {
				superClasses.add(new ResolvableIdentifier(type.ClassIdentifier().getText()));
			}
		}
		ClassDeclaration cl =
		        new ClassDeclaration(position(ctx.getStart()), new Identifier(ctx.ClassIdentifier().getText()),
		                superClasses, new Block(position(ctx.getStart())));

        if (ctx.typeParamDeclaration() != null) {
            for (final TerminalNode param : ctx.typeParamDeclaration().ConstantIdentifier()) {
                final Identifier id = new Identifier(param.getText());
                cl.addTypeParameter(id);
            }
        }

		this.currentBlocks.push(cl.getBlock());
		for (MemberDeclarationContext member : ctx.memberDeclaration()) {
			this.currentVariableContext = VariableDeclaration.DeclarationType.ATTRIBUTE;
			this.currentProcedureContext = ProcedureDeclaration.DeclarationType.METHOD;
			ASTNode astNode = visit(member);
			if (astNode instanceof Declaration) {

				Declaration decl = (Declaration) astNode;
				decl.setAccessModifier(AccessModifier.stringToAccess(member.accessModifier().modifier.getText()));
				cl.getBlock().addDeclaration(decl);
			} else if (astNode instanceof Assignment) {

				Assignment asgnmnt =
				        new Assignment(astNode.getPosition(), new MemberAccess(astNode.getPosition(),
				                new SelfExpression(new Position()), ((Assignment) astNode).getLeft()),
				                ((Assignment) astNode).getRight());
				cl.getBlock().addStatement(asgnmnt);
			}
		}
		this.currentBlocks.pop();
		return cl;
	}

	private List<VariableDeclaration> parameterListToVarDeclList(ParameterListContext parameter) {
		if (parameter == null) {
			return new ArrayList<>();
		}
		ArrayList<VariableDeclaration> parameterList = new ArrayList<>();
		this.currentVariableContext = VariableDeclaration.DeclarationType.PARAMETER;
		for (VariableDeclarationContext var : parameter.variableDeclaration()) {
			parameterList.add((VariableDeclaration) visit(var));
		}
		return parameterList;
	}

	private List<DefaultParameterContext> defaultParameterListToVarDeclList(ParameterListContext parameter) {
		if (parameter == null) {
			return new ArrayList<>();
		}
		this.currentVariableContext = VariableDeclaration.DeclarationType.PARAMETER;
		return parameter.defaultParameter();
	}

	@Override
	public ASTNode visitWhileStatement(WhileStatementContext ctx) {
		ASTNode expr = visit(ctx.expression());
		if (!(expr instanceof Expression)) {

			return null;
		}

		WhileLoop loop =
		        new WhileLoop(position(ctx.getStart()), (Expression) expr, (Block) visit(ctx.statementBlock()));

		return loop;
	}

	@Override
	public ASTNode visitIfStatement(IfStatementContext ctx) {

		Block leastElseBlock = new Block(new Position());
		if (ctx.elseBlock != null) {
			leastElseBlock = (Block) visit(ctx.elseBlock);
		}
		Block firstElseBlock;

		if (ctx.elif().isEmpty()) {
			firstElseBlock = leastElseBlock;
		} else {

			Block lastElseBlock = new Block(position(ctx.getStart()));
			firstElseBlock = lastElseBlock;
			Block currentElseBlock;

			for (int i = 0; i < ctx.elif().size(); i++) {
				ElifContext currentCtx = ctx.elif(i);

				if (i == ctx.elif().size() - 1) {
					currentElseBlock = leastElseBlock;
				} else {
					currentElseBlock = new Block(position(currentCtx.getStart()));
				}

				lastElseBlock.addStatement(new ConditionalStatement(position(ctx.elif().get(i).getStart()),
				        (Expression) visit(currentCtx.elifCondition), (Block) visit(currentCtx.elifBlock),
				        currentElseBlock));
				lastElseBlock = currentElseBlock;

			}
		}
		return new ConditionalStatement(position(ctx.getStart()), (Expression) visit(ctx.ifCondition),
		        (Block) visit(ctx.thenBlock), firstElseBlock);

	}

	@Override
	public ASTNode visitTryStatement(TryStatementContext ctx) {
		ASTNode decl = visit(ctx.variableDeclaration().get(0));
		TryStatement tryStm =
		        new TryStatement(position(ctx.getStart()), (VariableDeclaration) decl, new Block(
		                position(ctx.getStart())), new Block(position(ctx.getStart())));
		addStatementsToBlock(tryStm.getTryBlock(), ctx.tryBlock.statement());
		addStatementsToBlock(tryStm.getHandleBlock(), ctx.handleBlock.statement());
		return tryStm;
	}

	public void addStatementsToBlock(Block block, List<StatementContext> statements) {
		for (StatementContext stm : statements) {
			this.currentVariableContext = VariableDeclaration.DeclarationType.VARIABLE;
			this.currentProcedureContext = ProcedureDeclaration.DeclarationType.UNBOUND;
			ASTNode node = visit(stm);
			if (node instanceof Statement) {
				block.addStatement((Statement) node);
			} else {
				block.addDeclaration((Declaration) node);
			}
		}
	}

	@Override
	public ASTNode visitIndependentDeclaration(IndependentDeclarationContext ctx) {
		ASTNode node;
		if (ctx.functionDeclaration() != null) {
			node = visit(ctx.functionDeclaration());
		} else if (ctx.procedureDeclaration() != null) {
			node = visit(ctx.procedureDeclaration());
		} else {
			node = visit(ctx.variableDeclaration());
			if (ctx.expression() != null) {
				this.currentBlocks.peek().addDeclaration((Declaration) node);
				return new Assignment(position(ctx.getStart()), new VariableAccess(position(ctx.getStart()),
				        ResolvableIdentifier.convert(((VariableDeclaration) node).getIdentifier())),
				        (Expression) visit(ctx.expression()));
			}
		}
		return node;
	}

	@Override
	public ASTNode visitStatementBlock(StatementBlockContext ctx) {

		Block block = new Block(position(ctx.getStart()));
		this.currentBlocks.push(block);
		addStatementsToBlock(block, ctx.statement());
		this.currentBlocks.pop();
		return block;
	}

	@Override
	public ASTNode visitReturnStm(ReturnStmContext ctx) {
		ASTNode expr = null;
		if (ctx.expression() != null) {

			expr = visit(ctx.expression());
		}

		return new ReturnStatement(position(ctx.getStart()), (Expression) expr);
	}

	@Override
	public ASTNode visitRaiseStm(RaiseStmContext ctx) {
		ASTNode expr = null;
		if (ctx.expression() != null) {

			expr = visit(ctx.expression());
		}
		return new RaiseStatement(position(ctx.getStart()), (Expression) expr);
	}

	@Override
	public ASTNode visitBreakStm(BreakStmContext ctx) {

		return new BreakStatement(position(ctx.getStart()));
	}

	@Override
	public ASTNode visitSkipStm(SkipStmContext ctx) {

		return new SkipStatement(position(ctx.getStart()));
	}

	@Override
	public ASTNode visitExpression(ExpressionContext ctx) {

		if (ctx.primary() != null) {
			return visit(ctx.primary());
		} else if (ctx.ifExpCondition != null && ctx.ifExprElse != null && ctx.ifExprThen != null) {

			return visitTernary(ctx);
		} else if (ctx.functionCall() != null) {

			return visit(ctx.functionCall());
		} else if (ctx.array != null) {
			List<Expression> arguments = Arrays.asList((Expression) visit(ctx.array), (Expression) visit(ctx.index));
			return new FunctionCall(position(ctx.getStart()), new ResolvableIdentifier("operator[]"), arguments);
		} else if (ctx.accessOperator() != null) {

			return visitMemberAccessExpr(ctx);
		} else if (ctx.plusMinusOperator() != null && ctx.singleExpression != null) {

			return unaryExpression(
			        position(ctx.getStart()),
			        ctx.plusMinusOperator().operator.getText(),
			        ctx.singleExpression);
		} else if (ctx.notOperator() != null) {

			return unaryExpression(position(ctx.getStart()), ctx.notOperator().operator.getText(), ctx.singleExpression);
		} else if (ctx.powerOperator() != null) {

			return binaryExpression(position(ctx.getStart()), ctx.powerOperator().getText(), ctx.left, ctx.right);
		} else if (ctx.dotOperator() != null) {

			return binaryExpression(position(ctx.getStart()), ctx.dotOperator().getText(), ctx.left, ctx.right);
		} else if (ctx.plusMinusOperator() != null) {

			return binaryExpression(position(ctx.getStart()), ctx.plusMinusOperator().getText(), ctx.left, ctx.right);
		} else if (ctx.compareOperator() != null) {

			return binaryExpression(position(ctx.getStart()), ctx.compareOperator().getText(), ctx.left, ctx.right);
		} else if (ctx.eqOperator() != null) {

			return binaryExpression(position(ctx.getStart()), ctx.eqOperator().getText(), ctx.left, ctx.right);
		} else if (ctx.inOperator() != null) {

			return binaryExpression(position(ctx.getStart()), ctx.inOperator().getText(), ctx.left, ctx.right);
		} else if (ctx.andOperator() != null) {

			return binaryExpression(position(ctx.getStart()), ctx.andOperator().getText(), ctx.left, ctx.right);
		} else if (ctx.orOperator() != null) {

			return binaryExpression(position(ctx.getStart()), ctx.orOperator().getText(), ctx.left, ctx.right);
		} else if (ctx.asOperator() != null) {
			return visitCastExpression(ctx);
		} else if (ctx.isOperator() != null) {
			return visitIsExpression(ctx);
		}
		return null;
	}

	@Override
	public ASTNode visitPrimary(PrimaryContext ctx) {
		if (ctx.singleExpression != null) {

			return visit(ctx.singleExpression);
		} else if (ctx.literal() != null) {

			return visit(ctx.literal());
		} else if (ctx.parent != null) {

			return visitParent(ctx);
		} else if (ctx.Identifier() != null) {

			return visitIdentifier(ctx);
		} else {

			return visitSelf(ctx);
		}
	}

	@Override
	public ASTNode visitLiteral(LiteralContext ctx) {

		if (ctx.IntegerLiteral() != null) {

			return new IntegerLiteral(position(ctx.getStart()),
			        Integer.parseInt(ctx.IntegerLiteral().getSymbol().getText()));
		} else if (ctx.RealLiteral() != null) {

			return new FloatLiteral(position(ctx.getStart()), Float.parseFloat(ctx.RealLiteral().getSymbol().getText()));
		} else if (ctx.CharacterLiteral() != null) {

			return new CharacterLiteral(position(ctx.getStart()),
			        ctx.CharacterLiteral().getSymbol().getText().charAt(1));
		} else if (ctx.StringLiteral() != null) {

			return new StringLiteral(position(ctx.getStart()), ctx.StringLiteral().getSymbol().getText());
		} else if (ctx.arrayLiteral() != null) {
			ArrayList<Expression> elements = new ArrayList<>();
			for (ExpressionContext eContext : ctx.arrayLiteral().expression()) {
				elements.add((Expression) visit(eContext));
			}
			return new ArrayLiteral(position(ctx.getStart()), elements);
		} else {

			return new BooleanLiteral(position(ctx.getStart()), Boolean.parseBoolean(ctx.BooleanLiteral().toString()));
		}
	}

	public ASTNode visitIdentifier(PrimaryContext ctx) {

		return new VariableAccess(position(ctx.getStart()), new ResolvableIdentifier(getText(ctx.Identifier())));
	}

	public ASTNode visitSelf(PrimaryContext ctx) {

		return new SelfExpression(position(ctx.getStart()));
	}

	public ParentExpression visitParent(PrimaryContext ctx) {
		return new ParentExpression(position(ctx.getStart()), new ResolvableIdentifier(getText(ctx.ClassIdentifier())));
	}

	public ASTNode visitTernary(ExpressionContext ctx) {
		ASTNode condition = visit(ctx.ifExpCondition);
		ASTNode thenExpr = visit(ctx.ifExprThen);
		ASTNode elseExpr = visit(ctx.ifExprElse);
		return new ConditionalExpression(position(ctx.getStart()), (Expression) condition, (Expression) thenExpr,
		        (Expression) elseExpr);
	}

	@Override
	public ASTNode visitMemberAccessStmt(@NotNull MemberAccessStmtContext ctx) {
		ASTNode left = visit(ctx.left);
		ASTNode right = visit(ctx.right);
		return new MemberAccess(position(ctx.getStart()), (Expression) left, (Expression) right);
	}

	public ASTNode visitMemberAccessExpr(ExpressionContext ctx) {
		ASTNode left = visit(ctx.left);
		ASTNode right = visit(ctx.right);
		return new MemberAccess(position(ctx.getStart()), (Expression) left, (Expression) right);
	}

	private MemberAccess unaryExpression(Position position, String operator, ExpressionContext expr) {
		String underscore = "";
		if (operator.equals("not")) {
			underscore = "_";
		}
		Expression self = (Expression) visit(expr);
		FunctionCall operatorCall =
		        new FunctionCall(position, new ResolvableIdentifier("operator" + underscore + operator),
		                new ArrayList<Expression>());
		return new MemberAccess(position, self, operatorCall);
	}

	private MemberAccess binaryExpression(Position position, String operator, ExpressionContext left,
	        ExpressionContext right) {
		String underscore = "";
		List<String> needsUnderscore = Arrays.asList("and", "or", "xor", "in");
		if (needsUnderscore.contains(operator)) {
			underscore = "_";
		}
		Expression self = (Expression) visit(left);
		FunctionCall operatorCall =
		        new FunctionCall(position, new ResolvableIdentifier("operator" + underscore + operator),
		                Arrays.asList((Expression) visit(right)));
		return new MemberAccess(position, self, operatorCall);
	}

	private CastExpression visitCastExpression(ExpressionContext ctx) {
		return new CastExpression(position(ctx.getStart()), (Expression) visit(ctx.expr), new ResolvableIdentifier(
		        getText(ctx.ClassIdentifier())));
	}

	private IsExpression visitIsExpression(ExpressionContext ctx) {
		return new IsExpression(position(ctx.getStart()), (Expression) visit(ctx.expr), new ResolvableIdentifier(
		        getText(ctx.ClassIdentifier())));
	}

	@Override
	protected ASTNode aggregateResult(ASTNode aggregate, ASTNode nextResult) {
		return nextResult == null ? aggregate : nextResult;
	}
}
