package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import java.util.List;

public interface Typed {

    public interface TypeContextBuilder {
        /**
         * Adds a constraint to the type which has been added by the before-hand
         * call to {@link Typed#addType(Type)}.
         *
         * @param unification The unification which constrains type variables to
         *            certain substitutions.
         * @return Builder object for chaining the calls.
         */
        TypeContextBuilder withConstraint(Unification unification);

        /**
         * Adds a scope constraint to the type which has been added by the
         * before-hand call to {@link Typed#addType(Type)}. The constraint
         * denotes that the type is only valid in this scope.
         *
         * @param type The scope constraint.
         * @return Builder object for chaining the calls.
         */
        TypeContextBuilder qualifiedBy(Type type);
    }

    public interface TypeContext {

        Type getType();

        boolean hasConstraint();

        Unification getConstraint();

        boolean isQualified();

        Type getQualification();
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
     * Gets the context of the given type. The type <b>must</b> be contained
     * within the collection of possible types of this entity, otherwise an
     * exception will be thrown.
     *
     * @param type The type to get the context for.
     * @return The type's context.
     */
    TypeContext getContextFor(Type type);

    /**
     * Adds a shallow copy of the given type context to the types of this node.
     *
     * @param typeContext The type context to add.
     */
    void addTypeContext(TypeContext typeContext);

    /**
     * Adds a new possible type for this node.
     *
     * @param type The type.
     * @return Builder to optionally add a constraint on the given type to this
     *         expression.
     */
    TypeContextBuilder addType(Type type);

    /**
     * Adds a new possible type for this node. The type to add is the
     * {@link #getType() unique type} if the given typed node.
     *
     * @param typed The typed instance to retrieve the type to add from.
     * @return Builder to optionally add a constraint on the given type to this
     *         expression.
     */
    TypeContextBuilder addTypeOf(Typed typed);

    /**
     * Returns a read-only view to the set of possible types associated with
     * this expression. Further types can be added with {@link #addType(Type)}.
     *
     * @return The associated types.
     */
    List<TypeContext> getTypes();
}
