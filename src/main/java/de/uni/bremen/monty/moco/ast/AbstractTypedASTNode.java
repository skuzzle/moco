package de.uni.bremen.monty.moco.ast;

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
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }
        this.type = type;
    }

    @Override
    public TypeDeclaration getTypeDeclaration() {
        if (this.typeDecl == null) {
            throw new IllegalStateException(String.format("declaration not resolved (at %s)",
                    getPosition()));
        }
        return this.typeDecl;
    }

    @Override
    public void setTypeDeclaration(TypeDeclaration typeDecl) {
        if (typeDecl == null) {
            throw new IllegalArgumentException("typeDecl is null");
        }
        this.typeDecl = typeDecl;
    }

    @Override
    public boolean isTypeDeclarationResolved() {
        return this.typeDecl != null;
    }

    /**
     * Sets the TypeDeclaration of this node to the TypeDeclaration of the
     * given typed node, only if the given node's type is already resolved.
     *
     * @param other The typed node to obtain the type from.
     */
    public void setTypeDeclarationIfResolved(Typed other) {
        if (other.isTypeDeclarationResolved()) {
            setTypeDeclaration(other.getTypeDeclaration());
        }
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
