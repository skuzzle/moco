package de.uni.bremen.monty.moco.visitor.typeinf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.ClassScope;
import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.Location;
import de.uni.bremen.monty.moco.ast.ResolvableIdentifier;
import de.uni.bremen.monty.moco.ast.Scope;
import de.uni.bremen.monty.moco.ast.declaration.ClassDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.Declaration;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeInstantiation;
import de.uni.bremen.monty.moco.ast.declaration.TypeVariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType.ClassNamed;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.TypeVariable;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Unification;
import de.uni.bremen.monty.moco.ast.expression.Expression;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;
import de.uni.bremen.monty.moco.ast.expression.MemberAccess;
import de.uni.bremen.monty.moco.ast.expression.ParentExpression;
import de.uni.bremen.monty.moco.ast.expression.SelfExpression;
import de.uni.bremen.monty.moco.ast.expression.VariableAccess;
import de.uni.bremen.monty.moco.ast.statement.Assignment;
import de.uni.bremen.monty.moco.exception.UnknownIdentifierException;
import de.uni.bremen.monty.moco.exception.UnknownTypeException;
import de.uni.bremen.monty.moco.util.astsearch.SearchAST;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;

public class QuantumTypeResolver3000 extends BaseVisitor {

    private final Set<ASTNode> visited = new HashSet<>();

    private boolean shouldVisit(ASTNode node) {
        return this.visited.add(node);
    }

    @Override
    public void visit(TypeInstantiation node) {
        // this is either of:
        // ? [ special case ]
        // typename
        // typename < typename >
        // typename < typename < typename > >

        final Scope scope = node.getScope();
        final ResolvableIdentifier typeName = node.getIdentifier();
        final TypeDeclaration typeDecl = scope.resolveType(node, typeName);

        final Type typeBinding = scope.resolveTypeBinding(node, node.getIdentifier());

        if (typeName.isTypeVariableIdentifier() &&
                node.getParentNode() instanceof TypeInstantiation) {
            // forbidden case: typename < ? >
            reportError(node,
                    "Type can not be quantified with anonymous type variable");
        } else if (typeBinding.isVariable() && !node.getTypeArguments().isEmpty()) {
            // forbidden case typevar < typename >
            reportError(node, "Typevariables can not be quantified");
        } else if (typeBinding.isVariable()) {
            // case: '?' or single variable

            if (typeName.isTypeVariableIdentifier()) {
                final TypeVariableDeclaration typeVar = new TypeVariableDeclaration(
                        node.getPosition(), typeBinding.getName());
                typeVar.setType(typeBinding);
                scope.define(typeVar);
            }
            node.setType(typeBinding);
            node.setUnification(Unification.EMPTY);
            return;
        }

        assert typeBinding.isClass();
        final ClassType classBinding = typeBinding.asClass();

        // Ensure that the referenced class's type has been resolved
        final TypeDeclaration decl = node.getScope()
                .resolveType(node, typeBinding.asClass());
        decl.visit(this);
        node.setDeclaration(decl);

        // resolve nested quantifications
        super.visit(node);

        if (node.getTypeArguments().size() != classBinding.getTypeParameters().size()) {
            reportError(node, "Type parameter count mismatch");
        }

        final List<Type> typeArgs = new ArrayList<>(node.getTypeArguments().size());
        for (final TypeInstantiation arg : node.getTypeArguments()) {
            typeArgs.add(arg.getType());
        }

        final Unification unification = Unification
                .substitute(classBinding.getTypeParameters())
                .simultaneousFor(typeArgs);

        final Type instance = unification.apply(typeBinding);
        node.setType(instance);
        node.setUnification(unification);
    }

