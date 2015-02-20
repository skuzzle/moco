package de.uni.bremen.monty.moco.util.astsearch;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Function;

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

    public static Iterator<ASTNode> depthFirst(ASTNode root) {
        return new SteppingVisitor(root, Deque::addLast, Deque::pollLast);
    }

    public static Iterator<ASTNode> levelOrder(ASTNode root) {
        return new SteppingVisitor(root, Deque::addLast, Deque::pollFirst);
    }

    private final Deque<ASTNode> current;
    private final BiConsumer<Deque<ASTNode>, ASTNode> addOperation;
    private final Function<Deque<ASTNode>, ASTNode> pollOperation;

    private SteppingVisitor(ASTNode root, BiConsumer<Deque<ASTNode>, ASTNode> addOp,
            Function<Deque<ASTNode>, ASTNode> pollOp) {
        this.current = new ArrayDeque<>();
        this.addOperation = addOp;
        this.pollOperation = pollOp;
        this.current.add(root);
    }

    private ASTNode poll() {
        return this.pollOperation.apply(this.current);
    }

    private void addNode(ASTNode node) {
        this.addOperation.accept(this.current, node);
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
        final ASTNode nextNode = poll();
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
        addNode(node);
    }

    @Override
    public void visit(ClassDeclaration node) {
        addNode(node);
    }

    @Override
    public void visit(FunctionDeclaration node) {
        addNode(node);
    }

    @Override
    public void visit(ProcedureDeclaration node) {
        addNode(node);
    }

    @Override
    public void visit(VariableDeclaration node) {
        addNode(node);
    }

    // Expression

    @Override
    public void visit(ConditionalExpression node) {
        addNode(node);
    }

    @Override
    public void visit(SelfExpression node) {
        addNode(node);
    }

    @Override
    public void visit(ParentExpression node) {
        addNode(node);
    }

    @Override
    public void visit(CastExpression node) {
        addNode(node);
    }

    @Override
    public void visit(IsExpression node) {
        addNode(node);
    }

    @Override
    public void visit(FunctionCall node) {
        addNode(node);
    }

    @Override
    public void visit(MemberAccess node) {
        addNode(node);
    }

    @Override
    public void visit(VariableAccess node) {
        addNode(node);
    }

    // Literal

    @Override
    public void visit(BooleanLiteral node) {
        addNode(node);
    }

    @Override
    public void visit(FloatLiteral node) {
        addNode(node);
    }

    @Override
    public void visit(IntegerLiteral node) {
        addNode(node);
    }

    @Override
    public void visit(StringLiteral node) {
        addNode(node);
    }

    @Override
    public void visit(ArrayLiteral node) {
        addNode(node);
    }

    @Override
    public void visit(CharacterLiteral node) {
        addNode(node);
    }

    // Statements

    @Override
    public void visit(Assignment node) {
        addNode(node);
    }

    @Override
    public void visit(BreakStatement node) {
        addNode(node);
    }

    @Override
    public void visit(SkipStatement node) {
        addNode(node);
    }

    @Override
    public void visit(ConditionalStatement node) {
        addNode(node);
    }

    @Override
    public void visit(ContinueStatement node) {
        addNode(node);
    }

    @Override
    public void visit(ReturnStatement node) {
        addNode(node);
    }

    @Override
    public void visit(WhileLoop node) {
        addNode(node);
    }

    @Override
    public void visit(TryStatement node) {
        addNode(node);
    }

    // Other

    @Override
    public void visit(Block node) {
        addNode(node);
    }

    @Override
    public void visit(Package node) {
        addNode(node);
    }

    @Override
    public void visit(Import node) {
        addNode(node);
    }
}
