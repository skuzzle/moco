package de.uni.bremen.monty.moco.visitor.typeinf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
import de.uni.bremen.monty.moco.ast.declaration.TypeDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeInstantiation;
import de.uni.bremen.monty.moco.ast.declaration.TypeVariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.ClassType.ClassNamed;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.TypeVariable;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Unification;
import de.uni.bremen.monty.moco.ast.expression.ConditionalExpression;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;
import de.uni.bremen.monty.moco.ast.expression.MemberAccess;
import de.uni.bremen.monty.moco.ast.expression.ParentExpression;
import de.uni.bremen.monty.moco.ast.expression.SelfExpression;
import de.uni.bremen.monty.moco.ast.expression.VariableAccess;
import de.uni.bremen.monty.moco.ast.expression.literal.BooleanLiteral;
import de.uni.bremen.monty.moco.ast.expression.literal.CharacterLiteral;
import de.uni.bremen.monty.moco.ast.expression.literal.FloatLiteral;
import de.uni.bremen.monty.moco.ast.expression.literal.IntegerLiteral;
import de.uni.bremen.monty.moco.ast.expression.literal.StringLiteral;
import de.uni.bremen.monty.moco.ast.statement.Assignment;
import de.uni.bremen.monty.moco.util.astsearch.SearchAST;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;

public class QuantumTypeResolver3000 extends BaseVisitor implements TypeResolver {

    private final Set<ASTNode> visited = new HashSet<>();

    public QuantumTypeResolver3000() {
        setStopOnFirstError(true);
    }

    private boolean shouldVisit(ASTNode node) {
        return this.visited.add(node);
    }

