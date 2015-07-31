package de.uni.bremen.monty.moco.ast;

import java.util.Objects;

import de.uni.bremen.monty.moco.ast.declaration.TypeDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Typed;

public abstract class AbstractTypedASTNode extends BasicASTNode implements Typed {

    /** The node's type */
    private Type type;
    private TypeDeclaration typeDecl;

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
        Objects.requireNonNull(type);
        this.type = type;
    }

    @Override
    public TypeDeclaration getTypeDeclaration() {
        if (this.typeDecl == null) {
            throw new IllegalStateException(String.format(
                    "declaration not resolved for node <%s> (at %s)",
                    this, getPosition()));
        }
        return this.typeDecl;
    }

    @Override
    public void setTypeDeclaration(TypeDeclaration typeDecl) {
        Objects.requireNonNull(typeDecl);
        this.typeDecl = typeDecl;
    }

    @Override
    public boolean isTypeDeclarationResolved() {
        return this.typeDecl != null;
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
