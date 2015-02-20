package de.uni.bremen.monty.moco.visitor.typeinf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uni.bremen.monty.moco.ast.ClassScope;
import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.Scope;
import de.uni.bremen.monty.moco.ast.declaration.ClassDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.Declaration;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration.DeclarationType;
import de.uni.bremen.monty.moco.ast.declaration.TypeDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeInstantiation;
import de.uni.bremen.monty.moco.ast.declaration.TypeParameterDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType.Named;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.TypeVariable;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Typed.TypeContext;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Unification;
import de.uni.bremen.monty.moco.ast.expression.Expression;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;
import de.uni.bremen.monty.moco.ast.expression.MemberAccess;
import de.uni.bremen.monty.moco.ast.expression.ParentExpression;
import de.uni.bremen.monty.moco.ast.expression.SelfExpression;
import de.uni.bremen.monty.moco.ast.expression.VariableAccess;
import de.uni.bremen.monty.moco.ast.expression.literal.ArrayLiteral;
import de.uni.bremen.monty.moco.ast.expression.literal.BooleanLiteral;
import de.uni.bremen.monty.moco.ast.expression.literal.CharacterLiteral;
import de.uni.bremen.monty.moco.ast.expression.literal.FloatLiteral;
import de.uni.bremen.monty.moco.ast.expression.literal.IntegerLiteral;
import de.uni.bremen.monty.moco.ast.expression.literal.StringLiteral;
import de.uni.bremen.monty.moco.ast.statement.Assignment;
import de.uni.bremen.monty.moco.exception.MontyBaseException;
import de.uni.bremen.monty.moco.exception.TypeMismatchException;
import de.uni.bremen.monty.moco.exception.UnknownTypeException;
import de.uni.bremen.monty.moco.util.ASTUtil;

public class FirstPassTypeResolver extends AbstractTypeResolver {

    public FirstPassTypeResolver() {
        setStopOnFirstError(true);
    }


    // Literals


    @Override
    public void visit(BooleanLiteral node) {
        if (shouldVisit(node)) {
            node.addTypeOf(CoreClasses.boolType());
        }
    }

    @Override
    public void visit(CharacterLiteral node) {
        if (shouldVisit(node)) {
            node.addTypeOf(CoreClasses.charType());
        }
    }

    @Override
    public void visit(FloatLiteral node) {
        if (shouldVisit(node)) {
            node.addTypeOf(CoreClasses.floatType());
        }
    }

    @Override
    public void visit(StringLiteral node) {
        if (shouldVisit(node)) {
            node.addTypeOf(CoreClasses.stringType());
        }
    }

    @Override
    public void visit(ArrayLiteral node) {
        if (shouldVisit(node)) {
            node.addTypeOf(CoreClasses.arrayType());
        }
    }

    @Override
    public void visit(IntegerLiteral node) {
        if (shouldVisit(node)) {
            node.addTypeOf(CoreClasses.intType());
        }
    }


    // Declarations

    @Override
    public void visit(TypeInstantiation node) {
        if (!shouldVisit(node)) {
            return;
        }

        final Type type = resolveType(node, node.getTypeName());

        if (type.isVariable() && !node.getTypeArguments().isEmpty()) {
            throw new TypeMismatchException(node, "Type variables can not be quantified");
        } else if (type.isVariable()) {
            node.setType(type);
        } else {
            final ClassType declType = (ClassType) type;
            final Named builder = ClassType
                    .named(node.getTypeName())
                    .atLocation(node)
                    .withSuperClasses(declType.getSuperClasses());
            super.visit(node);
            for (final TypeInstantiation child : node.getTypeArguments()) {
                builder.addTypeParameter(child.getType());
            }
            final ClassType ct = builder.createType();
            final Unification unification = Unification.testIf(ct).isA(declType);
            if (unification.isSuccessful()) {
                node.setType(unification.apply(ct));
            } else {
                throw new TypeMismatchException(node, "huh?!");
            }
        }
    }

    @Override
    public void visit(TypeParameterDeclaration node) {
        if (!shouldVisit(node)) {
            return;
        }
        final TypeVariable type = TypeVariable
                .named(node.getIdentifier())
                .atLocation(node)
                .createType();
        node.setType(type);
    }

