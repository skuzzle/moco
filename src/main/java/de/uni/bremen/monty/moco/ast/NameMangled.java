package de.uni.bremen.monty.moco.ast;

/**
 * Type for nodes that have a mangled name attached.
 * 
 * @author Simon Taddiken
 */
public interface NameMangled {
    /**
     * Get the mangled Identifier.
     *
     * @return mangled Identifier
     */
    public Identifier getMangledIdentifier();
}
