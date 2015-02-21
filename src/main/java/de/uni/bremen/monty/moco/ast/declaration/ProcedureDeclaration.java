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
package de.uni.bremen.monty.moco.ast.declaration;

import java.util.ArrayList;
import java.util.List;

import de.uni.bremen.monty.moco.ast.Block;
import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.Position;
import de.uni.bremen.monty.moco.ast.statement.ReturnStatement;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;

/** A ProcedureDeclaration represents the declaration of a procedure in the AST.
 * <p>
 * It can be used as a type. */
public class ProcedureDeclaration extends TypeDeclaration {
	public enum DeclarationType {
		INITIALIZER, METHOD, UNBOUND
	}

	/** The declarations and statements within this declaration. */
	private final Block body;

	/** The parameters of this declaration. */
	private final List<VariableDeclaration> parameter;

    /** The type parameters of this declaration */
    private final List<TypeParameterDeclaration> typeParameters;

    /** The return statements which occurred within the body of this declaration */
    private final List<ReturnStatement> returnStatements;

	private DeclarationType declarationType;

	/** Index of the procedure in the vmt if it is a procedure in the class struct */
	private int vmtIndex;

	/** Constructor.
	 *
	 * @param position
	 *            Position of this node
	 * @param identifier
	 *            the identifier
	 * @param body
	 *            the body of this procedure
	 * @param parameter
	 *            the parameter of this procedure */
	public ProcedureDeclaration(Position position, Identifier identifier, Block body,
	        List<VariableDeclaration> parameter, DeclarationType declarationType) {
		super(position, identifier);
        this.returnStatements = new ArrayList<>();
		this.body = body;
		this.parameter = parameter;
		this.declarationType = declarationType;
		this.vmtIndex = -1;
        this.typeParameters = new ArrayList<>();
	}

	public ProcedureDeclaration(Position position, Identifier identifier, Block body,
	        List<VariableDeclaration> parameter) {
		this(position, identifier, body, parameter, DeclarationType.UNBOUND);
	}

    /**
     * Adds a return statement.
     *
     * @param stmt The statement.
     */
    public void addReturnStatement(ReturnStatement stmt) {
        this.returnStatements.add(stmt);
    }

    public List<ReturnStatement> getReturnStatements() {
        return this.returnStatements;
    }

    public List<TypeParameterDeclaration> getTypeParameters() {
        return this.typeParameters;
    }

	/** Get the body block.
	 *
	 * @return the body */
	public Block getBody() {
		return this.body;
	}

	/** Get the list of parameter.
	 *
	 * @return the paramter */
	public List<VariableDeclaration> getParameter() {
		return this.parameter;
	}

	/** set the declaration type */
	public void setDeclarationType(DeclarationType type) {
		this.declarationType = type;
	}

	/** get the declaration type
	 *
	 * @return the declaration type */
	public DeclarationType getDeclarationType() {
		return this.declarationType;
	}

	public boolean isInitializer() {
		return this.declarationType == DeclarationType.INITIALIZER;
	}

	public boolean isMethod() {
		return this.declarationType == DeclarationType.METHOD;
	}

	public boolean isUnbound() {
		return this.declarationType == DeclarationType.UNBOUND;
	}

	public ClassDeclaration getDefiningClass() {
		if (isMethod() || isInitializer()) {
			return (ClassDeclaration) getParentNode().getParentNode();
		}
		return null;
	}

	/** Get the vmtIndex. */
	public int getVMTIndex() {
		return this.vmtIndex;
	}

	/** Set the vmtIndex. */
	public void setVMTIndex(int vmtIndex) {
		this.vmtIndex = vmtIndex;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(BaseVisitor visitor) {
		visitor.visit(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visitChildren(BaseVisitor visitor) {
		for (VariableDeclaration variableDeclaration : this.parameter) {
			visitor.visitDoubleDispatched(variableDeclaration);
		}
		visitor.visitDoubleDispatched(this.body);
	}
}
