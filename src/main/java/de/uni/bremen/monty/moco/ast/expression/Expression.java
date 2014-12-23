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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni.bremen.monty.moco.ast.BasicASTNode;
import de.uni.bremen.monty.moco.ast.Position;
import de.uni.bremen.monty.moco.ast.declaration.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Typed;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Unification;

/** The base class for every expression.
 * <p>
 * An expression has a type which must be set by a visitor. */
public abstract class Expression extends BasicASTNode implements Typed {

    public class AddTypeBuilderImpl implements AddTypeBuilder {
        private final Type type;

        private AddTypeBuilderImpl(Type type) {
            this.type = type;
        }

        /**
         * Adds a constraint to the type which has been added by the before-hand
         * call to {@link Expression#addType(Type)}.
         *
         * @param unification The unification which constrains type variables to
         *            certain substitutions.
         */
        @Override
        public void withConstraint(Unification unification) {
            if (unification == null) {
                throw new IllegalArgumentException("unification is null");
            }

            Expression.this.constraints.put(this.type, unification);
        }
    }

	/** Possible types for this expression */
    private final List<Type> types;

    /** The resolved unique type */
    private Type unique;

    private final Map<Type, Unification> constraints;

	/** Constructor.
	 *
	 * @param position
	 *            Position of this node */
	public Expression(Position position) {
		super(position);
		this.types = new ArrayList<>();
        this.constraints = new HashMap<>();
	}

	/**
	 * Whether an unique type has been resolved for this node.
	 * @return Whether the type has been resolved.
	 */
	@Override
    public boolean isTypeResolved() {
        return this.unique != null;
	}

    @Override
    public AddTypeBuilder addType(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }

	    this.types.add(type);
        return new AddTypeBuilderImpl(type);
	}


    @Override
    public List<Type> getTypes() {
        return Collections.unmodifiableList(this.types);
    }

    @Override
    public void setType(Type unique) {
        if (unique == null) {
            throw new IllegalArgumentException("unique is null");
        }

        this.unique = unique;
    }

    @Override
    public Type getType() {
        return this.unique;
    }
}