    @Override
    public void visit(ClassDeclaration node) {
        if (!shouldVisit(node)) {
            return;
        }

        final ClassScope scope = node.getScope();

        final List<ClassType> superTypes = new ArrayList<>(
                node.getSuperClassIdentifiers().size());
        // Resolve super types first
        for (final TypeInstantiation superType : node.getSuperClassIdentifiers()) {
            visitDoubleDispatched(superType);

            if (superType.getType().isVariable()) {
                // can not inherit from a variable
                throw new TypeMismatchException(node,
                        String.format("%s can not inherit from type variable %s",
                                node.getIdentifier(), superType.getTypeName()));
            }
            assert superType.getType().isClass();
            final ClassDeclaration decl = (ClassDeclaration) scope.resolveFromType(superType.getType());
            decl.visit(this);

            // assert this.visited.contains(decl);

            node.addSuperClassDeclaration(decl);

            if (!decl.isTypeResolved()) {
                // this can only be the case if the same node is visited twice
                throw new MontyBaseException(node, "Cyclic inheritance detected");
            }

            superTypes.add((ClassType) superType.getType());
            scope.addParentClassScope(decl.getScope());
        }

        if (!node.isTypeResolved()) {
            // Types of core classes are already resolved!
            final List<Type> typeParams = new ArrayList<>(node.getTypeParameters().size());
            for (final Identifier typeParam : node.getTypeParameters()) {
                final TypeVariable tv = TypeVariable.named(typeParam)
                        .atLocation(node)
                        .createType();
                typeParams.add(tv);
            }
            final ClassType type = ClassType.named(node.getIdentifier())
                    .withSuperClasses(superTypes)
                    .addTypeParameters(typeParams)
                    .createType();
            node.setType(type);
        }
        visitDoubleDispatched(node.getBlock());
    }

    @Override
    public void visit(VariableDeclaration node) {
        if (shouldVisit(node)) {
            super.visit(node);
            node.setType(node.getTypeIdentifier().getType());
        }
    }

    @Override
    public void visit(ProcedureDeclaration node) {
        if (!shouldVisit(node)) {
            return;
        }

        // resolve parameter types
        for (final VariableDeclaration formalParam : node.getParameter()) {
            formalParam.visit(this);
        }

        final String procedureTypeName;
        final Type returnType;
        if (node.getDeclarationType() == DeclarationType.INITIALIZER) {
            // This is a constructor call
            final ClassDeclaration enclosingClass = ASTUtil.findAncestor(node,
                    ClassDeclaration.class);
            returnType = enclosingClass.getType();
            procedureTypeName = "initializer";
        } else {
            returnType = CoreClasses.voidType().getType();
            procedureTypeName = node.getIdentifier().getSymbol();
        }

        final Function nodeType = Function.named(procedureTypeName)
                .atLocation(node)
                .returning(returnType)
                .andParameters(Type.convert(node.getParameter()))
                .createType();
        node.setType(nodeType);

        node.getBody().visit(this);
    }

    @Override
    public void visit(FunctionDeclaration node) {
        if (!shouldVisit(node)) {
            return;
        }

        // resolve parameter types
        for (final VariableDeclaration formalParam : node.getParameter()) {
            formalParam.visit(this);
        }

        final Type returnType = resolveType(node, node.getReturnTypeIdentifier());
        final Function nodeType = Function.named(node.getIdentifier())
                .atLocation(node)
                .returning(returnType)
                .andParameters(Type.convert(node.getParameter()))
                .createType();
        node.setReturnType(returnType);
        node.setType(nodeType);

        node.getBody().visit(this);
    }


    // Statements


    @Override
    public void visit(Assignment node) {
        if (!shouldVisit(node)) {
            return;
        }
        super.visit(node);
    }


    // Expressions


