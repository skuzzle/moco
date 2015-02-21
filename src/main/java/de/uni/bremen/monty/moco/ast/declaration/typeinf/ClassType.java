package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.Location;
import de.uni.bremen.monty.moco.ast.Position;

public class ClassType extends Type {

    public static class ClassNamed {
        private final String name;
        private Location location;
        private final Set<ClassType> superClasses;
        private final List<Type> typeParameters;

        private ClassNamed(String name) {
            this.name = name;
            this.location = UNKNOWN_LOCATION;
            this.superClasses = new HashSet<>();
            this.typeParameters = new ArrayList<>();
        }

        public ClassNamed atLocation(Location location) {
            if (location == null) {
                throw new IllegalArgumentException("location is null");
            }
            this.location = location;
            return this;
        }

        public ClassNamed addTypeParameter(Type... var) {
            if (var == null) {
                throw new IllegalArgumentException("var is null");
            }
            this.typeParameters.addAll(Arrays.asList(var));
            return this;
        }

        public ClassNamed addTypeParameters(Collection<Type> vars) {
            if (vars == null) {
                throw new IllegalArgumentException("vars is null");
            }
            this.typeParameters.addAll(vars);
            return this;
        }

        public ClassNamed withSuperClass(ClassType superClass) {
            if (superClass == null) {
                throw new IllegalArgumentException("superClass is null");
            }
            return withSuperClasses(superClass);
        }

        public ClassNamed withSuperClasses(ClassType... classes) {
            if (classes == null) {
                throw new IllegalArgumentException("classes is null");
            }

            return withSuperClasses(Arrays.asList(classes));
        }

        public ClassNamed withSuperClasses(List<ClassType> classes) {
            if (classes == null) {
                throw new IllegalArgumentException("classes is null");
            }
            this.superClasses.addAll(classes);
            return this;
        }

        public ClassType createType() {
            return new ClassType(new Identifier(this.name), this.location.getPosition(),
                    this.superClasses, this.typeParameters);
        }
    }

    public static ClassNamed classNamed(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        return new ClassNamed(name);
    }

    public static ClassNamed classNamed(Identifier identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException("identifier is null");
        }
        return classNamed(identifier.getSymbol());
    }

    public static ClassNamed from(ClassType other) {
        if (other == null) {
            throw new IllegalArgumentException("other is null");
        }

        return classNamed(other.getName()).atLocation(other);
    }

    private final Set<ClassType> superClasses;
    private final List<Type> typeParameters;
    private final int distanceToObject;

    ClassType(Identifier name, Position positionHint,
            Set<ClassType> superClasses, List<Type> typeParameters) {
        super(name, positionHint);
        if (superClasses == null) {
            throw new IllegalArgumentException("superClasses is null");
        } else if (typeParameters == null) {
            throw new IllegalArgumentException("typeParameters is null");
        }

        this.superClasses = Collections.unmodifiableSet(superClasses);
        this.typeParameters = Collections.unmodifiableList(typeParameters);
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
    public ClassType fresh() {
        return Unification.fresh(this);
    }

    @Override
    ClassType apply(Unification unification) {
        final List<Type> newTypeParams = new ArrayList<>(this.typeParameters.size());
        for (final Type param : this.typeParameters) {
            newTypeParams.add(param.apply(unification));
        }
        final List<ClassType> newSuperClasses = new ArrayList<>(this.superClasses.size());
        for (final ClassType superClass : this.superClasses) {
            newSuperClasses.add(superClass.apply(unification));
        }
        return classNamed(getName())
                .atLocation(getPosition())
                .withSuperClasses(newSuperClasses)
                .addTypeParameters(newTypeParams)
                .createType();
    }

    public boolean isTemplate() {
        for (final Type type : this.typeParameters) {
            if (type.isVariable()) {
                return true;
            }
        }
        return false;
    }

    public Collection<ClassType> getSuperClasses() {
        return this.superClasses;
    }

    public List<Type> getTypeParameters() {
        return this.typeParameters;
    }

    @Override
    public boolean isVariable() {
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append(getName().toString());
        if (!this.typeParameters.isEmpty()) {
            b.append("<");
            final Iterator<Type> it = this.typeParameters.iterator();
            while (it.hasNext()) {
                b.append(it.next());
                if (it.hasNext()) {
                    b.append(", ");
                }
            }
            b.append(">");
        }
        final List<ClassType> superClassesCopy = new ArrayList<>(this.superClasses);
        superClassesCopy.remove(CoreClasses.objectType().getType());
        if (!superClassesCopy.isEmpty()) {
            b.append(" inherits ");
            final Iterator<ClassType> it = superClassesCopy.iterator();
            while (it.hasNext()) {
                b.append(it.next());
                if (it.hasNext()) {
                    b.append(", ");
                }
            }
        }
        return b.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getSuperClasses(), getTypeParameters());
    }

    @Override
    public boolean equals(Object obj) {
        final ClassType other;
        return obj == this || obj instanceof ClassType &&
                getName().equals((other = (ClassType) obj).getName()) &&
                getTypeParameters().equals(other.getTypeParameters()) &&
                getSuperClasses().equals(other.getSuperClasses());
    }
}
