package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.Location;
import de.uni.bremen.monty.moco.ast.Position;

public class ClassType extends Type {

    public static class Named {
        private final String name;
        private Location location;
        private final List<ClassType> superClasses;

        private Named(String name) {
            this.name = name;
            this.location = UNKNOWN_LOCATION;
            this.superClasses = new ArrayList<>();
        }

        public Named atLocation(Location location) {
            if (location == null) {
                throw new IllegalArgumentException("location is null");
            }
            this.location = location;
            return this;
        }

        public Named withSuperClass(ClassType superClass) {
            if (superClass == null) {
                throw new IllegalArgumentException("superClass is null");
            }
            return withSuperClasses(superClass);
        }

        public Named withSuperClasses(ClassType... classes) {
            if (classes == null) {
                throw new IllegalArgumentException("classes is null");
            }

            return withSuperClasses(Arrays.asList(classes));
        }

        public Named withSuperClasses(List<ClassType> classes) {
            if (classes == null) {
                throw new IllegalArgumentException("classes is null");
            }
            this.superClasses.addAll(classes);
            return this;
        }

        public ClassType createType() {
            return new ClassType(new Identifier(this.name), this.location.getPosition(),
                    this.superClasses);
        }
    }

    public static Named named(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        return new Named(name);
    }

    public static Named named(Identifier identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException("identifier is null");
        }
        return named(identifier.getSymbol());
    }

    public static Named from(ClassType other) {
        if (other == null) {
            throw new IllegalArgumentException("other is null");
        }

        return named(other.getName()).atLocation(other);
    }

    private final List<ClassType> superClasses;
    private final int distanceToObject;

    private ClassType(Identifier name, Position positionHint,
            List<ClassType> superClasses) {
        super(name, positionHint);
        if (superClasses == null) {
            throw new IllegalArgumentException("superClasses is null");
        }

        this.superClasses = superClasses;
        this.distanceToObject = calcDistanceToObject(this);
    }

    private int calcDistanceToObject(ClassType current) {
        if (current.getName().getSymbol().equals("Object")) {
            return 0;
        } else {
            int min = Integer.MAX_VALUE;
            for (final ClassType superType : this.superClasses) {
                int distance = superType.distanceToObject() + 1;
                min = Math.min(min, distance);
            }
            return min;
        }
    }

    public int distanceToObject() {
        return this.distanceToObject;
    }

    @Override
    Type apply(Unification unification) {
        return this;
    }

    /**
     * Determines whether this type is assignment compatible with the given type
     * {@code other}. That is, if a declaration has the type {@code other} then
     * expressions with type {@code this} can be assigned to that declaration.
     *
     * @param other The target type to check against.
     * @return Whether this is type is compatible with other.
     */
    public boolean isA(Type other) {
        if (other == this) {
            return true;
        } else if (other instanceof ClassType) {
            if (getName().equals(other.getName())) {
                return true;
            }

            for (final ClassType superType : this.superClasses) {
                if (superType.isA(other)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<ClassType> getSuperClasses() {
        return this.superClasses;
    }

    @Override
    public boolean isVariable() {
        return false;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof ClassType &&
                getName().equals(((ClassType) obj).getName());
    }
}