    @Override
    public void resolveTypeOf(ASTNode node) {
        visitDoubleDispatched(node);
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
                typeVar.setArtificial(true);
                node.setTypeDeclaration(typeVar);
                scope.define(typeVar);
            } else {
                final TypeDeclaration typeDecl = scope.resolveType(node, typeName);
                node.setTypeDeclaration(typeDecl);
            }
            node.getTypeDeclaration().addUsage(node);
            node.setType(typeBinding);
            node.setUnification(Unification.EMPTY);
            return;
        }

        assert typeBinding.isClass();
        final ClassType classBinding = typeBinding.asClass();

        // Ensure that the referenced class's type has been resolved
        final TypeDeclaration decl = node.getScope()
                .resolveType(node, typeBinding.asClass());
        resolveTypeOf(decl);
        decl.addUsage(node);

        // resolve nested quantifications
        super.visit(node);

        if (node.getTypeArguments().size() != classBinding.getTypeParameters().size()) {
            reportError(node, "Type parameter count mismatch");
        }

        Unification merged = Unification.EMPTY;
        final List<Type> typeArgs = new ArrayList<>(node.getTypeArguments().size());
        for (final TypeInstantiation arg : node.getTypeArguments()) {
            typeArgs.add(arg.getType());
            merged = merged.merge(arg.getUnification());
        }

        final Unification unification = Unification
                .substitute(classBinding.getTypeParameters())
                .simultaneousFor(typeArgs)
                .merge(merged);

        final Type instance = unification.apply(typeBinding);
        node.setType(instance);
        node.setUnification(unification);
        node.setTypeDeclaration(decl);
    }

    @Override
    public void visit(TypeVariableDeclaration node) {
        if (!shouldVisit(node)) {
            return;
        }
        final Type var = TypeVariable
                .named(node.getIdentifier())
                .atLocation(node)
                .createType();
        node.setType(var);
        // safe is safe!
        node.setTypeDeclaration(node);
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
        for (final TypeVariableDeclaration typeParam : node.getTypeParameters()) {
            resolveTypeOf(typeParam);
            builder.addTypeParameter(typeParam.getType());
        }

        // resolve super classes
        for (final TypeInstantiation superClass : node.getSuperClassIdentifiers()) {
            resolveTypeOf(superClass);
            assert superClass.isTypeResolved();

            if (superClass.getType().isVariable()) {
                // XYZ inherits ? OR XYZ<A> inherits A
                reportError(superClass, "Can not inherit from type variable");
            }

            assert superClass.getType().isClass();
            assert superClass.getTypeDeclaration() != null;

            // HINT: super classes added here have unbound type variables! (
            // thus their types are not suitable for resolving the type of
            // parent expressions)
            final ClassDeclaration superClassDecl = (ClassDeclaration) superClass.getTypeDeclaration();
            node.addSuperClassDeclaration(superClassDecl);
            builder.withSuperClass(superClass.getType().asClass());
            scope.addParentClassScope(superClassDecl.getScope(),
                    superClass.getUnification());
        }

        node.setType(builder.createType());
        node.setTypeDeclaration(node);
        node.getBlock().visit(this);

        fillVTable(node);
    }

    private void fillVTable(ClassDeclaration node) {
        int attributeIndex = 1;
        final List<ProcedureDeclaration> virtualMethodTable = node.getVirtualMethodTable();
        final List<TypeDeclaration> superClasses = node.getSuperClassDeclarations();
        // This can only deal with single inheritance!
        if (!superClasses.isEmpty()) {
            TypeDeclaration type = superClasses.get(0);
            if (type instanceof ClassDeclaration) {
                ClassDeclaration clazz = (ClassDeclaration) type;
                attributeIndex = clazz.getLastAttributeIndex();
                virtualMethodTable.addAll(clazz.getVirtualMethodTable());
            }
        }

        // Make room for the ctable pointer
        int vmtIndex = virtualMethodTable.size() + 1;

        for (Declaration decl : node.getBlock().getDeclarations()) {
            if (decl instanceof VariableDeclaration) {
                VariableDeclaration varDecl = (VariableDeclaration) decl;
                varDecl.setAttributeIndex(attributeIndex++);
            } else if (decl instanceof ProcedureDeclaration) {
                ProcedureDeclaration procDecl = (ProcedureDeclaration) decl;
                if (!procDecl.isInitializer()) {
                    // TODO: overload handling in VMT generation
                    boolean foundEntry = false;
                    for (int i = 0; !foundEntry && i < virtualMethodTable.size(); i++) {
                        ProcedureDeclaration vmtEntry = virtualMethodTable.get(i);
                        if (procDecl.matchesType(vmtEntry)) {
                            virtualMethodTable.set(i, procDecl);
                            procDecl.setVMTIndex(vmtEntry.getVMTIndex());
                            foundEntry = true;
                        }
                    }
                    if (!foundEntry) {
                        virtualMethodTable.add(procDecl);
                        procDecl.setVMTIndex(vmtIndex++);
                    }
                }
            }
        }
        node.setLastAttributeIndex(attributeIndex);
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

        new ProcedureTypeResolver(this).resolveProcedureDeclaration(node);
    }

    @Override
    public void visit(FunctionCall node) {
        new CallTypeResolver(this).resolveCallType(node);
    }

    @Override
    public void visit(VariableDeclaration node) {
        if (!shouldVisit(node)) {
            return;
        }
        resolveTypeOf(node.getTypeIdentifier());
        node.setType(node.getTypeIdentifier().getType());
        node.setTypeDeclaration(node.getTypeIdentifier().getTypeDeclaration());
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
        resolveTypeOf(varDecl);
        varDecl.addUsage(node);

        assert varDecl.isTypeResolved();
        final Type nodeType = node.getScope().getSubstitutions().apply(varDecl.getType());
        node.setType(nodeType);
        node.setDeclaration(decl);
        node.setTypeDeclaration(decl.getTypeDeclaration());
    }

    @Override
    public void visit(MemberAccess node) {
        resolveTypeOf(node.getLeft());
        assert node.getLeft().isTypeResolved();

        if (node.getLeft().getType().isVariable()) {
            reportError(node.getLeft(), "Could not infer left hand type of member access");
        }
        assert node.getLeft().getType().isClass();
        final ClassType instanceType = node.getLeft().getType().asClass();
        final TypeDeclaration raw = node.getScope().resolveType(node, instanceType);

        final Unification subst = Unification.testIf(raw.getType()).isA(instanceType);
        // Resolve type of the right hand node in the scope of the left hand
        // node. This will yield the raw (declared type) of the right hand node.
        // It must therefore be run through the substitution which binds type
        // variables

        // Create a sub scope in which type variables of the LHS are defined
        final Scope memberScope = new Scope("$member" + raw.getIdentifier(),
                raw.getScope());
        memberScope.defineSubstitutions(subst);
        node.getRight().setScope(memberScope);
        resolveTypeOf(node.getRight());

        assert node.getRight().isTypeResolved();

        if (raw instanceof ClassDeclaration) {
            final ClassScope rawScope = (ClassScope) raw.getScope();
            final Unification typeVarBindings = rawScope.getSubstitutions().merge(subst);
            // We need to substitute type variables of the right with their
            // bindings from
            // the left scope

            final Type rightType = typeVarBindings.apply(node.getRight());
            node.setType(rightType);
        } else {
            node.setType(node.getRight().getType());
        }
        node.setTypeDeclaration(node.getRight().getTypeDeclaration());
    }

    @Override
    public void visit(Assignment node) {
        super.visit(node);

        final Unification unification = Unification
                .given(node.getScope())
                .testIf(node.getRight())
                .isA(node.getLeft());

        if (!unification.isSuccessful()) {
            reportError(node, "Can not assign <%s> to <%s>", node.getRight().getType(),
                    node.getLeft().getType());
        }

        PushDown.unification(unification).into(node);
        if (node.getLeft() instanceof VariableAccess) {
            ((VariableAccess) node.getLeft()).setLValue();
        } else if (node.getLeft() instanceof MemberAccess) {
            MemberAccess ma = (MemberAccess) node.getLeft();
            if (ma.getRight() instanceof VariableAccess) {
                ((VariableAccess) ma.getRight()).setLValue();
            }
        } else {
            reportError(node, "Left side is no variable");
        }
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
        node.setTypeDeclaration(decl);
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
        node.setSelfTypeDecl(decl);

        node.setType(superClass.get().getType());
        node.setTypeDeclaration(superClass.get().getTypeDeclaration());
    }

    @Override
    public void visit(ConditionalExpression node) {
        resolveTypeOf(node.getCondition());
        final Unification condition = Unification
                .testIf(node.getCondition().getType())
                .isA(CoreClasses.boolType().getType());

        if (!condition.isSuccessful()) {
            reportError(node.getCondition(), "%s is not a bool",
                    node.getCondition().getType());
        }

        // TODO: find common type
        resolveTypeOf(node.getThenExpression());
        resolveTypeOf(node.getElseExpression());

        final Optional<Type> common = TypeHelper.findLeastCommonSuperTyped(
                node.getThenExpression(),
                node.getElseExpression());

        if (!common.isPresent()) {
            reportError(node, "Conditional branches type mismatch: %s != %s", node.getThenExpression().getType(),
                    node.getElseExpression().getType());
        }
        node.setType(common.get());
        // TODO: use proper type declaration
        node.setTypeDeclaration(node.getThenExpression().getTypeDeclaration());
    }

    @Override
    public void visit(StringLiteral node) {
        final ClassDeclaration core = CoreClasses.stringType();
        resolveTypeOf(core);
        node.setType(core.getType());
        node.setTypeDeclaration(core);
    }

    @Override
    public void visit(BooleanLiteral node) {
        final ClassDeclaration core = CoreClasses.boolType();
        resolveTypeOf(core);
        node.setType(core.getType());
        node.setTypeDeclaration(core);
    }

    @Override
    public void visit(FloatLiteral node) {
        final ClassDeclaration core = CoreClasses.floatType();
        resolveTypeOf(core);
        node.setType(core.getType());
        node.setTypeDeclaration(core);
    }

    @Override
    public void visit(IntegerLiteral node) {
        final ClassDeclaration core = CoreClasses.intType();
        resolveTypeOf(core);
        node.setType(core.getType());
        node.setTypeDeclaration(core);
    }

    @Override
    public void visit(CharacterLiteral node) {
        final ClassDeclaration core = CoreClasses.charType();
        resolveTypeOf(core);
        node.setType(core.getType());
        node.setTypeDeclaration(core);
    }

    @Override
    public void reportError(Location location, String message, Object... content) {
        throw new TypeInferenceException(location, String.format(message, content));
    }
}
