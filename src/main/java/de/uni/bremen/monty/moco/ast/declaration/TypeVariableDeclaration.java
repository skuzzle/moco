package de.uni.bremen.monty.moco.ast.declaration;

import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.Position;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;

public class TypeVariableDeclaration extends TypeDeclaration {

    private boolean artificial;

    public TypeVariableDeclaration(Position position, Identifier identifier) {
        super(position, identifier);
    }

    public void setArtificial(boolean artificial) {
        this.artificial = artificial;
    }

    public boolean isArtificial() {
        return this.artificial;
    }

    @Override
    public void visit(BaseVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void visitChildren(BaseVisitor visitor) {}

}
