package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import de.uni.bremen.monty.moco.ast.declaration.TypeDeclaration;


/**
 * Interface for AST nodes that have a type assigned to them. Typed nodes may
 * have a set of multiple possible types and a unique resolved type.
 *
 * @author Simon Taddiken
 */
public interface Typed {

    /**
     * Returns whether an unique type has been resolved for this object.
     *
     * @return Whether an unique type has been resolved for this object.
     */
    boolean isTypeResolved();

    /**
     * Gets the unique type of this node. Will return <code>null</code> until
     * the type has been resolved and set via {@link #setType(Type)}
     *
     * @return The unique type.
     */
    Type getType();

    /**
     * Sets the unique type for this node.
     *
     * @param type The unique type.
     */
    void setType(Type type);

    /**
     * Sets the declaration which belongs to the resolved type.
     *
     * @param typeDecl The type's declaration.
     */
    void setTypeDeclaration(TypeDeclaration typeDecl);

    /**
     * Gets the declaration of the resolved type.
     *
     * @return The resolved type's declaration.
     */
    TypeDeclaration getTypeDeclaration();

    /**
     * Whether this node has a type declaration assigned.
     *
     * @return Whether this node has a type declaration assigned.
     */
    boolean isTypeDeclarationResolved();
}
