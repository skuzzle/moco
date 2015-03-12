package de.uni.bremen.monty.moco.visitor.typeinf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.Package;
import de.uni.bremen.monty.moco.ast.declaration.ClassDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.statement.ReturnStatement;
import de.uni.bremen.monty.moco.util.astsearch.SearchAST;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;

class ProcedureTypeResolver extends TypeResolverFragment {

    public ProcedureTypeResolver(BaseVisitor resolver) {
        super(resolver);
    }

    public void resolveType(ProcedureDeclaration node) {
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
                .forParent(ClassDeclaration.class).in(node).get();
        final Type returnType = classDecl.getType();
        final List<Type> signature = getParameterTypes(node.getParameter());
        final Optional<Type> bodyType = getBodyType(node, returnType);
        if (!bodyType.isPresent()) {
            reportError(node, "Could not uniquely determine type of function's body");
        } else if (bodyType.get() != CoreClasses.voidType().getType()) {
            reportError(node, "Constructors must not return a value");
        }
        final Function nodeType = Function.named(classDecl.getIdentifier())
                .atLocation(node)
                .returning(returnType)
                .andParameters(signature)
                .createType();
        node.setType(nodeType);
    }

    private void resolveProcedureType(ProcedureDeclaration node) {
        final Type returnType = CoreClasses.voidType().getType();
        final List<Type> signature = getParameterTypes(node.getParameter());
        final Optional<Type> bodyType = getBodyType(node, returnType);
        if (!bodyType.isPresent()) {
            reportError(node, "Could not uniquely determine type of function's body");
        } else if (bodyType.get() != returnType) {
            reportError(node, "Procedures must not return a value");
        }
        final Function nodeType = Function.named(node.getIdentifier())
                .atLocation(node)
                .returning(returnType)
                .andParameters(signature)
                .createType();
        node.setType(nodeType);
    }

    private void resolveFunctionType(FunctionDeclaration node) {
        resolveTypeOf(node.getReturnTypeIdentifier());
        final Type declaredReturnType = node.getReturnTypeIdentifier().getType();
        final List<Type> signature = getParameterTypes(node.getParameter());

        final Type declared = Function.named(node.getIdentifier())
                .atLocation(node)
                .returning(declaredReturnType)
                .andParameters(signature)
                .createType();

        // set intermediate type
        node.setType(declared);

        final Optional<Type> bodyType = getBodyType(node, declaredReturnType);
        if (!bodyType.isPresent()) {
            reportError(node, "Could not uniquely determine type of function's body");
        }
        final Type returnType;
        if (node.getReturnTypeIdentifier().getIdentifier().isTypeVariableIdentifier()) {
            returnType = bodyType.get();
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
    }

    private List<Type> getParameterTypes(Collection<VariableDeclaration> parameters) {
        final List<Type> signature = new ArrayList<>(parameters.size());
        for (final VariableDeclaration decl : parameters) {
            resolveTypeOf(decl);
            signature.add(decl.getType());
        }
        return signature;
    }

    private Optional<Type> getBodyType(ProcedureDeclaration node, Type declaredType) {
        // resolve body's type
        resolveTypeOf(node.getBody());

        final Collection<ReturnStatement> stmts = node.getReturnStatements();
        final Collection<Type> returnTypes = stmts.stream()
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
        return TypeHelper.findLeastCommonSuperType(returnTypes);
    }
}
