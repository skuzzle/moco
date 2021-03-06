package de.uni.bremen.monty.moco.visitor.typeinf;

import java.util.Collection;
import java.util.Objects;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.declaration.TypeDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeVariableDeclaration;
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
            Objects.requireNonNull(root);
            root.visit(new PushDownVisitor(this.unification));
        }
        
        public void into(Collection<? extends ASTNode> nodes) {
            Objects.requireNonNull(nodes);
            final PushDownVisitor visitor = new PushDownVisitor(unification);
            for (final ASTNode root : nodes) {
                root.visit(visitor);
            }
        }
    }

    public static IntoClause unification(Unification unification) {
        Objects.requireNonNull(unification);
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

                // update type declaration only
                final TypeDeclaration raw = node.getScope().resolveRawType(node, unified);
                
                boolean updateRaw = true;
                if (typedNode.isTypeResolved() && typedNode.getTypeDeclaration() instanceof TypeVariableDeclaration) {
                    final TypeVariableDeclaration tvd = (TypeVariableDeclaration) typedNode.getTypeDeclaration();
                    updateRaw = tvd.isArtificial();
                }
                if (updateRaw) {
                    typedNode.setTypeDeclaration(raw);
                }
            }
        }

        @Override
        public void visit(VariableAccess node) {
            onEnterChildrenEachNode(node.getDeclaration());
            super.visit(node);
        }
    }
}
