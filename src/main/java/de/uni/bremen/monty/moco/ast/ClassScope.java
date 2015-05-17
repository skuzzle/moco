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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import de.uni.bremen.monty.moco.ast.declaration.Declaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Unification;
import de.uni.bremen.monty.moco.exception.UnknownIdentifierException;
import de.uni.bremen.monty.moco.visitor.typeinf.TypeResolver;

/**
 * A scope in which identifier are associated with declarations.
 * <p>
 * To nest scopes or build a stack the parent scope is passed as an argument to
 * the construtor. So you use it like this
 * <p>
 *
 * <pre>
 * {@code
 * // create a new scope and nest the old one
 * currentScope = new ClassScope(currentScope);
 * // do something
 * // destroy this scope and use the old (nested) one
 * currentScope = currentScope.getParentScope();
 * }
 * </pre>
 * <p>
 * This special scope searches its associations, the parent classes in
 * inheritance hierachy and only then the parent scope.
 * <p>
 * Note: only single inheritance so far.
 */
public class ClassScope extends Scope {

    /** The parent class in inheritance hierachy. */
    private final List<ClassScope> parentClassesScopes;

    /**
     * Constructor.
     *
     * @param name The scope's name.
     * @param parent the parent scope in nesting hierachy
     */
    public ClassScope(String name, Scope parent) {
        super(name, parent);
        this.parentClassesScopes = new ArrayList<>();
        this.substitutions = Unification.EMPTY;
    }

    @Override
    public ClassScope copy() {
        final ClassScope result = new ClassScope(this.name, this.parent);
        copyThis(result, false);
        return result;
    }

    @Override
    public ClassScope deepCopy() {
        final Scope copyParent = this.parent == null
                ? null
                : this.parent.deepCopy();
        final ClassScope result = new ClassScope(this.name, copyParent);
        copyThis(result, true);
        return result;
    }

    @Override
    protected void copyThis(Scope to, boolean deep) {
        final ClassScope cs = (ClassScope) to;
        super.copyThis(cs, deep);
        if (deep) {
            for (final ClassScope parentClassScope : this.parentClassesScopes) {
                cs.parentClassesScopes.add(parentClassScope.deepCopy());
            }
        } else {
            cs.parentClassesScopes.addAll(this.parentClassesScopes);
        }
    }

    public void addParentClassScope(ClassScope scope, Unification substitutions) {
        if (scope == null) {
            throw new IllegalArgumentException("scope is null");
        } else if (substitutions == null) {
            throw new IllegalArgumentException("substitutions is null");
        } else if (scope == this) {
            throw new IllegalArgumentException("scope can not be its own parent");
        }

        this.parentClassesScopes.add(scope);
        defineSubstitutions(substitutions);
    }

    /**
     * Resolve an identifier in inherited scopes.
     *
     * @param identifier the identifier
     * @return the declaration or null if nothing is found
     */
    protected Declaration resolveMember(ResolvableIdentifier identifier) {
        Declaration declaration = this.members.get(identifier);

        if (declaration != null) {
            return declaration;
        }
        for (ClassScope scope : this.parentClassesScopes) {
            declaration = scope.resolveMember(identifier);
            if (declaration != null) {
                return declaration;
            }
        }
        return null;
    }

    /**
     * Resolve an identifier for list of overloaded procedures or functions in
     * inherited scope.
     *
     * @param positionHint The location of where the procedure is called.
     * @param identifier the identifier to resolve
     * @return the list of procedure declarations
     */
    protected List<ProcedureDeclaration> resolveProcedureMember(Location positionHint,
            ResolvableIdentifier identifier) {
        List<ProcedureDeclaration> result = new ArrayList<ProcedureDeclaration>();

        if (this.procedures.containsKey(identifier)) {
            result.addAll(this.procedures.get(identifier));
        }
        for (ClassScope scope : this.parentClassesScopes) {
            final List<ProcedureDeclaration> parentProcs =
                    scope.resolveProcedureMember(positionHint, identifier);

            // Only add parent declarations which are not overridden.
            outer: for (Iterator<ProcedureDeclaration> it = parentProcs.iterator(); it.hasNext();) {
                final ProcedureDeclaration parentDecl = it.next();
                for (final ProcedureDeclaration decl : result) {
                    if (decl.overrides(parentDecl)) {
                        it.remove();
                        continue outer;
                    }
                }
            }
            result.addAll(parentProcs);
        }
        return result;
    }


    @Override
    public Optional<ProcedureDeclaration> getOverridden(TypeResolver resolver,
            ProcedureDeclaration decl) {
        final ResolvableIdentifier name = ResolvableIdentifier.of(decl.getIdentifier());
        final List<ProcedureDeclaration> candidates = this.procedures.get(name);

        if (candidates != null) {
            final Optional<ProcedureDeclaration> opt = candidates.stream()
                    .filter(d -> d != decl)
                    .peek(resolver::resolveTypeOf)
                    .filter(d -> isPossibleOverride(d, decl))
                    .findFirst();

            if (opt.isPresent()) {
                return opt;
            }
        }

        for (final ClassScope scope : this.parentClassesScopes) {
            final Optional<ProcedureDeclaration> opt = scope.getOverridden(resolver, decl);
            if (opt.isPresent()) {
                return opt;
            }
        }
        return Optional.empty();
    }

    private boolean isPossibleOverride(ProcedureDeclaration overridden,
            ProcedureDeclaration override) {

        final Function overriddenType = overridden.getType().asFunction();
        final Function overrideType = override.getType().asFunction();
        return overridden.getIdentifier().equals(override.getIdentifier()) &&
            Unification.given(override.getScope())
                    .testIf(overrideType.getParameters())
                    .isA(overriddenType.getParameters())
                    .isSuccessful();
    }

    /**
     * Resolve an identifier for a declaration
     * <p>
     * It first searches its associations, the parent classes in inheritance
     * hierarchy and only then the parent scope.
     *
     * @param location The location from which the declaration is resolved.
     * @param identifier the identifier to resolve
     * @return the declaration or null if nothing is found
     */
    @Override
    public Declaration resolve(Location location, ResolvableIdentifier identifier) {
        Declaration declaration = resolveMember(identifier);

        if (declaration != null) {
            return declaration;
        }
        return super.resolve(location, identifier);
    }

    /**
     * Resolve an identifier for list of overloaded procedures or functions.
     * <p>
     * It first searches its associations, the parent classes in inheritance
     * hierachy and only then the parent scope.
     *
     * @param identifier the identifier to resolve
     * @return the list of procedure declarations
     */
    @Override
    public List<ProcedureDeclaration> resolveProcedure(Location location,
            ResolvableIdentifier identifier) {
        List<ProcedureDeclaration> result = new ArrayList<ProcedureDeclaration>();
        result.addAll(resolveProcedureMember(location, identifier));
        if (this.parent != null) {
            try {
                result.addAll(this.parent.resolveProcedure(location, identifier));
            } catch (UnknownIdentifierException e) {
            }
        }
        if (result.isEmpty()) {
            throw new UnknownIdentifierException(location, identifier);
        }
        return result;
    }
}
