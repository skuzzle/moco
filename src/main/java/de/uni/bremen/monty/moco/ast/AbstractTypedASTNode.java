package de.uni.bremen.monty.moco.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Typed;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Unification;

public abstract class AbstractTypedASTNode extends BasicASTNode implements Typed {

    private class TypeContextBuilderImpl implements TypeContextBuilder {

        private final TypeContextImpl addedType;

        private TypeContextBuilderImpl(TypeContextImpl addedType) {
            this.addedType = addedType;
        }

        @Override
        public TypeContextBuilder withConstraint(Unification unification) {
            if (unification == null) {
                throw new IllegalArgumentException("unification is null");
            }

            this.addedType.constraint = unification;
            return this;
        }

        @Override
        public TypeContextBuilder qualifiedBy(Type qualification) {
            if (qualification == null) {
                throw new IllegalArgumentException("qualification is null");
            }
            this.addedType.qualification = qualification;
            return this;
        }
    }

    public final class TypeContextImpl implements TypeContext {
        Unification constraint;
        Type qualification;
        final Type type;

        private TypeContextImpl(Type type) {
            this.type = type;
        }

        @Override
        public Type getType() {
            return this.type;
        }

        @Override
        public boolean hasConstraint() {
            return this.constraint != null;
        }

        @Override
        public Unification getConstraint() {
            return hasConstraint()
                    ? this.constraint
                    : Unification.EMPTY;
        }

        @Override
        public boolean isQualified() {
            return this.qualification != null;
        }

        @Override
        public Type getQualification() {
            return this.qualification;
        }

        @Override
        public String toString() {
            final StringBuilder b = new StringBuilder();
            b.append(this.type.toString());
            if (isQualified()) {
                // b.append(" qualified by ").append(this.qualification);
            }
            if (hasConstraint() && !this.constraint.toString().isEmpty()) {
                // b.append(" with constraint ").append(this.constraint);
            }
            return b.toString();
        }
    }

    /** The node's type */
    private Type type;

    /** Possible types of this node */
    private List<TypeContext> types;

    public AbstractTypedASTNode(Position position) {
        super(position);
    }

    @Override
    public boolean isTypeResolved() {
        return this.type != null;
    }

    @Override
    public Type getType() {
        if (!isTypeResolved()) {
            throw new IllegalStateException("type not resolved");
        }
        return this.type;
    }

    @Override
    public void setType(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }
        this.type = type;
    }

    @Override
    public void addTypeContext(TypeContext typeContext) {
        final TypeContextImpl tci = new TypeContextImpl(typeContext.getType());
        if (typeContext.hasConstraint()) {
            tci.constraint = typeContext.getConstraint();
        }
        if (typeContext.isQualified()) {
            tci.qualification = typeContext.getQualification();
        }
        this.types.add(tci);
        return;
    }

    @Override
    public TypeContextBuilder addType(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        } else if (this.types == null) {
            this.types = new ArrayList<>();
        }

        final TypeContextImpl tci = new TypeContextImpl(type);
        this.types.add(tci);
        return new TypeContextBuilderImpl(tci);
    }

    @Override
    public TypeContext getContextFor(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }

        for (final TypeContext ctx : getTypes()) {
            if (ctx.getType().equals(type)) {
                return ctx;
            }
        }
        throw new IllegalStateException(String.format("%s has no context for type %s",
                this, type));
    }

    @Override
    public TypeContextBuilder addTypeOf(Typed typed) {
        if (typed == null) {
            throw new IllegalArgumentException("typed is null");
        }

        return addType(typed.getType());
    }

    @Override
    public List<TypeContext> getTypes() {
        return this.types == null
                ? Collections.<TypeContext> emptyList()
                : Collections.unmodifiableList(this.types);
    }
}
