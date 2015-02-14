package de.uni.bremen.monty.moco.visitor.typeinf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.ClassScope;
import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.Location;
import de.uni.bremen.monty.moco.ast.ResolvableIdentifier;
import de.uni.bremen.monty.moco.ast.Scope;
import de.uni.bremen.monty.moco.ast.declaration.ClassDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.Declaration;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration.DeclarationType;
import de.uni.bremen.monty.moco.ast.declaration.TypeDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType;
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
import de.uni.bremen.monty.moco.exception.UnknownTypeException;
import de.uni.bremen.monty.moco.util.ASTUtil;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;

public class FirstPassTypeResolver extends BaseVisitor {

    private static final String TYPE_VAR = "?";
    private final Set<ASTNode> visited;

    public FirstPassTypeResolver() {
        this.visited = new HashSet<>();
        setStopOnFirstError(true);
    }

    private boolean shouldVisit(ASTNode node) {
        return this.visited.add(node);
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
    public void visit(ClassDeclaration node) {
        if (!shouldVisit(node)) {
            return;
        }

        final ClassScope scope = node.getScope();
        final List<ClassType> superTypes = new ArrayList<>(
                node.getSuperClassIdentifiers().size());
        // Resolve super types first
        for (final ResolvableIdentifier superTypeName : node.getSuperClassIdentifiers()) {
            final ClassDeclaration decl = (ClassDeclaration) node.getScope()
                    .resolveType(node, superTypeName);

            node.addSuperClassDeclaration(decl);
            visitDoubleDispatched(decl);

            if (!decl.isTypeResolved()) {
                throw new MontyBaseException(node, "Cyclic inheritance detected");
            }

            superTypes.add((ClassType) decl.getType());
            scope.addParentClassScope(decl.getScope());
        }

        final ClassType type = ClassType.named(node.getIdentifier())
                .withSuperClasses(superTypes)
                .createType();
        node.setType(type);

        visitDoubleDispatched(node.getBlock());
    }

    @Override
    public void visit(VariableDeclaration node) {
        if (shouldVisit(node)) {
            final Type type = resolveType(node, node.getTypeIdentifier());
            node.setType(type);
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

        final Type returnType;
        if (node.getDeclarationType() == DeclarationType.INITIALIZER) {
            // This is a constructor call
            final ClassDeclaration enclosingClass = ASTUtil.findAncestor(node, ClassDeclaration.class);
            returnType = enclosingClass.getType();
        } else {
            returnType = CoreClasses.voidType().getType();
        }

        final Function nodeType = Function.named(node.getIdentifier())
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

            node.addTypeOf(varDecl);
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
        }
        node.setConstructorCall(typeDecl instanceof ClassDeclaration);

        // Cartesian product of signature types
        final List<List<Type>> signatureTypes = signatureTypes(node.getArguments());
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
            declaredTypes = resolveTypes(typeDecl.getScope(),
                    new ResolvableIdentifier("initializer"), node);
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

    private List<List<Type>> signatureTypes(List<Expression> actual) {
        final List<List<Type>> parameterTypes = new ArrayList<>(actual.size());
        for (final Expression parameter : actual) {
            final List<Type> types = new ArrayList<>();
            for (final TypeContext ctx : parameter.getTypes()) {
                types.add(ctx.getType());
            }
            parameterTypes.add(types);
        }
        return cartesianProduct(parameterTypes);
    }

    /**
     * Create the Cartesian product of given lists.
     *
     * @param lists The list to create the products of.
     * @return The Cartesian product.
     */
    private <T> List<List<T>> cartesianProduct(List<List<T>> lists) {
        final List<List<T>> resultLists = new ArrayList<List<T>>();
        if (lists.size() == 0) {
            resultLists.add(new ArrayList<T>());
            return resultLists;
        } else {
            final List<T> firstList = lists.get(0);
            final List<List<T>> remainingLists = cartesianProduct(
                    lists.subList(1, lists.size()));

            for (T condition : firstList) {
                for (List<T> remainingList : remainingLists) {
                    ArrayList<T> resultList = new ArrayList<T>();
                    resultList.add(condition);
                    resultList.addAll(remainingList);
                    resultLists.add(resultList);
                }
            }
        }
        return resultLists;
    }

    private Type resolveType(ASTNode node, ResolvableIdentifier name) {
        if (name.getSymbol().equals(TYPE_VAR)) {
            return TypeVariable.anonymous().atLocation(node).createType();
        } else {
            final Scope scope = node.getScope();
            final TypeDeclaration declaredType = scope.resolveType(node, name);

            visitDoubleDispatched(declaredType);
            return declaredType.getType();
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
}
