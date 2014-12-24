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
package de.uni.bremen.monty.moco.ast.expression;

import java.util.List;

import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.Position;
import de.uni.bremen.monty.moco.ast.ResolvableIdentifier;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.statement.Statement;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;

public class FunctionCall extends Expression implements Statement {
	private final ResolvableIdentifier identifier;
	private final List<Expression> arguments;
	private ProcedureDeclaration declaration;
    private List<List<Type>> signatureTypes;
    private boolean constructorCall;

	public FunctionCall(Position position, ResolvableIdentifier identifier, List<Expression> arguments) {
		super(position);
		this.identifier = identifier;
		this.arguments = arguments;
	}

    /**
     * Sets the signature types.
     *
     * @param signatureTypes The signature types.
     */
    public void setSignatureTypes(List<List<Type>> signatureTypes) {
        this.signatureTypes = signatureTypes;
    }

    /**
     * Gets all possible signature types. That is, the Cartesian product of the
     * types of actual parameters of this call. These types are set during first
     * pass of type resolving.
     *
     * @return The signature types.
     */
    public List<List<Type>> getSignatureTypes() {
        return this.signatureTypes;
    }

    public void setConstructorCall(boolean constructorCall) {
        this.constructorCall = constructorCall;
    }

    public boolean isConstructorCall() {
        return this.constructorCall;
    }

	/** get the identifier.
	 *
	 * @return the identifier */
	public ResolvableIdentifier getIdentifier() {
		return this.identifier;
	}

	/** get the List of paramter
	 *
	 * @return the paramters */
	public List<Expression> getArguments() {
		return this.arguments;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(BaseVisitor visitor) {
		visitor.visit(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visitChildren(BaseVisitor visitor) {
		for (Expression expression : this.arguments) {
			visitor.visitDoubleDispatched(expression);
		}
	}

	/** @return the declaration */
	public ProcedureDeclaration getDeclaration() {
		return this.declaration;
	}

	/** @param declaration
	 *            the declaration to set */
	public void setDeclaration(ProcedureDeclaration declaration) {
		this.declaration = declaration;
	}

	/** Get mangled identifier
	 *
	 * @return the mangled identifier */
	public Identifier getMangledIdentifier() {
		return this.declaration.getMangledIdentifier();
	}
}
