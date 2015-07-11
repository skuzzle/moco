package de.uni.bremen.monty.moco.visitor.typeinf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.NamedNode;
import de.uni.bremen.monty.moco.ast.Package;
import de.uni.bremen.monty.moco.ast.Position;
import de.uni.bremen.monty.moco.ast.ResolvableIdentifier;
import de.uni.bremen.monty.moco.ast.Scope;
import de.uni.bremen.monty.moco.ast.declaration.ClassDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeVariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Unification;
import de.uni.bremen.monty.moco.ast.statement.ReturnStatement;
import de.uni.bremen.monty.moco.util.astsearch.SearchAST;

class ProcedureTypeResolver extends TypeResolverFragment {

    public ProcedureTypeResolver(TypeResolver resolver) {
        super(resolver);
    }

    public void resolveProcedureDeclaration(ProcedureDeclaration node) {
        if (node instanceof FunctionDeclaration) {
            resolveFunctionType((FunctionDeclaration) node);
        } else if (node.isInitializer()) {
            resolveConstructorType(node);
        } else {
            resolveProcedureType(node);
        }
    }

    private void resolveConstructorType(ProcedureDeclaration node) {
        assert node.isInitializer();

        final ClassDeclaration classDecl = SearchAST
                .forParent(ClassDeclaration.class)
                .in(node).get();

        // explicit type args on constructor
        final List<Type> typeArgs = getTypeParametersForConstructor(
                node.getTypeParameters(), classDecl);

        final List<TypeVariableDeclaration> typeVarDecls =
                new ArrayList<>(node.getTypeParameters());
        // Constructors must be quantified equally to the classes they create
        final List<Type> fresh = new ArrayList<>(typeArgs);

        for (final TypeVariableDeclaration typeVar : classDecl.getTypeParameters()) {
            resolveTypeOf(typeVar);

            final TypeVariableDeclaration copy = new TypeVariableDeclaration(
                    typeVar.getPosition(), typeVar.getIdentifier());
            copy.setParentNode(node);
            copy.setScope(node.getScope());
            copy.setType(typeVar.getType());
            copy.setTypeDeclaration(typeVar.getTypeDeclaration());
            fresh.add(typeVar.getType());
            typeVarDecls.add(copy);
        }
        node.setTypeParameters(typeVarDecls);

        final Type returnType = classDecl.getType();

        final List<Type> signature = resolveTypesOf(node.getParameter());
        final Optional<Type> bodyType = getBodyType(node, returnType);

        if (!bodyType.isPresent()) {
            reportError(node, "Could not uniquely determine type of function's body");
        } else if (bodyType.get() != CoreClasses.voidType().getType()) {
            reportError(node, "Constructors must not return a value");
        }
        final Function nodeType = Function.named(classDecl.getIdentifier())
                .atLocation(node)
                .returning(returnType)
                .quantifiedBy(fresh)
                .andParameters(signature)
                .createType();
        final Function unified = nodeType;
        node.setType(unified);
        node.setTypeDeclaration(classDecl);
    }

    private void resolveProcedureType(ProcedureDeclaration node) {
        final List<Type> typeArgs = resolveTypesOf(node.getTypeParameters());

        final Type returnType = CoreClasses.voidType().getType();
        final List<Type> signature = resolveTypesOf(node.getParameter());

        // set type before checking body
        final Function intermediate = Function.named(node.getIdentifier())
                .atLocation(node)
                .returningVoid()
                .andParameters(signature)
                .quantifiedBy(typeArgs)
                .createType();
        node.setType(intermediate);
        node.setTypeDeclaration(CoreClasses.voidType());

        final Optional<Type> bodyType = getBodyType(node, returnType);
        if (!bodyType.isPresent()) {
            reportError(node, "Could not uniquely determine type of function's body");
        } else if (bodyType.get() != returnType) {
            reportError(node, "Procedures must not return a value");
        }
        validateOverride(node);
    }

