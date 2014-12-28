package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import java.util.List;

public interface Typed {

    public interface AddTypeBuilder {
        /**
         * Adds a constraint to the type which has been added by the before-hand
         * call to {@link Typed#addType(Type)}.
         *
         * @param unification The unification which constrains type variables to
         *            certain substitutions.
         */
        void withConstraint(Unification unification);
    }

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
     * Adds a new possible type for this node.
     *
     * @param type The type.
     * @return Builder to optionally add a constraint on the given type to this
     *         expression.
     */
    AddTypeBuilder addType(Type type);

    /**
     * Returns a read-only view to the set of possible types associated with
     * this expression. Further types can be added with {@link #addType(Type)}.
     *
     * @return The associated types.
     */
    List<Type> getTypes();
}
