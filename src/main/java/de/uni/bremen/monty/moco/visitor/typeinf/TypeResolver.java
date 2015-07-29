package de.uni.bremen.monty.moco.visitor.typeinf;

import java.util.ArrayList;
import java.util.List;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.Location;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Typed;

/**
 * Abstraction for resolving the type of a node.
 * 
 * @author Simon Taddiken
 */
public interface TypeResolver {

    /**
     * Resolves the type of the given node and all of its sub nodes.
     * 
     * @param node The root node.
     */
    void resolveTypeOf(ASTNode node);
    
    /**
     * Resolves the types of each of the given nodes. 
     * 
     * @param nodes The list of nodes.
     * @return A list containing the types of the resolved nodes.
     */
    default List<Type> resolveTypesOf(Iterable<? extends ASTNode> nodes) {
        return resolveTypesOf(nodes, true);
    }

    /**
     * Resolves the types of each of the given nodes. 
     * 
     * @param nodes The list of nodes.
     * @param allowUnitialized 
     * @return A list containing the types of the resolved nodes.
     */
    default List<Type> resolveTypesOf(Iterable<? extends ASTNode> nodes, 
            boolean allowUnitialized) {
        final List<Type> result = new ArrayList<>();
        for (final ASTNode node : nodes) {
            resolveTypeOf(node);
            if (node instanceof Typed) {
                final Type type = ((Typed) node).getType();
                if (!allowUnitialized && type.isVariable() 
                        && type.asVariable().isIntermediate()) {
                    reportError(node, "Uninitialized variable");
                }
                result.add(type);
                
            }
        }
        return result;
    }

    /**
     * Reports a compile error.
     * 
     * @param location The location of the error.
     * @param msg The error message.
     * @param format Format parameters for the message.
     */
    void reportError(Location location, String msg, Object... format);
}