    @Override
    public void visit(ClassDeclaration node) {
        if (!shouldVisit(node) || node == CoreClasses.voidType()) {
            return;
        }

        final ClassNamed builder = ClassType
                .classNamed(node.getIdentifier())
                .atLocation(node);

        final ClassScope scope = node.getScope();

        // Define type arguments
        for (final Identifier typeParam : node.getTypeParameters()) {
            final TypeDeclaration decl = new TypeVariableDeclaration(node.getPosition(),
                    typeParam);
            final Type var = TypeVariable
                    .named(typeParam)
                    .atLocation(decl)
                    .createType();
            decl.setType(var);
            builder.addTypeParameter(var);
            scope.getParentScope().define(decl);
        }

        // resolve super classes
        for (final TypeInstantiation superClass : node.getSuperClassIdentifiers()) {
            superClass.visit(this);
            assert superClass.isTypeResolved();

            if (superClass.getType().isVariable()) {
                // XYZ inherits ? OR XYZ<A> inherits A
                reportError(superClass, "Can not inherit from type variable");
            }

            assert superClass.getType().isClass();
            assert superClass.getDeclaration() != null;

            // HINT: super classes added here have unbound type variables! (
            // thus their types are not suitable for resolving the type of
            // parent expressions)
            final ClassDeclaration superClassDecl = (ClassDeclaration) superClass.getDeclaration();
            node.addSuperClassDeclaration(superClassDecl);
            builder.withSuperClass(superClass.getType().asClass());
            scope.addParentClassScope(superClassDecl.getScope(),
                    superClass.getUnification());
        }

        node.setType(builder.createType());
        super.visit(node);
    }

    @Override
    public void visit(FunctionDeclaration node) {
        visit((ProcedureDeclaration) node);
    }

    @Override
    public void visit(ProcedureDeclaration node) {
        if (!shouldVisit(node)) {
            return;
        }

        final ProcedureTypeResolver ptr = new ProcedureTypeResolver(this);
        ptr.resolveType(node);
    }


    @Override
    public void visit(FunctionCall node) {
        // resolve parameter types
        final List<Type> signature = new ArrayList<>(node.getArguments().size());
        for (final Expression param : node.getArguments()) {
            param.visit(this);
            signature.add(param.getType());
        }

        List<ProcedureDeclaration> overloads;
        if (checkIsConstructorCall(node)) {
            overloads = getConstructorOverloads(node, node.getConstructorType());
        } else {
            overloads = node.getScope().resolveProcedure(node, node.getIdentifier());
        }

        // sort out recursive call
        overloads = sortOutSelf(node, overloads);

        final Optional<ProcedureDeclaration> target = TypeHelper.bestFit(overloads, node, this);
        if (!target.isPresent()) {
            reportError(node, "Could not uniquely resolve overload of <%s>",
                    node.getIdentifier());
        }
        assert target.get().isTypeResolved();

        final Function fun = target.get().getType().asFunction();
        final Function call = Function.named(node.getIdentifier())
                .atLocation(node).returning(fun.getReturnType())
                .andParameters(signature).createType();

        final Unification unification = Unification.testIf(call).isA(fun);
        final Function unified = unification.apply(fun);
        node.setDeclaration(target.get());
        node.setType(unified.getReturnType());
    }

    private List<ProcedureDeclaration> sortOutSelf(ASTNode call,
            List<ProcedureDeclaration> overloads) {
        final Optional<ProcedureDeclaration> parent = SearchAST
                .forParent(ProcedureDeclaration.class)
                .in(call);
        if (!parent.isPresent() || overloads.size() == 1) {
            return overloads;
        }
        final List<ProcedureDeclaration> result = new ArrayList<>(overloads.size() - 1);
        for (final ProcedureDeclaration overload : overloads) {
            if (overload != parent.get()) {
                result.add(overload);
            }
        }
        return result;
    }

    private List<ProcedureDeclaration> getConstructorOverloads(Location location,
            ClassDeclaration decl) {
        try {
            return decl.getScope().resolveProcedure(location,
                ResolvableIdentifier.of("initializer"));
        } catch (UnknownIdentifierException e) {
            return Collections.singletonList(decl.getDefaultInitializer());
        }
    }

