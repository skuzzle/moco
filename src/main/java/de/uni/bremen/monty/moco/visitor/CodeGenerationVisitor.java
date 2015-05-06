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
package de.uni.bremen.monty.moco.visitor;

import static de.uni.bremen.monty.moco.codegeneration.types.LLVMTypeFactory.pointer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.Block;
import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.Package;
import de.uni.bremen.monty.moco.ast.declaration.ClassDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.Declaration;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.expression.CastExpression;
import de.uni.bremen.monty.moco.ast.expression.ConditionalExpression;
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
import de.uni.bremen.monty.moco.ast.statement.ReturnStatement;
import de.uni.bremen.monty.moco.ast.statement.SkipStatement;
import de.uni.bremen.monty.moco.ast.statement.Statement;
import de.uni.bremen.monty.moco.ast.statement.WhileLoop;
import de.uni.bremen.monty.moco.codegeneration.CodeGenerator;
import de.uni.bremen.monty.moco.codegeneration.CodeWriter;
import de.uni.bremen.monty.moco.codegeneration.context.CodeContext;
import de.uni.bremen.monty.moco.codegeneration.context.ContextUtils;
import de.uni.bremen.monty.moco.codegeneration.identifier.LLVMIdentifier;
import de.uni.bremen.monty.moco.codegeneration.identifier.LLVMIdentifierFactory;
import de.uni.bremen.monty.moco.codegeneration.types.LLVMPointer;
import de.uni.bremen.monty.moco.codegeneration.types.LLVMStructType;
import de.uni.bremen.monty.moco.codegeneration.types.LLVMType;
import de.uni.bremen.monty.moco.codegeneration.types.TypeConverter;
import de.uni.bremen.monty.moco.util.Params;

/** The CodeGenerationVisitor has the following tasks:
 *
 * <p>
 * <ul>
 * <li>Process the AST</li>
 * <li>Delegates as much work as possible to the CodeGenerator</li>
 * <li>Tell the CodeGenerator in which {@link CodeContext} to write</li>
 * <li>Evaluated expression should be given Statements as Arguments, see {@link #stack}</li>
 * </ul>
 * </p> */
public class CodeGenerationVisitor extends BaseVisitor {

	private final LLVMIdentifierFactory llvmIdentifierFactory = new LLVMIdentifierFactory();
	private final ContextUtils contextUtils = new ContextUtils();
	private final CodeGenerator codeGenerator;
	private final CodeWriter codeWriter;

	/** Each Expression pushes it's evaluated value onto the Stack. The value is represented by a LLVMIdentifier where
	 * the evaluated value is stored at runtime.
	 *
	 * Statements or complex Expressions can pop those values from the stack, which they use as parameters for further
	 * calculation.
	 *
	 * e.g. a := 3 is an Assignment having a VariableAccess and IntLiteral as children. VariableAccess and IntLiteral
	 * are expressions, thus pushing their values on the stack. An Assignment on the other hand is an Statement and
	 * return nothing, so doesn't push sth. on the stack, but instead it needs two Arguments. Those are popped from the
	 * Stack and yield the the evaluated VariableAccess and IntLiteral.
	 *
	 * Of course this only works, if the Assignment first process the children and afterwards popping from the stack. */
	private Stack<LLVMIdentifier<LLVMType>> stack = new Stack<>();

	/** Only Expressions push to a Stack. So this is a Stack of Stacks so every Statement has its own stack.
	 *
	 * e.g. the FunctionCall as a statement would leave behind a non-empty stack. */
	private final Stack<Stack<LLVMIdentifier<LLVMType>>> stackOfStacks = new Stack<>();

	public CodeGenerationVisitor(Params params) throws IOException {
		TypeConverter typeConverter = new TypeConverter(this.llvmIdentifierFactory, this.contextUtils.constant());
		this.codeWriter = new CodeWriter(params);
		this.codeGenerator = new CodeGenerator(typeConverter, this.llvmIdentifierFactory);
	}

