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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.declaration.ClassDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ModuleDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeVariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.expression.VariableAccess;
import de.uni.bremen.monty.moco.ast.statement.ConditionalStatement;
import de.uni.bremen.monty.moco.ast.statement.WhileLoop;

enum Mangled {
	MODULE, CLASS, FUNC, PROC, BLOCK, VAR, TYPE, IF, ELSE, WHILE
}

/** The NameManglingVisitor uses the following pattern to mangle the names:
 *
 * _ : '.';
 *
 * $ : '$';
 *
 *
 * module : 'module'_name;
 *
 * class : 'class'_name;
 *
 * func : 'func'_name$type($type)*;
 *
 * proc : 'proc'_name($type)*;
 *
 * var : 'var'_name$type;
 *
 * type : 'type'_module(_class)?((_block|_proc|_func)*(_proc|_func))?;
 *
 * block : 'block'_(IF|TRY|WHILE|HANDLE|ELSE)_number;
 *
 * mangled : packet_module(_class)?((_block|_proc|_func)*(_proc|_func|_var))?; * */
public class NameManglingVisitor extends BaseVisitor {

	/** Various stacks for interlaced and nested prefixes. */
	private final Stack<AtomicInteger> numbers;
	private final Stack<String> moduleNames;
	private final Stack<String> classNames;
	private final Stack<ArrayList<String>> parentScopes;

	/** Used for mapping prefixes in the pattern. */
	private EnumMap<Mangled, String> nameManglingPrefixes;

	/** Constructor. */
	public NameManglingVisitor() {
		this.numbers = new Stack<>();
		this.moduleNames = new Stack<>();
		this.classNames = new Stack<>();
		this.parentScopes = new Stack<>();

		initNameManglingPrefixes();
		manglePredefinedTypes();
	}

	/** Initialize the mapping of prefixes. */
	private void initNameManglingPrefixes() {
		this.nameManglingPrefixes = new EnumMap<>(Mangled.class);

		this.nameManglingPrefixes.put(Mangled.MODULE, "M.");
		this.nameManglingPrefixes.put(Mangled.CLASS, ".C.");
		this.nameManglingPrefixes.put(Mangled.FUNC, ".F.");
		this.nameManglingPrefixes.put(Mangled.PROC, ".P.");
		this.nameManglingPrefixes.put(Mangled.BLOCK, ".B.");
		this.nameManglingPrefixes.put(Mangled.VAR, ".V.");
		this.nameManglingPrefixes.put(Mangled.TYPE, "$");
		this.nameManglingPrefixes.put(Mangled.IF, "IF.");
		this.nameManglingPrefixes.put(Mangled.ELSE, "ELSE.");
		this.nameManglingPrefixes.put(Mangled.WHILE, "WHILE.");
	}

	/** This function mangles the base types. Current module for base types is "std". */
	private void manglePredefinedTypes() {
		final String prefix =
		        this.nameManglingPrefixes.get(Mangled.MODULE) + "std" + this.nameManglingPrefixes.get(Mangled.CLASS);

		CoreClasses.stringType().setMangledIdentifier(new Identifier(prefix + "String"));
		CoreClasses.arrayType().setMangledIdentifier(new Identifier(prefix + "Array"));
	}

	@Override
    public void visit(TypeVariableDeclaration node) {
	    if (node.getMangledIdentifier() == null) {
	        node.setMangledIdentifier(node.getTypeDeclaration().getMangledIdentifier());
	    }
	}

	@Override
	public void visit(ModuleDeclaration node) {
		if (node.getMangledIdentifier() == null) {
			this.numbers.push(new AtomicInteger(-1));
			this.parentScopes.push(new ArrayList<String>());

			String moduleName = escapeForLLVM(node.getIdentifier());
			moduleName = this.nameManglingPrefixes.get(Mangled.MODULE) + moduleName;

			this.moduleNames.push(moduleName);
			node.setMangledIdentifier(new Identifier(moduleName));
			super.visit(node);

			this.numbers.pop();
			this.moduleNames.pop();
			this.parentScopes.pop();
		}
	}

	@Override
	public void visit(ClassDeclaration node) {
		if (node.getMangledIdentifier() == null) {
			final String className = this.nameManglingPrefixes.get(Mangled.CLASS) + escapeForLLVM(node.getIdentifier());

			this.classNames.push(className);
			node.setMangledIdentifier(new Identifier(this.moduleNames.peek() + className));
			super.visit(node);
			this.classNames.pop();
		}
	}

	@Override
	public void visit(FunctionDeclaration node) {
	    if (CoreClasses.voidType().equals(node.getTypeDeclaration())) {
	        visit((ProcedureDeclaration) node);
	    } else if (node.getMangledIdentifier() == null) {
			String funcName = this.nameManglingPrefixes.get(Mangled.FUNC) + escapeForLLVM(node.getIdentifier());
			funcName += this.nameManglingPrefixes.get(Mangled.TYPE) + mangleTypeDeclaration(node.getTypeDeclaration());

			mangleProcedureDeclaration(node, funcName);
		}
	}