    private void resolveFunctionType(FunctionDeclaration node) {
        final List<Type> typeArgs = resolveTypesOf(node.getTypeParameters());

        resolveTypeOf(node.getReturnTypeIdentifier());
        final Type declaredReturnType = node.getReturnTypeIdentifier().getType();
        final List<Type> signature = resolveTypesOf(node.getParameter());

        final Type declared = Function.named(node.getIdentifier())
                .atLocation(node)
                .returning(declaredReturnType)
                .andParameters(signature)
                .quantifiedBy(typeArgs)
                .createType();

        // set intermediate type in case there is a recursive call
        node.setType(declared);
        node.setTypeDeclaration(node.getReturnTypeIdentifier().getTypeDeclaration());

        final Optional<Type> bodyType = getBodyType(node, declaredReturnType);
        if (!bodyType.isPresent()) {
            reportError(node, "Could not uniquely determine type of function's body");
        }
        final Type returnType;
        if (node.getReturnTypeIdentifier().getIdentifier().isTypeVariableIdentifier()) {
            returnType = bodyType.get();

            // if inferred type is a procedure, add a return statement
            if (returnType.equals(CoreClasses.voidType().getType())) {
                final ReturnStatement retStmt = new ReturnStatement(
                        Position.UNKNOWN_POSITION, null);
                retStmt.setParentNode(node.getBody());
                node.getBody().addStatement(retStmt);
            }
        } else {
            returnType = declaredReturnType;
        }

        if (!bodyType.get().isA(returnType)) {
            reportError(node, "Body type <%s> not compatible with return type <%s>",
                    bodyType.get(), returnType);
        }

        final Function nodeType = Function.named(node.getIdentifier())
                .atLocation(node)
                .returning(returnType)
                .andParameters(signature)
                .createType();
        node.setType(nodeType);
        final TypeDeclaration typeDecl = node.getScope().resolveRawType(node,
                nodeType.getReturnType());
        node.setTypeDeclaration(typeDecl);

        if (node.getReturnTypeIdentifier().getIdentifier().isTypeVariableIdentifier() &&
            node.getType().asFunction().getReturnType().isVariable() &&
            node.getType().asFunction().getReturnType().asVariable().isIntermediate()) {

            reportError(node.getReturnTypeIdentifier(),
                    "Could not infer return type of <%s>", node.getIdentifier());
        }
        validateOverride(node);
    }

    private List<Type> getTypeParametersForConstructor(
            Collection<TypeVariableDeclaration> typeParams, ClassDeclaration classDecl) {
        final Set<String> names = classDecl.getTypeParameters().stream()
                .map(NamedNode::getIdentifier)
                .map(Identifier::getSymbol)
                .collect(Collectors.toSet());
        final List<Type> types = new ArrayList<>(typeParams.size());
        for (final TypeVariableDeclaration typeParam : typeParams) {
            if (names.contains(typeParam.getIdentifier().getSymbol())) {
                reportError(typeParam,
                        "Constructor can not redeclare generic parameter <%s>",
                        typeParam.getIdentifier());
            }
            resolveTypeOf(typeParam);
            types.add(typeParam.getType());
        }
        return types;
    }

    private Optional<Type> getBodyType(ProcedureDeclaration node, Type declaredType) {
        // resolve body's type
        resolveTypeOf(node.getBody());

        final Collection<ReturnStatement> stmts = node.getReturnStatements();
        final Set<Type> returnTypes = stmts.stream()
                .map(stmt -> stmt.getParameter() == null
                        ? CoreClasses.voidType().getType()
                        : stmt.getParameter().getType())
                .collect(Collectors.toCollection(() -> new HashSet<>(stmts.size())));

        if (returnTypes.isEmpty()) {
            final Optional<Package> pkg = SearchAST
                    .forParent(Package.class)
                    .where(Package::isNativePackage)
                    .in(node);
            if (pkg.isPresent()) {
                // everything is allowed in a native package!
                return Optional.of(declaredType);
            }
            return Optional.of(CoreClasses.voidType().getType());
        }
        return TypeHelper.findCommonType(returnTypes, node.getScope());
    }

    private void validateOverride(ProcedureDeclaration decl) {
        if (decl.isUnbound()) {
            return;
        }
        final Scope scope = decl.getScope();
        final ResolvableIdentifier name = ResolvableIdentifier.of(decl.getIdentifier());
        final List<ProcedureDeclaration> overloads = scope.resolveProcedure(decl, name);

        final Optional<ProcedureDeclaration> overridden = scope.getOverridden(this, decl);
        if (overridden.isPresent()) {
            final Unification test = Unification
                    .given(overridden.get().getScope())
                    .testIf(decl.getType().asFunction().getReturnType())
                    .isA(overridden.get().getType().asFunction().getReturnType());

            if (!test.isSuccessful()) {
                reportError(decl,
                        "Return type <%s> not compatible to overridden return type <%s>",
                        decl.getType().asFunction().getReturnType(),
                        overridden.get().getType().asFunction().getReturnType());
            }
        }
    }
}
