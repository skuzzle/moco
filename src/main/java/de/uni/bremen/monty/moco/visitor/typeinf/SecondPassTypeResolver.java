package de.uni.bremen.monty.moco.visitor.typeinf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.Block;
import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.Location;
import de.uni.bremen.monty.moco.ast.ResolvableIdentifier;
import de.uni.bremen.monty.moco.ast.Scope;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Typed.TypeContext;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Unification;
import de.uni.bremen.monty.moco.ast.expression.Expression;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;
import de.uni.bremen.monty.moco.ast.expression.MemberAccess;
import de.uni.bremen.monty.moco.ast.statement.Assignment;
import de.uni.bremen.monty.moco.ast.statement.ConditionalStatement;
import de.uni.bremen.monty.moco.ast.statement.ReturnStatement;
import de.uni.bremen.monty.moco.exception.MontyBaseException;
import de.uni.bremen.monty.moco.exception.TypeMismatchException;
import de.uni.bremen.monty.moco.util.ASTUtil;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;

public class SecondPassTypeResolver extends BaseVisitor {

    private final Set<ASTNode> visited;

    public SecondPassTypeResolver() {
        this.visited = new HashSet<>();
        setStopOnFirstError(true);
    }

    private boolean shouldVisit(ASTNode node) {
        return this.visited.add(node);
    }

    // Declarations

    @Override
    public void visit(FunctionDeclaration node) {
        if (!shouldVisit(node)) {
            return;
        }
        final Type returnType;
        if (node.getReturnType().isVariable()) {
            // build the section of all return types
            final List<Type> possibleTypes = section(node.getReturnStatements());
            if (possibleTypes.isEmpty()) {
                throw new TypeMismatchException(node,
                        "Could not infer a common return type");
            } else if (possibleTypes.size() > 1) {
                returnType = TypeHelper.findCommonSuperType(possibleTypes);
            } else {
                returnType = possibleTypes.get(0);
            }
        } else {
            returnType = node.getReturnType();
        }

        final Type unique = Function
                .named(node.getIdentifier())
                .atLocation(node)
                .returning(returnType)
                .andParameters(Type.convert(node.getParameter()))
                .createType();
        node.setType(unique);
        node.setReturnType(returnType);

        node.getBody().visit(this);
    }


    // Expressions


    @Override
    public void visit(ReturnStatement node) {
        if (!shouldVisit(node)) {
            return;
        }

        final ProcedureDeclaration procedure = ASTUtil.findAncestor(node,
                ProcedureDeclaration.class);

        if (procedure instanceof FunctionDeclaration) {
            final FunctionDeclaration func = (FunctionDeclaration) procedure;
            final Type targetType = func.getReturnType();
            node.getParameter().setType(targetType);
        } else if (node.getParameter() != null) {
            throw new TypeMismatchException(node, "Procedures can not return a value");

        }
        super.visit(node);
    }

    @Override
    public void visit(FunctionCall node) {
        if (!shouldVisit(node)) {
            return;
        }
        // at this point, the unique type of the called function should have
        // already been resolved

        // find candidates with resolved return type
        final List<Function> candidates = new ArrayList<>(node.getSignatureTypes().size());
        final Collection<Function> lhsTypes;
        if (node.isConstructorCall()) {
            final TypeDeclaration typeDecl = node.getScope().resolveType(node,
                    node.getIdentifier());
            lhsTypes = resolveTypes(typeDecl.getScope(),
                    new ResolvableIdentifier("initializer"), node);
        } else {
            lhsTypes = resolveTypes(node.getScope(), node.getIdentifier(), node);
        }

        for (final List<Type> signature : node.getSignatureTypes()) {
            for (final TypeContext retType : node.getTypes()) {
                final Function func = Function.named(node.getIdentifier())
                        .atLocation(node)
                        .returning(retType.getType())
                        .andParameters(signature)
                        .createType();

                for (final Function lhsType : lhsTypes) {
                    final Unification unification = Unification.testIf(func).isA(lhsType);

                    if (unification.isSuccessful()) {
                        candidates.add(unification.apply(func));
                    }
                }

            }
        }
        if (candidates.size() == 1) {
            final Function candidate = candidates.get(0);
            final ProcedureDeclaration decl = (ProcedureDeclaration) node.getScope().resolveFromType(candidate);
            visitDoubleDispatched(decl);
            node.setDeclaration(decl);

            final Function declaredType = (Function) decl.getType();
            node.setType(declaredType.getReturnType());
            final Iterator<Type> it = declaredType.getParameterTypes().iterator();
            for (final Expression actual : node.getArguments()) {
                // push down types
                actual.setType(it.next());
            }
        }
    }

