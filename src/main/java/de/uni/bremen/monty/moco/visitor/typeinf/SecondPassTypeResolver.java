package de.uni.bremen.monty.moco.visitor.typeinf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.Block;
import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.declaration.Declaration;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType;
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

    // Declarations

    @Override
    public void visit(FunctionDeclaration node) {
        final Type returnType;
        if (node.getReturnType().isVariable()) {
            // build the section of all return types
            final List<Type> possibleTypes = section(node.getReturnStatements());
            if (possibleTypes.isEmpty()) {
                throw new TypeMismatchException(node,
                        "Could not infer a common return type");
            } else if (possibleTypes.size() > 1) {
                returnType = findCommonSupertype(node, possibleTypes);
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
        // at this point, the unique type of the called function should have
        // already been resolved

        // find candidates with resolved return type
        final List<Function> candidates = new ArrayList<>(node.getSignatureTypes().size());
        for (final List<Type> signature : node.getSignatureTypes()) {
            final Function func = Function.named(node.getIdentifier())
                    .atLocation(node)
                    .returning(node.getType())
                    .andParameters(signature)
                    .createType();

            for (final TypeContext candidate : node.getTypes()) {
                final Unification unification = Unification
                        .testIf(candidate.getType()).isA(func);

                if (unification.isSuccessful()) {
                    candidates.add(unification.apply(func));
                }
            }
        }
        if (candidates.size() == 1) {
            final Function type = candidates.get(0);
            final Declaration decl = node.getScope().resolveFromType(type);
            node.setDeclaration((ProcedureDeclaration) decl);

            final Iterator<Type> it = type.getParameterTypes().iterator();
            for (final Expression actual : node.getArguments()) {
                // push down types
                actual.setType(it.next());
            }
        }
    }

    @Override
    public void visit(MemberAccess node) {
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
            throw new TypeMismatchException(node, "Ambiguous types");
        } else {
            final Type unique = candidates.get(0);
            node.getLeft().setType(unique);
            node.getRight().setType(unique);
        }

        super.visit(node);
    }


    private Type findCommonSupertype(ASTNode positionHint, List<Type> types) {
        final Map<Type, Set<ClassType>> superTypeMap = new HashMap<>();
        // For each type, collect super types
        final Set<ClassType> commonTypes = new HashSet<>();
        for (final Type type : types) {
            final Set<ClassType> superTypes = new HashSet<>();
            traverseSuperTypes((ClassType) type, superTypes);
            superTypeMap.put(type, superTypes);
            commonTypes.addAll(superTypes);
        }

        // build section of all encountered super types
        for (final Set<ClassType> superTypes : superTypeMap.values()) {
            commonTypes.retainAll(superTypes);
        }
        if (commonTypes.isEmpty()) {
            throw new TypeMismatchException(positionHint, String.format(
                    "The types %s do not share a common super type", types));
        }

        // Chose the most concrete type (
        ClassType minDistanceType = null;
        for (final ClassType type : commonTypes) {
            int dist = type.distanceToObject();
            if (minDistanceType == null || dist > minDistanceType.distanceToObject()) {
                minDistanceType = type;
            }
        }
        return minDistanceType;
    }

    private void traverseSuperTypes(ClassType current, Set<ClassType> types) {
        if (types.add(current)) {
            for (final ClassType parent : current.getSuperClasses()) {
                traverseSuperTypes(parent, types);
            }
        }
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