	private void openNewFunctionScope() {
		this.contextUtils.addNewContext();
		this.llvmIdentifierFactory.openScope();
	}

	private void closeFunctionContext() {
		this.contextUtils.active().close();
		this.contextUtils.closeContext();
		this.llvmIdentifierFactory.closeScope();
	}

	private List<LLVMIdentifier<? extends LLVMType>> buildLLVMParameter(ProcedureDeclaration node) {
		List<LLVMIdentifier<? extends LLVMType>> llvmParameter = new ArrayList<>();

		if (node.isMethod() || node.isInitializer()) {
			LLVMType selfType = this.codeGenerator.mapToLLVMType(node.getDefiningClass());
			LLVMIdentifier<LLVMType> selfReference = this.llvmIdentifierFactory.newLocal("self", selfType, false);
			llvmParameter.add(selfReference);
		}

		for (VariableDeclaration param : node.getParameter()) {
			LLVMType llvmType = this.codeGenerator.mapToLLVMType(param.getTypeDeclaration());
			llvmType = llvmType instanceof LLVMStructType ? pointer(llvmType) : llvmType;
			boolean resolvable = llvmType instanceof LLVMStructType;
			LLVMIdentifier<LLVMType> e =
			        this.llvmIdentifierFactory.newLocal(param.getMangledIdentifier().getSymbol(), llvmType, resolvable);

			llvmParameter.add(e);
		}
		return llvmParameter;
	}

	private void addFunction(ProcedureDeclaration node, TypeDeclaration returnType) {
		List<LLVMIdentifier<? extends LLVMType>> llvmParameter = buildLLVMParameter(node);
		String name = node.getMangledIdentifier().getSymbol();
		this.codeGenerator.addFunction(this.contextUtils.active(), returnType, llvmParameter, name);
	}

	private void addNativeFunction(ProcedureDeclaration node, TypeDeclaration returnType) {
		List<LLVMIdentifier<? extends LLVMType>> llvmParameter = buildLLVMParameter(node);
		String name = node.getMangledIdentifier().getSymbol();
		this.codeGenerator.addNativeFunction(this.contextUtils.active(), returnType, llvmParameter, name);
	}

	private boolean isNative(ASTNode node) {
		while (node.getParentNode() != null) {
			node = node.getParentNode();
			if (node instanceof Package) {
				if (((Package) node).isNativePackage()) {
					return true;
				}
			}
		}
		return false;
	}

	protected void writeData() throws IOException {
		this.codeWriter.write(this.contextUtils.getData());
	}

	@Override
	protected void onEnterEachNode(ASTNode node) {
		this.contextUtils.setNode(node);
	}

	@Override
	protected void onExitChildrenEachNode(ASTNode node) {
		this.contextUtils.setNode(node);
	}