	@Override
	public void visit(ProcedureDeclaration node) {
		if (node.getMangledIdentifier() == null) {
			final String procName = this.nameManglingPrefixes.get(Mangled.PROC) + escapeForLLVM(node.getIdentifier());

			mangleProcedureDeclaration(node, procName);
		}
	}

	@Override
	public void visit(VariableDeclaration node) {
		if (node.getMangledIdentifier() == null) {
			String varName =
			        this.nameManglingPrefixes.get(Mangled.VAR) + escapeForLLVM(node.getIdentifier())
			                + this.nameManglingPrefixes.get(Mangled.TYPE) + mangleTypeDeclaration(node.getTypeDeclaration());

			final String wholeName = buildNameHelper() + varName;
			node.setMangledIdentifier(new Identifier(wholeName));
		}
	}

	@Override
	public void visit(VariableAccess node) {
		if (node.getMangledIdentifier() == null) {
			visitDoubleDispatched(node.getDeclaration());
		}
	}

	@Override
	public void visit(ConditionalStatement node) {
		AtomicInteger number = this.numbers.peek();
		number.incrementAndGet();
		String blockName = this.nameManglingPrefixes.get(Mangled.BLOCK) + this.nameManglingPrefixes.get(Mangled.IF) + number;
		visitDoubleDispatched(node.getCondition());
		this.parentScopes.peek().add(blockName);

		visitDoubleDispatched(node.getThenBlock());
		this.parentScopes.peek().remove(blockName);
		number.incrementAndGet();
		blockName = this.nameManglingPrefixes.get(Mangled.BLOCK) + this.nameManglingPrefixes.get(Mangled.ELSE) + number;

		visitDoubleDispatched(node.getElseBlock());
		this.parentScopes.peek().remove(blockName);
	}

	@Override
	public void visit(WhileLoop node) {
		final AtomicInteger number = this.numbers.peek();
		number.incrementAndGet();
		final String whileName =
		        this.nameManglingPrefixes.get(Mangled.BLOCK) + this.nameManglingPrefixes.get(Mangled.WHILE) + number;
		this.parentScopes.peek().add(whileName);
		super.visit(node);
		this.parentScopes.peek().remove(whileName);
	}

	/** If a TypeDeclaration is not mangled yet, it has to be in some other ModuleDeclaration. The other
	 * ModuleDeclaration must be mangled first.
	 *
	 * @param node
	 *            TypeDeclaration to mangle
	 * @return mangled Identifier */
	private String mangleTypeDeclaration(TypeDeclaration node) {
		ASTNode tmp = node;
		if (node.getMangledIdentifier() == null) {
			while (!(tmp instanceof ModuleDeclaration)) {
				tmp = tmp.getParentNode();
			}
			visitDoubleDispatched(tmp);
			visitDoubleDispatched(node);
		}
		return node.getMangledIdentifier().getSymbol();
	}

	/** This function mangles the parameters of FunctionDeclaration and ProcedureDeclaration.
	 *
	 * @param node
	 *            FunctionDeclaration or ProcedureDeclaration to mangle
	 * @param procName
	 *            some prefix to build the full mangled name */
	private void mangleProcedureDeclaration(ProcedureDeclaration node, String procName) {
		for (final VariableDeclaration variableDeclaration : node.getParameter()) {
			if (variableDeclaration.getIdentifier().getSymbol().equals("self")) {
				variableDeclaration.setMangledIdentifier(new Identifier("self"));
			} else {
				procName +=
				        this.nameManglingPrefixes.get(Mangled.TYPE) + mangleTypeDeclaration(variableDeclaration.getTypeDeclaration());
				visitDoubleDispatched(variableDeclaration);
			}
		}

		this.parentScopes.peek().add(procName);
		final String wholeName = buildNameHelper();

		node.setMangledIdentifier(new Identifier(wholeName));
		visitDoubleDispatched(node.getBody());
		this.parentScopes.peek().remove(procName);
	}

	/** Builds the name with module, class and parentScopes.
	 *
	 * @return prefix of mangled name */
	private String buildNameHelper() {
		final StringBuilder wholeName = new StringBuilder();
		wholeName.append(this.moduleNames.peek());

		if (!this.classNames.isEmpty()) {
			wholeName.append(this.classNames.peek());
		}

		for (final String prevScope : this.parentScopes.peek()) {
			wholeName.append(prevScope);
		}

		return wholeName.toString();
	}

	private String escapeForLLVM(Identifier identifier) {
		String string = identifier.getSymbol();
		string = string.replaceAll("\\[\\]", "_array_access");
		string = string.replaceAll("%", "_rem");
		string = string.replaceAll("\\*", "_mult");
		string = string.replaceAll("/", "_div");
		string = string.replaceAll("\\+", "_plus");
		string = string.replaceAll("-", "_minus");
		string = string.replaceAll("<=", "_lesser_equal");
		string = string.replaceAll(">=", "_greater_equal");
		string = string.replaceAll("!=", "_not_equal");
		string = string.replaceAll("=", "_equal");
		string = string.replaceAll("<", "_lesser");
		string = string.replaceAll(">", "_greater");
		string = string.replaceAll("%", "_rem");
		return string;
	}
}
