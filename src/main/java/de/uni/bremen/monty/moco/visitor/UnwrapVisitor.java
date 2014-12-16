package de.uni.bremen.monty.moco.visitor;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.expression.Expression;

/**
 * Replaces all type variables with their resolved types.
 *
 * @author Simon Taddiken
 */
public class UnwrapVisitor extends BaseVisitor {

    @Override
    protected void onEnterChildrenEachNode(ASTNode node) {
        if (node instanceof Expression) {
            final Expression expression = (Expression) node;
            expression.setType(expression.getType().unwrapVariable());
        }
    }

    @Override
    public void visit(VariableDeclaration node) {
        super.visit(node);
        node.setType(node.getType().unwrapVariable());
    }

    @Override
    public void visit(FunctionDeclaration node) {
        super.visit(node);
        node.setReturnType(node.getReturnType().unwrapVariable());
    }
}
