package de.uni.bremen.monty.moco.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Typed;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Unification;

public abstract class AbstractTypedASTNode extends BasicASTNode implements Typed {

    private class AddTypeBuilderImpl implements AddTypeBuilder {

        private final Type addedType;

        public AddTypeBuilderImpl(Type addedType) {
            this.addedType = addedType;
        }

        @Override
        public void withConstraint(Unification unification) {
            if (AbstractTypedASTNode.this.constraints == null) {
                AbstractTypedASTNode.this.constraints = new HashMap<>();
            }
            AbstractTypedASTNode.this.constraints.put(this.addedType, unification);
        }
    }

    /** The declaration's type */
    private Type type;

    /** Possible types of this declaration */
    private List<Type> types;

    private Map<Type, Unification> constraints;

    public AbstractTypedASTNode(Position position) {
        super(position);
    }

    @Override
    public boolean isTypeResolved() {
        return this.type != null;
    }

    @Override
    public Type getType() {
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
    public AddTypeBuilder addType(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        } else if (this.types == null) {
            this.types = new ArrayList<>();
        }

        this.types.add(type);
        return new AddTypeBuilderImpl(type);
    }

    @Override
    public List<Type> getTypes() {
        return this.types == null
                ? Collections.<Type> emptyList()
                : Collections.unmodifiableList(this.types);
    }
}
