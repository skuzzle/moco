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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.uni.bremen.monty.moco.ast.declaration.Declaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.TypeVariable;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Unification;
import de.uni.bremen.monty.moco.exception.RedeclarationException;
import de.uni.bremen.monty.moco.exception.UnknownIdentifierException;
import de.uni.bremen.monty.moco.exception.UnknownTypeException;

/** A scope in which an identifier is associated with a declaration.
 * <p>
 * To nest scopes or build a stack the parent scope is passed as an argument to the constructor. So you use it like
 * this:
 * <p>
 *
 * <pre>
 * {@code
 * // create a new scope and nest the old one
 * currentScope = new Scope(currentScope);
 * // do something
 * // destroy this scope and use the old (nested) one
 * currentScope = currentScope.getParentScope();
 * }
 * </pre> */
public class Scope {

    /** Scope's name */
    protected final String name;

	/** The parent scope in nesting hierarchy. */
    protected final Scope parent;

	/** The map to store the associations to procedure declarations. */
    protected final Map<Identifier, List<ProcedureDeclaration>> procedures;

	/** The map to store the remaining associations. */
    protected final Map<Identifier, Declaration> members;

    protected final Map<TypeVariable, Type> typeMap;

	        /**
     * Constructor.
     *
     * @param name The scope's name.
     * @param parent the parent scope in nesting hierarchy
     */
    public Scope(String name, Scope parent) {
        this.name = name;
		this.parent = parent;
		this.procedures = new HashMap<Identifier, List<ProcedureDeclaration>>();
		this.members = new HashMap<Identifier, Declaration>();
        this.typeMap = new HashMap<>();
	}

	/** Get the parent scope in nesting hierarchy.
	 * <p>
	 * This method acts as the 'pop()'-operation in the scope-stack analogy.
	 *
	 * @return the parent scope */
	public Scope getParentScope() {
		return this.parent;
	}

    /**
     * Returns a new Scope which has the current scope as parent.
     *
     * @param name The new scope's name
     * @return The new Scope
     */
    public Scope enter(String name) {
        return new Scope(name, this);
    }

    /**
     * Returns a new {@link ClassScope} which has the current scope as parent.
     *
     * @param name The new scope's name.
     *
     * @return The new ClassScope.
     */
    public ClassScope enterClass(String name) {
        return new ClassScope(name, this);
    }

    /**
     * Returns the parent scope.
     *
     * @return The parent scope.
     */
    public Scope leave() {
        return this.parent;
    }

    /**
     * Creates a copy of this scope. The result will contain the same
     * definitions as this store and will have the same parent. Modifications to
     * the copy of the scope will *not* be reflected to this scope. However,
     * modifications to the copy's parent will have influence on this scope as
     * the parent is not copied.
     *
     * @return A copy of this scope.
     */
    public Scope copy() {
        final Scope result = new Scope(this.name, this.parent);
        copyThis(result, false);
        return result;
    }

    /**
     * Creates a deep copy of this scope. In contrast to {@link #copy()}, this
     * method does create copies of all parent scopes.
     *
     * @return A deep copy.
     * @see #copy()
     */
    public Scope deepCopy() {
        final Scope copyParent = this.parent == null
                ? null
                : this.parent.deepCopy();
        final Scope result = new Scope(this.name, copyParent);
        copyThis(result, true);
        return result;
    }

    protected void copyThis(Scope to, boolean deep) {
        to.members.putAll(this.members);
        for (final Entry<Identifier, List<ProcedureDeclaration>> e : this.procedures.entrySet()) {
            final List<ProcedureDeclaration> listCopy = new ArrayList<>(e.getValue());
            to.procedures.put(e.getKey(), listCopy);
        }
    }

    public Declaration resolveFromType(Type type) {
        final ResolvableIdentifier name = new ResolvableIdentifier(
                type.getName().getSymbol());
        if (type instanceof ClassType) {
            return resolveType(type, name);
        } else if (type instanceof Function) {
            final List<ProcedureDeclaration> procedures = resolveProcedure(type, name);
            final Collection<ProcedureDeclaration> matches = new ArrayList<>();

            for (final ProcedureDeclaration proc : procedures) {
                final Function declared = (Function) proc.getType();
                if (Unification.of(type).with(declared).isSuccessful()) {
                    matches.add(proc);
                }
            }

            if (matches.isEmpty()) {
                throw new RuntimeException(type.toString());
            } else if (matches.size() > 1) {
                // TODO: best fit
                throw new RuntimeException();
            } else {
                return matches.iterator().next();
            }
        } else {
            // TODO: proper error handling
            throw new RuntimeException();
        }
    }

    private int compare(ClassType t1, ClassType t2) {
        return Math.abs(t1.distanceToObject() - t2.distanceToObject());
    }