    /**
     * Resolves all possible types of the given call.
     *
     * @param call The call.
     * @return Collection of function types.
     */
    private Collection<Function> resolveTypes(Scope scope,
            ResolvableIdentifier identifier, Location location) {
        final List<ProcedureDeclaration> declarations = scope.resolveProcedure(location,
                identifier);
        final List<Function> result = new ArrayList<>(declarations.size());

        for (final ProcedureDeclaration decl : declarations) {
            // ensure that declaration's type has been resolved
            visitDoubleDispatched(decl);

            result.add((Function) decl.getType());
        }
        return result;
    }

    @Override
    public void visit(MemberAccess node) {
        if (!shouldVisit(node)) {
            return;
        }
        if (node.getParentNode() instanceof Block) {
            // member access is a statement, so there is no parent which could
            // have pushed down its type
            if (node.getTypes().size() == 1) {
                node.setType(node.getTypes().get(0).getType());
            } else {
                throw new MontyBaseException(node, "Ambiguous types: " + node.getTypes());
            }
        }

        final TypeContext ctx = node.getContextFor(node.getType());
        node.getLeft().setType(ctx.getQualification());
        node.getRight().setType(ctx.getType());
        super.visit(node);
    }


    // Statements


    @Override
    public void visit(ConditionalStatement node) {
        if (!shouldVisit(node)) {
            return;
        }
        boolean boolType = false;
        for (final TypeContext ctx : node.getCondition().getTypes()) {
            boolType = ctx.getType() == CoreClasses.boolType().getType();
            if (boolType) {
                break;
            }
        }

        if (!boolType) {
            throw new TypeMismatchException(node.getCondition(),
                    "Condition must be of type Bool");
        }
        node.getCondition().setType(CoreClasses.boolType().getType());

        super.visit(node);
    }

    @Override
    public void visit(Assignment node) {
        if (!shouldVisit(node)) {
            return;
        }

        final Type targetType;
        if (node.getRight().isTypeResolved() && node.getLeft().isTypeResolved()) {
            final Type left = node.getLeft().getType();
            final Type right = node.getRight().getType();
            if (!Unification.testIf(right).isA(left).isSuccessful()) {
                throw new TypeMismatchException(node,
                        String.format("%s not assignable to %s", right, left));
            }
            targetType = left;
        } else if (node.getLeft().isTypeResolved()) {
            targetType = node.getLeft().getType();
        } else if (node.getRight().isTypeResolved()) {
            targetType = node.getRight().getType();
        } else {
            final List<Type> candidates = new ArrayList<>();
            for (final TypeContext lhsCtx : node.getLeft().getTypes()) {
                for (final TypeContext rhsCtx : node.getRight().getTypes()) {
                    final Unification unification = Unification.testIf(lhsCtx.getType())
                            .isA(rhsCtx.getType());
                    if (unification.isSuccessful()) {
                        candidates.add(unification.apply(lhsCtx.getType()));
                    }
                }
            }

            if (candidates.isEmpty()) {
                throw new TypeMismatchException(node, "Incompatible assignment types");
            } else if (candidates.size() > 1) {
                throw new TypeMismatchException(node, "Ambiguous types: " + candidates);
            } else {
                targetType = candidates.get(0);
            }
        }

        node.getLeft().setType(targetType);
        node.getRight().setType(targetType);
        super.visit(node);
    }

    /**
     * Collect all types that occur in every of the given return statements.
     *
     * @param returnStmts The list of return statements.
     * @return The types which occurred in every statement.
     */
    private List<Type> section(List<ReturnStatement> returnStmts) {
        final List<Type> returnTypes = new ArrayList<>(returnStmts.size());

        for (final ReturnStatement outer : returnStmts) {
            final Collection<TypeContext> outerTypes = outer.getParameter().getTypes();

            for (final TypeContext outerType : outerTypes) {
                // check whether outer exists in all others too
                boolean matchAll = true;
                for (final ReturnStatement inner : returnStmts) {
                    if (inner == outer) {
                        continue;
                    }
                    final Collection<TypeContext> innerTypes =
                            inner.getParameter().getTypes();

                    boolean match = false;
                    for (final TypeContext innerType : innerTypes) {
                        match = Unification
                                .testIf(outerType.getType())
                                .isA(innerType.getType())
                                .isSuccessful();
                        if (match) {
                            break;
                        }
                    }
                    matchAll &= match;
                    if (!matchAll) {
                        break;
                    }
                }

                if (matchAll) {
                    // this is a type which occurred in each return statement
                    returnTypes.add(outerType.getType());
                }
            }
        }
        return returnTypes;
    }
}
