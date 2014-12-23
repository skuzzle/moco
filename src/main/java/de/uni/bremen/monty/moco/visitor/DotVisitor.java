package de.uni.bremen.monty.moco.visitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.Block;
import de.uni.bremen.monty.moco.ast.Import;
import de.uni.bremen.monty.moco.ast.Package;
import de.uni.bremen.monty.moco.ast.ResolvableIdentifier;
import de.uni.bremen.monty.moco.ast.declaration.ClassDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.Declaration;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ModuleDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.expression.CastExpression;
import de.uni.bremen.monty.moco.ast.expression.ConditionalExpression;
import de.uni.bremen.monty.moco.ast.expression.Expression;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;
import de.uni.bremen.monty.moco.ast.expression.IsExpression;
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
import de.uni.bremen.monty.moco.ast.statement.BreakStatement;
import de.uni.bremen.monty.moco.ast.statement.ConditionalStatement;
import de.uni.bremen.monty.moco.ast.statement.ContinueStatement;
import de.uni.bremen.monty.moco.ast.statement.ReturnStatement;
import de.uni.bremen.monty.moco.ast.statement.SkipStatement;
import de.uni.bremen.monty.moco.ast.statement.WhileLoop;
import de.uni.bremen.monty.moco.util.DotBuilder;

public class DotVisitor extends BaseVisitor implements AutoCloseable {

    public static DotVisitor toFile(File dotFile) throws FileNotFoundException {
        return new DotVisitor(new PrintStream(dotFile));
    }

    private final DotBuilder dotBuilder;

    public DotVisitor(PrintStream out) {
        if (out == null) {
            throw new IllegalArgumentException("out is null");
        }

        this.dotBuilder = new DotBuilder(out);
    }

    @Override
    public void visit(FunctionDeclaration node) {
        final String description = String.format("FuncDecl (%s) '%s'",
                node.getDeclarationType(), node.getIdentifier());
        this.dotBuilder.printNode(node,
                description,
                node.getPosition().toString(),
                "Declared Type: " + node.getReturnTypeIdentifier());

        super.visit(node);

        for (final VariableDeclaration param : node.getParameter()) {
            this.dotBuilder.printEdge(node, param, "param");
        }
        this.dotBuilder.printEdge(node, node.getBody(), "body");
    }

    @Override
    public void visit(ProcedureDeclaration node) {
        final String description = String.format("ProcDecl (%s) '%s'",
                node.getDeclarationType(), node.getIdentifier());
        this.dotBuilder.printNode(node, description, node.getPosition().toString());

        super.visit(node);

        for (final VariableDeclaration param : node.getParameter()) {
            this.dotBuilder.printEdge(node, param, "param");
        }
        this.dotBuilder.printEdge(node, node.getBody(), "body");
    }

    @Override
    public void visit(VariableDeclaration node) {
        final String description = String.format("Decl (%s) '%s'%s",
                node.getDeclarationType(), node.getIdentifier(),
                node.getIsGlobal()
                        ? " global"
                        : "");
        this.dotBuilder.printNode(node,
                description,
                node.getPosition().toString());
    }

    @Override
    public void visit(ClassDeclaration node) {
        final StringBuilder superClasses = new StringBuilder();
        final Iterator<ResolvableIdentifier> names = node.getSuperClassIdentifiers().iterator();

        while (names.hasNext()) {
            superClasses.append(names.next().getSymbol());
            if (names.hasNext()) {
                superClasses.append(", ");
            }
        }

        this.dotBuilder.printNode(node,
                String.format("ClassDecl '%s'", node.getIdentifier()),
                node.getPosition().toString(),
                String.format("Super classes: %s", superClasses));

        super.visit(node);

        this.dotBuilder.printEdge(node, node.getBlock(), "body");
    }

    @Override
    public void visit(Assignment node) {
        this.dotBuilder.printNode(node, "Assign", node.getPosition().toString());
        super.visit(node);
        this.dotBuilder.printEdge(node, node.getLeft(), "lhs");
        this.dotBuilder.printEdge(node, node.getRight(), "rhs");
    }

    @Override
    public void visit(ArrayLiteral node) {
        printExpression(node);
        super.visit(node);
        for (final Expression entry : node.getEntries()) {
            this.dotBuilder.printEdge(node, entry, "");
        }
    }

    @Override
    public void visit(BooleanLiteral node) {
        printLiteral(node, node.getValue());
    }

    @Override
    public void visit(CharacterLiteral node) {
        printLiteral(node, node.getValue());
    }

    @Override
    public void visit(FloatLiteral node) {
        printLiteral(node, node.getValue());
    }

    @Override
    public void visit(IntegerLiteral node) {
        printLiteral(node, node.getValue());
    }

    @Override
    public void visit(StringLiteral node) {
        printLiteral(node, node.getValue());
    }