    private boolean checkIsConstructorCall(FunctionCall call) {
        final Scope scope = call.getScope();
        try {
            final TypeDeclaration typeDecl = scope.resolveType(call, call.getIdentifier());
            if (!(typeDecl instanceof ClassDeclaration)) {
                reportError(call, "<%s> is not callable", call.getIdentifier());
            }
            call.setConstructorCall(typeDecl);
            return true;
        } catch (UnknownTypeException | UnknownIdentifierException e) {
        }
        return false;
    }

    @Override
    public void visit(VariableDeclaration node) {
        if (!shouldVisit(node)) {
            return;
        }
        node.getTypeIdentifier().visit(this);
        node.setType(node.getTypeIdentifier().getType());
    }

    @Override
    public void visit(VariableAccess node) {
        final Scope scope = node.getScope();
        final Declaration decl = scope.resolve(node, node.getIdentifier());

        if (!(decl instanceof VariableDeclaration)) {
            reportError(node, "<%s> does not resolve to a variable declaration",
                    node.getIdentifier());
        }
        final VariableDeclaration varDecl = (VariableDeclaration) decl;
        varDecl.visit(this);

        assert varDecl.isTypeResolved();
        node.setType(varDecl.getType().fresh(scope));
        node.setDeclaration(decl);
    }

    @Override
    public void visit(MemberAccess node) {
        node.getLeft().visit(this);
        assert node.getLeft().isTypeResolved();

        if (node.getLeft().getType().isVariable()) {
            reportError(node.getLeft(), "Could not infer left hand type of member access");
        }
        assert node.getLeft().getType().isClass();
        final ClassType instanceType = node.getLeft().getType().asClass();
        final TypeDeclaration raw = node.getScope().resolveType(node, instanceType);


        // Resolve type of the right hand node in the scope of the left hand
        // node. This will yield the raw (declared type) of the right hand node.
        // It must therefore be run through the substitution which binds type
        // variables
        node.getRight().setScope(raw.getScope());
        node.getRight().visit(this);

        assert node.getRight().isTypeResolved();

        if (raw instanceof ClassDeclaration) {
            final ClassScope rawScope = (ClassScope) raw.getScope();
            final Unification typeVarBindings = rawScope.getSubstitutions();
            // We need to substitute type variables of the right with their
            // bindings from
            // the left scope

            final Type rightType = typeVarBindings.apply(node.getRight());
            node.setType(rightType);
        } else {
            node.setType(node.getRight().getType());
        }
    }

    @Override
    public void visit(Assignment node) {
        node.getLeft().visit(this);
        node.getRight().visit(this);

        final Unification unification = Unification
                .testIf(node.getRight())
                .isA(node.getLeft());

        if (!unification.isSuccessful()) {
            reportError(node, "Can not assign <%s> to <%s>", node.getRight().getType(),
                    node.getLeft().getType());
        }

        PushDown.unification(unification).into(node);
    }

    @Override
    public void visit(SelfExpression node) {
        final Optional<ClassDeclaration> parent = SearchAST
                .forParent(ClassDeclaration.class)
                .in(node);

        if (!parent.isPresent()) {
            reportError(node, "No nested class declaration found");
        }

        final ClassDeclaration decl = parent.get();
        node.setType(decl.getType());
    }

    @Override
    public void visit(ParentExpression node) {
        final Optional<ClassDeclaration> parent = SearchAST
                .forParent(ClassDeclaration.class)
                .in(node);

        if (!parent.isPresent()) {
            reportError(node, "No nested class declaration found");
        }
        final ClassDeclaration decl = parent.get();
        assert decl.isTypeResolved();
        final Optional<TypeInstantiation> superClass = decl.getRecursiveParent(
                node.getParentIdentifier());

        if (!superClass.isPresent()) {
            reportError(node, "<%s> is not a super class of <%s>",
                    node.getParentIdentifier(), decl.getIdentifier());
        }
        node.setSelfType(decl.getType());
        node.setType(superClass.get().getType());
    }

    private void reportError(Location location, String message, Object... content) {
        throw new TypeInferenceException(location, String.format(message, content));
    }
}
