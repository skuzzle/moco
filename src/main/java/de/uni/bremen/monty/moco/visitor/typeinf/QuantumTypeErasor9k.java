package de.uni.bremen.monty.moco.visitor.typeinf;

import java.util.List;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.Location;
import de.uni.bremen.monty.moco.ast.Scope;
import de.uni.bremen.monty.moco.ast.declaration.ClassDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.Declaration;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeVariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Product;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Typed;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Unification;
import de.uni.bremen.monty.moco.exception.RedeclarationException;
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

    @Override
    public void visit(FunctionDeclaration node) {
        super.visit(node);
        //checkRedeclaration(node);
    }

    @Override
    public void visit(ProcedureDeclaration node) {
        super.visit(node);
        //checkRedeclaration(node);
    }

    private void checkRedeclaration(ProcedureDeclaration node) {
        final Scope scope = node.getScope();
        final List<ProcedureDeclaration> overloads =
                scope.resolveProceduresInSameScope(node);
        final Product nodeSig = Unification
                .eraseTypes()
                .apply(node.getType().asFunction().getParameters());

        for (final ProcedureDeclaration overload : overloads) {
            final Product overloadSig = Unification
                    .eraseTypes()
                    .apply(overload.getType().asFunction().getParameters());

            if (overloadSig.equals(nodeSig)) {
                throw new RedeclarationException(node, String.format(
                        "Procedure <%s> has the same erasure type as <%s>",
                        node.getType(), overload.getType()));
            }
        }
    }
}
