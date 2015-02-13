package de.uni.bremen.monty.moco.util.astsearch;

import java.util.Iterator;
import java.util.NoSuchElementException;

import de.uni.bremen.monty.moco.ast.ASTNode;

class ParentIterator implements Iterator<ASTNode> {

    private ASTNode current;

    ParentIterator(ASTNode root) {
        this.current = root;
    }

    @Override
    public boolean hasNext() {
        return this.current != null;
    }

    @Override
    public ASTNode next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        final ASTNode result = this.current;
        this.current = this.current.getParentNode();
        return result;
    }

}
