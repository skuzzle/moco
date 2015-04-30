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
import java.util.Optional;

import de.uni.bremen.monty.moco.ast.Block;
import de.uni.bremen.monty.moco.ast.ClassScope;
import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.Position;
import de.uni.bremen.monty.moco.ast.ResolvableIdentifier;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;

/** A ClassDeclaration represents the declaration of a class in the AST.
 * <p>
 * A ClassDeclaration has a list of superclasses and a list of nested declarations. It can be used as a type. */
public class ClassDeclaration extends TypeDeclaration implements QuantifiedDeclaration {

	/** Identifier of superclasses. */
    private final List<TypeInstantiation> superClassIdentifiers = new ArrayList<>();

	/** Superclasses. */
    private final List<TypeDeclaration> superClassDeclarations = new ArrayList<>();

    /** Identifier of type parameters to this declaration */
    private List<TypeVariableDeclaration> typeParameters = new ArrayList<>();

	/** The generated default initializer to be called from every user defined initializer. */
	private ProcedureDeclaration defaultInitializer;

	/** Block with assignments */
	private final Block block;

	/** The virtal method table for this class */
	private final List<ProcedureDeclaration> virtualMethodTable = new ArrayList<>();

	/** The last index for the attributes of this class. This counter starts at `1` as index 0 is reserved for a pointer
	 * to the vmt. */
	private int lastAttributeIndex = 1;

	/** Constructor.
	 *
	 * @param position
	 *            Position of this node
	 * @param identifier
	 *            the identifier
	 * @param superClasses
	 *            a list of direct super-classes
	 * @param block
	 *            the block */
    public ClassDeclaration(Position position, Identifier identifier,
            List<TypeInstantiation> superClasses,
	        Block block) {
		super(position, identifier);
		this.block = block;
		this.superClassIdentifiers.addAll(superClasses);
	}

    public void setTypeParameters(List<TypeVariableDeclaration> typeParameters) {
        this.typeParameters = typeParameters;
    }

    @Override
    public List<TypeVariableDeclaration> getTypeParameters() {
        return this.typeParameters;
    }

    public void addSuperClassDeclaration(ClassDeclaration superClass) {
        this.superClassDeclarations.add(superClass);
    }

    /**
     * Whether this is a generic declaration. That is, there is at least one
     * type parameter.
     *
     * @return Whether this is a generic class.
     */
    public boolean isGeneric() {
        return this.typeParameters != null && !this.typeParameters.isEmpty();
    }

    @Override
    public ClassScope getScope() {
        return (ClassScope) super.getScope();
    }

	/** Get the list of declarations and assignments.
	 *
	 * @return the block with declarations and assignments */
	public Block getBlock() {
		return this.block;
	}

	/** Get the list of identifiers of direct superclasses
	 *
	 * @return the identifier of superclasses */
    public List<TypeInstantiation> getSuperClassIdentifiers() {
		return this.superClassIdentifiers;
	}

	/** Get the list of direct superclasses this class inherits from.
	 *
	 * @return the superclasses */
	public List<TypeDeclaration> getSuperClassDeclarations() {
		return this.superClassDeclarations;
	}

	/** Get a list of all the declarations of superclasses and this one. */
	public List<ClassDeclaration> getSuperClassDeclarationsRecursive() {
		List<ClassDeclaration> allSuperClassDeclarations = new ArrayList<>();
		for (TypeDeclaration superClass : this.superClassDeclarations) {
			if (superClass instanceof ClassDeclaration) {
				allSuperClassDeclarations.addAll(((ClassDeclaration) superClass).getSuperClassDeclarationsRecursive());
			}
		}
		allSuperClassDeclarations.add(this);
		return allSuperClassDeclarations;
	}

    public Optional<TypeInstantiation> getRecursiveParent(ResolvableIdentifier name) {
        for (final TypeInstantiation superClass : this.superClassIdentifiers) {
            if (superClass.getIdentifier().equals(name)) {
                return Optional.of(superClass);
            }
            assert superClass.getDeclaration() != null;
            assert superClass.getDeclaration() instanceof ClassDeclaration;

            final ClassDeclaration decl = (ClassDeclaration) superClass.getDeclaration();
            final Optional<TypeInstantiation> nested = decl.getRecursiveParent(name);
            if (nested.isPresent()) {
                return nested;
            }
        }
        return Optional.empty();
    }

	/** set the last attribute index.
	 *
	 * @param lastAttributeIndex
	 *            the last attribute index */
	public void setLastAttributeIndex(int lastAttributeIndex) {
		this.lastAttributeIndex = lastAttributeIndex;
	}

	/** get the last attribute index
	 *
	 * @return the last attribute index */
	public int getLastAttributeIndex() {
		return this.lastAttributeIndex;
	}

	/** Get the VMT.
	 *
	 * @return the VMT */
	public List<ProcedureDeclaration> getVirtualMethodTable() {
		return this.virtualMethodTable;
	}

	/** Get the default initializer.
	 *
	 * @return the default initializer */
	public ProcedureDeclaration getDefaultInitializer() {
		return this.defaultInitializer;
	}

	/** Set the default initializer.
	 *
	 * @param defaultInitializer
	 *            the new default initializer */
	public void setDefaultInitializer(ProcedureDeclaration defaultInitializer) {
		this.defaultInitializer = defaultInitializer;
	}

	/** {@inheritDoc} */
	@Override
	public void visit(BaseVisitor visitor) {
		visitor.visit(this);
	}

	/** {@inheritDoc} */
	@Override
	public void visitChildren(BaseVisitor visitor) {
        for (final TypeVariableDeclaration decl : this.typeParameters) {
            decl.visit(visitor);
        }
        for (final TypeInstantiation superClass : this.superClassIdentifiers) {
            superClass.visit(visitor);
        }
        this.block.visit(visitor);
	}

	/** {@inheritDoc} */
	@Override
	public boolean matchesType(TypeDeclaration other) {
		if (super.matchesType(other)) {
			return true;
		}
		if (other instanceof ClassDeclaration) {
			for (TypeDeclaration parentClass : this.superClassDeclarations) {
				if (parentClass.matchesType(other)) {
					return true;
				}
			}
		}
		return false;
	}
}
