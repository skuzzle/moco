package de.uni.bremen.monty.moco.visitor.typeinf;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.Location;
import de.uni.bremen.monty.moco.ast.Scope;
import de.uni.bremen.monty.moco.ast.declaration.ClassDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.Declaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeVariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Typed;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;

public class QuantumTypeErasor9k extends BaseVisitor {

    public QuantumTypeErasor9k() {
        setStopOnFirstError(true);
    }

    @Override
    protected void onEnterChildrenEachNode(ASTNode node) {
        if (node instanceof Typed) {
            enterTypedNode(node, node.getScope(), (Typed) node);
        }
    }

    private void enterTypedNode(Location location, Scope scope, Typed node) {
        if (!node.isTypeDeclarationResolved()) {
            return;
        }
        final Declaration decl = node.getTypeDeclaration();
        if (decl instanceof ClassDeclaration) {
            node.setTypeDeclaration((TypeDeclaration) decl);
        } else if (decl instanceof TypeVariableDeclaration) {
            node.setTypeDeclaration(CoreClasses.objectType());
        }
    }
}
