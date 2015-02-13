package de.uni.bremen.monty.moco.util.astsearch;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.Block;
import de.uni.bremen.monty.moco.ast.Import;
import de.uni.bremen.monty.moco.ast.Package;
import de.uni.bremen.monty.moco.ast.declaration.ClassDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ModuleDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.expression.CastExpression;
import de.uni.bremen.monty.moco.ast.expression.ConditionalExpression;
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
import de.uni.bremen.monty.moco.ast.statement.TryStatement;
import de.uni.bremen.monty.moco.ast.statement.WhileLoop;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;

class SteppingVisitor extends BaseVisitor implements Iterator<ASTNode> {

    private final Queue<ASTNode> current;

    SteppingVisitor(ASTNode root) {
        this.current = new ArrayDeque<>();
        this.current.add(root);
    }

    @Override
    public boolean hasNext() {
        return !this.current.isEmpty();
    }

    @Override
    public ASTNode next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        final ASTNode nextNode = this.current.poll();
        nextNode.visitChildren(this);
        return nextNode;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    // Declaration

    @Override
    public void visit(ModuleDeclaration node) {
        this.current.add(node);
    }

    @Override
    public void visit(ClassDeclaration node) {
        this.current.add(node);
    }

    @Override
    public void visit(FunctionDeclaration node) {
        this.current.add(node);
    }

    @Override
    public void visit(ProcedureDeclaration node) {
        this.current.add(node);
    }

    @Override
    public void visit(VariableDeclaration node) {
        this.current.add(node);
    }

    // Expression

    @Override
    public void visit(ConditionalExpression node) {
        this.current.add(node);
    }

    @Override
    public void visit(SelfExpression node) {
        this.current.add(node);
    }

    @Override
    public void visit(ParentExpression node) {
        this.current.add(node);
    }

    @Override
    public void visit(CastExpression node) {
        this.current.add(node);
    }

    @Override
    public void visit(IsExpression node) {
        this.current.add(node);
    }

    @Override
    public void visit(FunctionCall node) {
        this.current.add(node);
    }

    @Override
    public void visit(MemberAccess node) {
        this.current.add(node);
    }

    @Override
    public void visit(VariableAccess node) {
        this.current.add(node);
    }

    // Literal

    @Override
    public void visit(BooleanLiteral node) {
        this.current.add(node);
    }

    @Override
    public void visit(FloatLiteral node) {
        this.current.add(node);
    }

    @Override
    public void visit(IntegerLiteral node) {
        this.current.add(node);
    }

    @Override
    public void visit(StringLiteral node) {
        this.current.add(node);
    }

    @Override
    public void visit(ArrayLiteral node) {
        this.current.add(node);
    }

    @Override
    public void visit(CharacterLiteral node) {
        this.current.add(node);
    }

    // Statements

    @Override
    public void visit(Assignment node) {
        this.current.add(node);
    }

    @Override
    public void visit(BreakStatement node) {
        this.current.add(node);
    }

    @Override
    public void visit(SkipStatement node) {
        this.current.add(node);
    }

    @Override
    public void visit(ConditionalStatement node) {
        this.current.add(node);
    }

    @Override
    public void visit(ContinueStatement node) {
        this.current.add(node);
    }

    @Override
    public void visit(ReturnStatement node) {
        this.current.add(node);
    }

    @Override
    public void visit(WhileLoop node) {
        this.current.add(node);
    }

    @Override
    public void visit(TryStatement node) {
        this.current.add(node);
    }

    // Other

    @Override
    public void visit(Block node) {
        this.current.add(node);
    }

    @Override
    public void visit(Package node) {
        this.current.add(node);
    }

    @Override
    public void visit(Import node) {
        this.current.add(node);
    }
}