	            /**
     * Resolve an identifier for a declaration.
     * <p>
     * First the declarations of this scope are searched. If the not successful
     * the search continues recursively in the parent scope. * @param
     * positionHint The node from which the procedure should be resolved. Will
     * be used as position in error message. * @param positionHint The node from
     * which the type should be resolved. Will be used as position in error
     * message.
     *
     * @param positionHint The node from which the type should be resolved. Will
     *            be used as position in error message.*
     * @param identifier the identifier to resolve
     * @return the declaration
     */
    public Declaration resolve(Location positionHint, ResolvableIdentifier identifier) {
		Declaration declaration = this.members.get(identifier);

		if (declaration != null) {
			return declaration;
		}
		if (this.parent != null) {
            return this.parent.resolve(positionHint, identifier);
		}
        throw new UnknownIdentifierException(positionHint, identifier);
	}

	        /**
     * Resolve an identifier for a type declaration.
     *
     * @param positionHint The node from which the type should be resolved. Will
     *            be used as position in error message.
     * @param identifier the identifier to resolve
     * @return the declaration
     */
    public TypeDeclaration resolveType(Location positionHint,
            ResolvableIdentifier identifier) {
		try {
            final Declaration declaration = resolve(positionHint, identifier);
			if (declaration instanceof TypeDeclaration) {
				return (TypeDeclaration) declaration;
			}
			throw new UnknownTypeException(identifier);
		} catch (UnknownIdentifierException e) {
			throw new UnknownTypeException(identifier);
		}
	}

	    /**
     * Resolve an identifier for list of overloaded procedures or functions.
     *
     * @param positionHint The node from which the procedure should be resolved.
     *            Will be used as position in error message.
     * @param identifier the identifier to resolve
     * @return the list of procedure declarations
     */
    public List<ProcedureDeclaration> resolveProcedure(Location positionHint,
            ResolvableIdentifier identifier) {
        final List<ProcedureDeclaration> result = new ArrayList<ProcedureDeclaration>();

        // important: add current scope's procedures first!
		if (this.procedures.containsKey(identifier)) {
			result.addAll(this.procedures.get(identifier));
		}
		if (this.parent != null) {
			try {
                result.addAll(this.parent.resolveProcedure(positionHint, identifier));
			} catch (UnknownIdentifierException e) {
			}
		}
		if (result.isEmpty()) {
            throw new UnknownIdentifierException(positionHint, identifier);
		}
		return result;
	}

	/** Associate an identifier with a declaration.
	 *
	 * This method uses define(Identifier, ProcedureDeclaration) if the given declaration is a procedure or function
	 * declaration.
	 *
	 * @param identifier
	 *            the identifier
	 * @param declaration
	 *            the declaration
	 * @throws RedeclarationException
	 *             if the identifier is already defined or this is invalid overloading */
	public void define(Identifier identifier, Declaration declaration) throws RedeclarationException {
		if (declaration instanceof ProcedureDeclaration) {
			define(identifier, (ProcedureDeclaration) declaration);
		} else if (this.members.get(identifier) != null) {
			throw new RedeclarationException(declaration, identifier.getSymbol());
		} else {
			this.members.put(identifier, declaration);
		}
	}

    public void define(TypeVariable var, Type type) {
        this.typeMap.put(var, type);
    }

    public Type resolve(TypeVariable var) {
        Type result = this.typeMap.get(var);
        if (result == null && this.parent != null) {
            result = this.parent.resolve(var);
        }
        if (result == null) {
            result = var;
        }
        return result;
    }

	/** Associate an identifier with a declaration.
	 * <p>
	 * This differs from define(Identifier, Declaration) as this method uses the declaration's Identifier-attribute to
	 * call define(Identifier, Declaration)
	 *
	 * @param declaration
	 *            the declaration
	 * @throws RedeclarationException
	 *             if the identifier is already defined or this is invalid overloading */
	public void define(Declaration declaration) throws RedeclarationException {
		define(declaration.getIdentifier(), declaration);
	}

	/** Associate an identifier with a procedure or function declaration.
	 *
	 * This takes overloading into account and throws a RedeclarationException if the declaration is an instance of
	 * invalid overloading.
	 *
	 * @param identifier
	 *            the identifier
	 * @param declaration
	 *            the declaration
	 * @throws RedeclarationException
	 *             if this is invalid overloading */
	public void define(Identifier identifier, ProcedureDeclaration declaration) throws RedeclarationException {
		if (!this.procedures.containsKey(identifier)) {
			this.procedures.put(identifier, new ArrayList<ProcedureDeclaration>());
		}
		this.procedures.get(identifier).add(declaration);
	}

    @Override
    public String toString() {
        return this.name;
    }
}
