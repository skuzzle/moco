package de.uni.bremen.monty.moco.ast;

import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Typed;

public abstract class AbstractTypedASTNode extends BasicASTNode implements Typed {

    /** The node's type */
    private Type type;

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
            throw new IllegalStateException(String.format("type not resolved (at %s)",
                    getPosition()));
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
    public String toString() {
        final StringBuilder b = new StringBuilder(super.toString());
        if (isTypeResolved()) {
            b.append(" [ ").append(this.type).append(" ] ");
        } else {
            b.append(" [ unknown ] ");
        }
        return b.toString();
    }
}