    @Override
    public void visit(VariableAccess node) {
        if (!shouldVisit(node)) {
            return;
        }
        super.visit(node);

        final Scope scope = node.getScope();
        final Declaration decl = scope.resolve(node, node.getIdentifier());

        if (decl instanceof VariableDeclaration) {
            final VariableDeclaration varDecl = (VariableDeclaration) decl;
            // ensure that the variable's type has been resolved
            visitDoubleDispatched(varDecl);

            node.setDeclaration(decl);
            node.addTypeOf(varDecl);
        } else {
            // TODO: what else?
        }
    }

    @Override
    public void visit(SelfExpression node) {
        if (shouldVisit(node)) {
            final ClassDeclaration enclosing = ASTUtil.findAncestor(node,
                    ClassDeclaration.class);
            node.addTypeOf(enclosing);
        }
    }

    @Override
    public void visit(ParentExpression node) {
        if (shouldVisit(node)) {
            final TypeDeclaration typeDecl = node.getScope().resolveType(node,
                    node.getParentIdentifier());
            node.addTypeOf(typeDecl);
            final ClassDeclaration enclosing = ASTUtil.findAncestor(node,
                    ClassDeclaration.class);
            node.setSelfType(enclosing.getType());
        }
        super.visit(node);
    }

    @Override
    public void visit(FunctionCall node) {
        if (!shouldVisit(node)) {
            return;
        }
        // Resolve parameter types
        for (final Expression actual : node.getArguments()) {
            visitDoubleDispatched(actual);
        }

        // check whether this is a constructor
        TypeDeclaration typeDecl = null;
        try {
            typeDecl = node.getScope().resolveType(node, node.getIdentifier());
        } catch (UnknownTypeException ignore) {
            typeDecl = null;
        }
        node.setConstructorCall(typeDecl);

        // Cartesian product of signature types
        final List<List<Type>> signatureTypes = TypeHelper.signatureTypes(node.getArguments());
        node.setSignatureTypes(signatureTypes);

        // create all possible types given the actual parameter types
        final List<Function> possibleTypes = new ArrayList<>();
        for (final List<Type> signature : signatureTypes) {
            final Function possibleType = Function.named(node.getIdentifier())
                    .atLocation(node)
                    .returning(TypeVariable.anonymous().atLocation(node).createType())
                    .andParameters(signature)
                    .createType();

            possibleTypes.add(possibleType);
        }

        // Find actual declared functions with the given name
        final Collection<Function> declaredTypes;
        if (node.isConstructorCall()) {
            declaredTypes = resolveConstructorTypes(node.getConstructorType(), node);
        } else {
            declaredTypes = resolveTypes(node.getScope(), node.getIdentifier(), node);
        }

        // eliminate all types for which there is no matching declaration
        for (final Function possibleType : possibleTypes) {
            for (final Function declaredType : declaredTypes) {
                final Unification unification = Unification
                        .testIf(declaredType).isA(possibleType);

                // When types are compatible, we found a possible type of this
                // call
                if (unification.isSuccessful()) {
                    final Function type = unification.apply(possibleType);
                    node.addType(type.getReturnType())
                            .withConstraint(unification);
                }
            }
        }
    }

    @Override
    public void visit(MemberAccess node) {
        visitDoubleDispatched(node.getLeft());

        final Set<TypeContext> handledTypes = new HashSet<>();
        for (final TypeContext lhsTypeCtx : node.getLeft().getTypes()) {
            if (lhsTypeCtx.getType() instanceof ClassType) {
                final ClassDeclaration cd = (ClassDeclaration) node.getScope()
                        .resolveFromType(lhsTypeCtx.getType());

                // Visit right node in the context of each possible left type
                node.getRight().setScope(cd.getScope());
                try {
                    node.getRight().visit(this);

                    // Check whether new types have been added to the rhs during
                    // this run
                    for (final TypeContext ctx : node.getRight().getTypes()) {
                        if (handledTypes.add(ctx)) {
                            // this is a type which is valid only in the context
                            // of lhsTypeCtx
                            node.addType(ctx.getType()).qualifiedBy(cd.getType());
                        }
                    }
                } catch (MontyBaseException ignore) {
                    // Error while resolving type of right hand side in the
                    // scope of left hand side. Thus current tested left hand
                    // side type can be sort out of possible types
                }
            }
        }
    }
}
