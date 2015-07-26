package de.uni.bremen.monty.moco.ast;

import de.uni.bremen.monty.moco.ast.declaration.Declaration;
import de.uni.bremen.monty.moco.ast.expression.FunctionCall;
import de.uni.bremen.monty.moco.ast.expression.VariableAccess;

/**
 * Represents a node which also has a name, like a {@link Declaration}, a
 * {@link FunctionCall} or a {@link VariableAccess}.
 * 
 * @author Simon Taddiken
 */
public interface NamedNode extends ASTNode {

    /**
     * Gets the node's name.
     * 
     * @return The name.
     */
    public Identifier getIdentifier();
}
