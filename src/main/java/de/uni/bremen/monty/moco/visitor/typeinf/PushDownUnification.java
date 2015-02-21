package de.uni.bremen.monty.moco.visitor.typeinf;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Typed;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Unification;
import de.uni.bremen.monty.moco.ast.expression.VariableAccess;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;

final class PushDown {

    public final static class IntoClause {
        final Unification unification;

        private IntoClause(Unification unification) {
            this.unification = unification;
        }

        public void into(ASTNode root) {
            if (root == null) {
                throw new IllegalArgumentException("root is null");
            }
            root.visit(new PushDownVisitor(this.unification));
        }
    }

    public static IntoClause unification(Unification unification) {
        if (unification == null) {
            throw new IllegalArgumentException("unification is null");
        }
        return new IntoClause(unification);
    }


    private static final class PushDownVisitor extends BaseVisitor {
        private final Unification unification;

        private PushDownVisitor(Unification unification) {
            this.unification = unification;
        }

        @Override
        protected void onEnterChildrenEachNode(ASTNode node) {
            if (node instanceof Typed) {
                final Typed typedNode = (Typed) node;
                final Type unified = this.unification.apply(typedNode);
                typedNode.setType(unified);
            }
        }

        @Override
        public void visit(VariableAccess node) {
            onEnterChildrenEachNode(node.getDeclaration());
            super.visit(node);
        }
    }
}
