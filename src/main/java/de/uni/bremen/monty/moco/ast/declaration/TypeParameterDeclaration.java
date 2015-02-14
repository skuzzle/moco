package de.uni.bremen.monty.moco.ast.declaration;

import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.Position;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;

public class TypeParameterDeclaration extends TypeDeclaration {

    public TypeParameterDeclaration(Position position, Identifier identifier) {
        super(position, identifier);
    }

    @Override
    public void visit(BaseVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void visitChildren(BaseVisitor visitor) {}

}
