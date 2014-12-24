package de.uni.bremen.monty.moco.ast;

/**
 * Represents an Object which has a location within a source file.
 *
 * @author Simon Taddiken
 */
public interface Location {

    /** Represents an unknown source position. Useful for artificial AST nodes */
    public static final Position UNKNOWN_POSITION = new Position("unknown", 0, 0);

    /** Represents an unknown source position. Useful for artificial AST nodes */
    public static final Location UNKNOWN_LOCATION = new Location() {

        @Override
        public Position getPosition() {
            return UNKNOWN_POSITION;
        }
    };

    /**
     * Gets the position of this entity.
     *
     * @return The position.
     */
    public Position getPosition();
}