    @Override
    public void visit(Block node) {
        this.dotBuilder.printNode(node, "Block", node.getPosition().toString());
        super.visit(node);

        for (final Declaration decl : node.getDeclarations()) {
            this.dotBuilder.printEdge(node, decl, "");
        }
        for (final ASTNode stmt : node.getStatements()) {
            this.dotBuilder.printEdge(node, stmt, "");
        }
    }

    @Override
    public void visit(BreakStatement node) {
        this.dotBuilder.printNode(node, "Break", node.getPosition().toString());
        // TODO: add edge to enclosing loop if already resolved
        super.visit(node);
    }

    @Override
    public void visit(CastExpression node) {
        printExpression(node);
        super.visit(node);
        this.dotBuilder.printEdge(node, node.getExpression(), "");
    }

    @Override
    public void visit(ConditionalExpression node) {
        printExpression(node);
        super.visit(node);
        this.dotBuilder.printEdge(node, node.getCondition(), "condition");
        this.dotBuilder.printEdge(node, node.getThenExpression(), "then");
        this.dotBuilder.printEdge(node, node.getElseExpression(), "else");
    }

    @Override
    public void visit(FunctionCall node) {
        this.dotBuilder.printNode(node,
                "Call " + node.getIdentifier(),
                node.getPosition().toString());

        super.visit(node);

        for (final Expression parameter : node.getArguments()) {
            this.dotBuilder.printEdge(node, parameter, "");
        }
    }

    @Override
    public void visit(ConditionalStatement node) {
        this.dotBuilder.printNode(node, "if", node.getPosition().toString());
        super.visit(node);
        this.dotBuilder.printEdge(node, node.getCondition(), "condition");
        this.dotBuilder.printEdge(node, node.getThenBlock(), "then");
        this.dotBuilder.printEdge(node, node.getElseBlock(), "else");
    }

    @Override
    public void visit(ContinueStatement node) {
        this.dotBuilder.printNode(node, "Continue", node.getPosition().toString());
    }

    @Override
    public void visit(Import node) {
        this.dotBuilder.printNode(node, "Import", node.getPosition().toString());
    }

    @Override
    public void visit(IsExpression node) {
        this.dotBuilder.printNode(node,
                "Is " + node.getIsIdentifier(),
                node.getPosition().toString());

        super.visit(node);

        this.dotBuilder.printEdge(node, node.getExpression(), "");
    }

    @Override
    public void visit(MemberAccess node) {
        printExpression(node);
        super.visit(node);
        this.dotBuilder.printEdge(node, node.getLeft(), "lhs");
        this.dotBuilder.printEdge(node, node.getRight(), "rhs");
    }

    @Override
    public void visit(ModuleDeclaration node) {
        this.dotBuilder.printNode(node, String.format("Module '%s'", node.getIdentifier()),
                node.getPosition().toString());
        super.visit(node);
        for (final Import imp : node.getImports()) {
            this.dotBuilder.printEdge(node, imp, "");
        }
        this.dotBuilder.printEdge(node, node.getBlock(), "");
    }

    @Override
    public void visit(Package node) {
        this.dotBuilder.printNode(node, "Package", node.getPosition().toString());
        super.visit(node);
        for (final ModuleDeclaration module : node.getModules()) {
            this.dotBuilder.printEdge(node, module, "");
        }
    }

    @Override
    public void visit(ParentExpression node) {
        printExpression(node);
        super.visit(node);
    }

    @Override
    public void visit(ReturnStatement node) {
        this.dotBuilder.printNode(node, "Return", node.getPosition().toString());
        super.visit(node);
        if (node.getParameter() != null) {
            this.dotBuilder.printEdge(node, node.getParameter(), "");
        }
    }

    @Override
    public void visit(SelfExpression node) {
        printExpression(node);
        super.visit(node);
    }

    @Override
    public void visit(SkipStatement node) {
        this.dotBuilder.printNode(node, "Skip", node.getPosition().toString());
        // TODO: add edge to enclosing loop if already resolved
        super.visit(node);
    }

    @Override
    public void visit(VariableAccess node) {
        this.dotBuilder.printNode(node, "Var " + node.getIdentifier(),
                node.getPosition().toString(),
                "L-Value:" + node.getLValue());
    }

    @Override
    public void visit(WhileLoop node) {
        this.dotBuilder.printNode(node, "While", node.getPosition().toString());
        super.visit(node);
        this.dotBuilder.printEdge(node, node.getCondition(), "cond");
        this.dotBuilder.printEdge(node, node.getBody(), "");
    }

    private void printLiteral(Expression node, Object value) {
        final String description = node.getClass().getSimpleName();
        this.dotBuilder.printNode(node,
                description,
                node.getPosition().toString(),
                "Value: " + value);
    }

    private void printExpression(Expression node) {
        final String description = node.getClass().getSimpleName();
        this.dotBuilder.printNode(node,
                description,
                node.getPosition().toString());
    }

    @Override
    public void close() throws IOException {
        this.dotBuilder.close();
    }
}