	@Override
	public void visit(Package node) {
		this.contextUtils.setNode(node);
		if (node.getParentNode() == null) {
			openNewFunctionScope();
			this.codeGenerator.addMain(this.contextUtils.active());

			super.visit(node);

			this.codeGenerator.returnMain(this.contextUtils.active());
			closeFunctionContext();

			try {
				writeData();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			super.visit(node);
		}
	}

	@Override
	public void visit(Block node) {
		for (Declaration declaration : node.getDeclarations()) {
			visitDoubleDispatched(declaration);
		}
		for (Statement statement : node.getStatements()) {
			this.stackOfStacks.push(this.stack);
			this.stack = new Stack<>();
			visitDoubleDispatched(statement);
			this.stack = this.stackOfStacks.pop();
		}
	}

	@Override
	public void visit(Assignment node) {
		super.visit(node);
		LLVMIdentifier<LLVMType> source = this.stack.pop();
		LLVMIdentifier<LLVMType> target = this.stack.pop();
		this.codeGenerator.assign(this.contextUtils.active(), target, source);
	}

	@Override
	public void visit(ClassDeclaration node) {
		// These are not boxed yet. So they cant inherit from object and cant have initializers.
		List<ClassDeclaration> treatSpecial =
		        Arrays.asList(CoreClasses.stringType(), CoreClasses.arrayType(), CoreClasses.voidType());
		if (!treatSpecial.contains(node)) {
			openNewFunctionScope();
			this.codeGenerator.buildConstructor(this.contextUtils.active(), node);
			closeFunctionContext();
		}
		super.visit(node);
	}

	@Override
	public void visit(VariableDeclaration node) {
		super.visit(node);
		if (!node.isAttribute()) {
			if (node.getIsGlobal()) {
				this.codeGenerator.declareGlobalVariable(
				        this.contextUtils.constant(),
				        node.getMangledIdentifier().getSymbol(),
				        node.getTypeDeclaration());
			} else {
				this.codeGenerator.declareLocalVariable(
				        this.contextUtils.active(),
				        node.getMangledIdentifier().getSymbol(),
				        node.getTypeDeclaration());
			}
		}
	}

	@Override
	public void visit(VariableAccess node) {
		super.visit(node);

		VariableDeclaration varDeclaration = (VariableDeclaration) node.getDeclaration();

		LLVMIdentifier<LLVMType> llvmIdentifier;
		if (varDeclaration.getIsGlobal()) {
			llvmIdentifier =
			        this.codeGenerator.resolveGlobalVarName(node.getMangledIdentifier().getSymbol(), node.getTypeDeclaration());
		} else if (varDeclaration.isAttribute()) {
			LLVMIdentifier<?> leftIdentifier = this.stack.pop();
			llvmIdentifier =
			        this.codeGenerator.accessMember(
			                this.contextUtils.active(),
			                (LLVMIdentifier<LLVMPointer<LLVMType>>) leftIdentifier,
			                varDeclaration.getAttributeIndex(),
			                node.getTypeDeclaration(),
			                !node.getLValue());
		} else {
			llvmIdentifier =
			        this.codeGenerator.resolveLocalVarName(
			                node.getMangledIdentifier().getSymbol(),
			                node.getTypeDeclaration(),
			                !varDeclaration.isParameter());
		}
		this.stack.push(llvmIdentifier);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(SelfExpression node) {
		this.stack.push(this.codeGenerator.resolveLocalVarName("self", node.getTypeDeclaration(), false));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(ParentExpression node) {
		LLVMIdentifier<?> self = this.codeGenerator.resolveLocalVarName("self", node.getSelfTypeDeclaration(), false);
		LLVMIdentifier<?> result =
		        this.codeGenerator.castClass(
		                this.contextUtils.active(),
		                (LLVMIdentifier<LLVMPointer<LLVMType>>) self,
		                node.getSelfTypeDeclaration(),
		                (ClassDeclaration) node.getTypeDeclaration(),
		                this.codeGenerator.createLabelPrefix("cast", node));
		this.stack.push((LLVMIdentifier<LLVMType>) result);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(CastExpression node) {
		super.visit(node);
		LLVMIdentifier<?> object = this.stack.pop();
		LLVMIdentifier<?> result =
		        this.codeGenerator.castClass(
		                this.contextUtils.active(),
		                (LLVMIdentifier<LLVMPointer<LLVMType>>) object,
		                (ClassDeclaration) node.getExpression().getTypeDeclaration(),
		                (ClassDeclaration) node.getTypeDeclaration(),
		                this.codeGenerator.createLabelPrefix("cast", node));
		this.stack.push((LLVMIdentifier<LLVMType>) result);
	}

	/** {@inheritDoc} */
	@Override
	public void visit(IsExpression node) {
		super.visit(node);
		LLVMIdentifier<?> object = this.stack.pop();
		LLVMIdentifier<?> result =
		        this.codeGenerator.isClass(
		                this.contextUtils.active(),
		                (LLVMIdentifier<LLVMPointer<LLVMType>>) object,
		                (ClassDeclaration) node.getExpression().getTypeDeclaration(),
		                (ClassDeclaration) node.getToType());
		LLVMIdentifier<LLVMType> boxedResult =
		        this.codeGenerator.boxType(this.contextUtils.active(), (LLVMIdentifier<LLVMType>) result, CoreClasses.boolType());
		this.stack.push(boxedResult);
	}

	@Override
	public void visit(MemberAccess node) {
		super.visit(node);
		// If right is VariableAccess, everything is done in visit(VariableAccess)
		// If right is FunctionCall, everything is done in visit(FunctionCall)
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(StringLiteral node) {
		super.visit(node);

		LLVMIdentifier<? extends LLVMType> addr =
		        this.codeGenerator.addConstantString(this.contextUtils.constant(), node.getValue());
		this.stack.push((LLVMIdentifier<LLVMType>) addr);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(CharacterLiteral node) {
		super.visit(node);
		LLVMIdentifier<? extends LLVMType> addr = this.codeGenerator.loadChar(node.getValue());
		// Boxing
		CodeContext c = this.contextUtils.active();
		LLVMIdentifier<LLVMType> box = this.codeGenerator.boxType(c, (LLVMIdentifier<LLVMType>) addr, node.getTypeDeclaration());
		this.stack.push(box);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(IntegerLiteral node) {
		super.visit(node);

		LLVMIdentifier<? extends LLVMType> addr = this.codeGenerator.loadInt(node.getValue());
		// Boxing
		CodeContext c = this.contextUtils.active();
		LLVMIdentifier<LLVMType> box = this.codeGenerator.boxType(c, (LLVMIdentifier<LLVMType>) addr, node.getTypeDeclaration());
		this.stack.push(box);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(BooleanLiteral node) {
		super.visit(node);

		LLVMIdentifier<? extends LLVMType> addr = this.codeGenerator.loadBool(node.getValue());
		// Boxing
		CodeContext c = this.contextUtils.active();
		LLVMIdentifier<LLVMType> box = this.codeGenerator.boxType(c, (LLVMIdentifier<LLVMType>) addr, node.getTypeDeclaration());
		this.stack.push(box);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(FloatLiteral node) {
		super.visit(node);

		LLVMIdentifier<? extends LLVMType> addr = this.codeGenerator.loadFloat(node.getValue());
		// Boxing
		CodeContext c = this.contextUtils.active();
		LLVMIdentifier<LLVMType> box = this.codeGenerator.boxType(c, (LLVMIdentifier<LLVMType>) addr, node.getTypeDeclaration());
		this.stack.push(box);
	}

	@Override
	public void visit(ArrayLiteral node) {
		super.visit(node);

		ClassDeclaration type = (ClassDeclaration) node.getTypeDeclaration();
		LLVMIdentifier<LLVMPointer<LLVMStructType>> array =
		        this.codeGenerator.addArray(this.contextUtils.active(), node.getEntries().size(), type);
		for (int i = node.getEntries().size() - 1; i >= 0; i--) {
			this.codeGenerator.setArrayElement(this.contextUtils.active(), array, i, this.stack.pop());
		}

		this.stack.push((LLVMIdentifier) array);
	}

	@Override
	public void visit(ConditionalExpression node) {

		String ifPre = this.codeGenerator.createLabelPrefix("ifexpr", node);
		String ifTrue = ifPre + ".true";
		String ifFalse = ifPre + ".false";
		String ifEnd = ifPre + ".end";

		visitDoubleDispatched(node.getCondition());

		LLVMIdentifier<LLVMType> condition = this.stack.pop();
		this.codeGenerator.branch(this.contextUtils.active(), condition, ifTrue, ifFalse);

		this.contextUtils.active().label(ifTrue);
		visitDoubleDispatched(node.getThenExpression());
		LLVMIdentifier<LLVMType> thenExpr = this.stack.pop();
		this.contextUtils.active().branch(ifEnd);

		this.contextUtils.active().label(ifFalse);
		visitDoubleDispatched(node.getElseExpression());
		LLVMIdentifier<LLVMType> elseExpr = this.stack.pop();
		this.contextUtils.active().branch(ifEnd);

		this.contextUtils.active().label(ifEnd);
		List<LLVMIdentifier<LLVMType>> identifiers = new ArrayList<>();
		identifiers.add(thenExpr);
		identifiers.add(elseExpr);
		List<String> labels = new ArrayList<>();
		labels.add(ifTrue);
		labels.add(ifFalse);
		this.stack.push(this.contextUtils.active().phi(
		        thenExpr.getType(),
		        thenExpr.needToBeResolved(),
		        identifiers,
		        this.llvmIdentifierFactory.newLocal(thenExpr.getType(), thenExpr.needToBeResolved()),
		        labels));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(FunctionCall node) {
		super.visit(node);

		List<TypeDeclaration> expectedParameters = new ArrayList<>();
		for (VariableDeclaration varDeclaration : node.getDeclaration().getParameter()) {
			expectedParameters.add(varDeclaration.getTypeDeclaration());
		}
		List<LLVMIdentifier<?>> arguments = new ArrayList<>(node.getArguments().size());
		for (int i = 0; i < node.getArguments().size(); i++) {
			arguments.add(this.stack.pop());
		}
		Collections.reverse(arguments);

		ProcedureDeclaration declaration = node.getDeclaration();
		ClassDeclaration definingClass = declaration.getDefiningClass();

		List<ClassDeclaration> treatSpecial =
		        Arrays.asList(
		                CoreClasses.intType(),
		                CoreClasses.boolType(),
		                CoreClasses.floatType(),
		                CoreClasses.charType());
		if (declaration.isInitializer() && treatSpecial.contains(definingClass)) {
			// Instead of calling the initializer of this boxed type with a boxed value as arguments just push the
			// argument on the stack and return.
			this.stack.push((LLVMIdentifier<LLVMType>) arguments.get(0));
			return;
		}

		if (declaration.isMethod() || declaration.isInitializer()) {
			expectedParameters.add(0, definingClass);
			if (declaration.isMethod()
			        || (declaration.isInitializer() && (node.getParentNode() instanceof MemberAccess))) {
				arguments.add(0, this.stack.pop());
			} else if (declaration.isInitializer()) {
				LLVMIdentifier<LLVMType> selfReference =
				        this.codeGenerator.callConstructor(this.contextUtils.active(), definingClass);
				this.codeGenerator.callVoid(
				        this.contextUtils.active(),
				        definingClass.getDefaultInitializer().getMangledIdentifier().getSymbol(),
				        Arrays.<LLVMIdentifier<?>> asList(selfReference),
				        Arrays.<TypeDeclaration> asList(definingClass));
				arguments.add(0, selfReference);
			}
		}

		if (declaration.isMethod() && !declaration.isInitializer()) {
			if (declaration instanceof FunctionDeclaration) {
				this.stack.push((LLVMIdentifier<LLVMType>) this.codeGenerator.callMethod(
				        this.contextUtils.active(),
				        (FunctionDeclaration) declaration,
				        arguments,
				        expectedParameters));
			} else {
				this.codeGenerator.callVoidMethod(this.contextUtils.active(), declaration, arguments, expectedParameters);
			}
		} else {
			if (declaration instanceof FunctionDeclaration) {
				this.stack.push((LLVMIdentifier<LLVMType>) this.codeGenerator.call(
				        this.contextUtils.active(),
				        declaration.getMangledIdentifier().getSymbol(),
				        node.getTypeDeclaration(),
				        arguments,
				        expectedParameters));
			} else {
				if (declaration.isInitializer()) {
					this.stack.push((LLVMIdentifier<LLVMType>) arguments.get(0));
				}
				this.codeGenerator.callVoid(
				        this.contextUtils.active(),
				        declaration.getMangledIdentifier().getSymbol(),
				        arguments,
				        expectedParameters);
			}
		}
	}

	@Override
	public void visit(FunctionDeclaration node) {
		openNewFunctionScope();
		if (isNative(node)) {
			addNativeFunction(node, node.getTypeDeclaration());
		} else {
			addFunction(node, node.getTypeDeclaration());
			visitDoubleDispatched(node.getBody());
		}
		closeFunctionContext();
	}

	@Override
	public void visit(ProcedureDeclaration node) {
		openNewFunctionScope();

		if (isNative(node) && !node.isInitializer()) {
			addNativeFunction(node, CoreClasses.voidType());
		} else {
			addFunction(node, CoreClasses.voidType());

			visitDoubleDispatched(node.getBody());
			if (node.isInitializer()) {
				this.codeGenerator.returnValue(
				        this.contextUtils.active(),
				        (LLVMIdentifier<LLVMType>) (LLVMIdentifier<?>) this.llvmIdentifierFactory.voidId(),
				        CoreClasses.voidType());
			}
		}
		closeFunctionContext();
	}

	@Override
	public void visit(ReturnStatement node) {
		super.visit(node);
		if (node.getParameter() != null) {
			ASTNode parent = node;
			while (!(parent instanceof FunctionDeclaration)) {
				parent = parent.getParentNode();
			}
			LLVMIdentifier<LLVMType> returnValue = this.stack.pop();
			this.codeGenerator.returnValue(
			        this.contextUtils.active(),
			        returnValue,
			        ((FunctionDeclaration) parent).getTypeDeclaration());
		} else {
			this.codeGenerator.returnValue(
			        this.contextUtils.active(),
			        (LLVMIdentifier<LLVMType>) (LLVMIdentifier<?>) this.llvmIdentifierFactory.voidId(),
			        CoreClasses.voidType());
		}
	}

	@Override
	public void visit(ConditionalStatement node) {
		visitDoubleDispatched(node.getCondition());

		String ifPre = this.codeGenerator.createLabelPrefix("if", node);

		String ifTrue = ifPre + ".true";
		String ifFalse = ifPre + ".false";
		String ifEnd = ifPre + ".end";

		LLVMIdentifier<LLVMType> condition = this.stack.pop();
		this.codeGenerator.branch(this.contextUtils.active(), condition, ifTrue, ifFalse);

		this.contextUtils.active().label(ifTrue);
		visitDoubleDispatched(node.getThenBlock());
		this.contextUtils.active().branch(ifEnd);

		this.contextUtils.active().label(ifFalse);
		visitDoubleDispatched(node.getElseBlock());
		this.contextUtils.active().branch(ifEnd);

		this.contextUtils.active().label(ifEnd);
	}

	@Override
	public void visit(WhileLoop node) {

		String whlPre = this.codeGenerator.createLabelPrefix("while", node);
		String whileCond = whlPre + ".condition";
		String whileBlk = whlPre + ".block";
		String whileEnd = whlPre + ".end";

		this.contextUtils.active().branch(whileCond);
		this.contextUtils.active().label(whileCond);
		visitDoubleDispatched(node.getCondition());

		LLVMIdentifier<LLVMType> condition = this.stack.pop();
		this.codeGenerator.branch(this.contextUtils.active(), condition, whileBlk, whileEnd);

		this.contextUtils.active().label(whileBlk);
		visitDoubleDispatched(node.getBody());
		this.contextUtils.active().branch(whileCond);
		this.contextUtils.active().label(whileEnd);
	}

	@Override
	public void visit(SkipStatement node) {
		super.visit(node);
		String whlPre = this.codeGenerator.getLabelPrefix(node.getLoop());
		this.contextUtils.active().branch(whlPre + ".condition");
	}

	@Override
	public void visit(BreakStatement node) {
		super.visit(node);
		String whlPre = this.codeGenerator.getLabelPrefix(node.getLoop());
		this.contextUtils.active().branch(whlPre + ".end");
	}
}
