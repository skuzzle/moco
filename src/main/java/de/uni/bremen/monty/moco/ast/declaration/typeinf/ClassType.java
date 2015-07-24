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
            Objects.requireNonNull(location);
            this.location = location;
            return this;
        }

        public ClassNamed addTypeParameter(Type... var) {
            Objects.requireNonNull(var);
            this.typeParameters.addAll(Arrays.asList(var));
            return this;
        }

        public ClassNamed addTypeParameters(Collection<Type> vars) {
            Objects.requireNonNull(vars);
            this.typeParameters.addAll(vars);
            return this;
        }

        public ClassNamed withSuperClass(ClassType superClass) {
            Objects.requireNonNull(superClass);
            return withSuperClasses(superClass);
        }

        public ClassNamed withSuperClasses(ClassType... classes) {
            Objects.requireNonNull(classes);
            return withSuperClasses(Arrays.asList(classes));
        }

        public ClassNamed withSuperClasses(List<ClassType> classes) {
            Objects.requireNonNull(classes);
            this.superClasses.addAll(classes);
            return this;
        }

        public ClassType createType() {
            return new ClassType(new Identifier(this.name), this.location.getPosition(),
                    this.superClasses, this.typeParameters);
        }
    }

    public static ClassNamed classNamed(String name) {
        Objects.requireNonNull(name);
        return new ClassNamed(name);
    }

    public static ClassNamed classNamed(Identifier identifier) {
        Objects.requireNonNull(identifier);
        return classNamed(identifier.getSymbol());
    }

    public static ClassNamed from(ClassType other) {
        Objects.requireNonNull(other);
        return classNamed(other.getName()).atLocation(other);
    }

    private final Set<ClassType> superClasses;
    private final List<Type> typeParameters;
    private final int distanceToObject;

    ClassType(Identifier name, Position positionHint,
            Set<ClassType> superClasses, List<Type> typeParameters) {
        super(name, positionHint);
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

    @Override
    public int distanceToObject() {
        return this.distanceToObject;
    }

    @Override
    ClassType apply(Unification unification) {
        if (this.typeParameters.isEmpty() && this.superClasses.isEmpty()) {
            // this cant be a ploy type, so avoid copying
            return this;
        }

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
    public boolean isA(Type other) {
        if (this == other) {
            return true;
        } else if (!other.isClass()) {
            return false;
        }

        final ClassType ct = other.asClass();
        if (getName().equals(ct.getName())) {
            return true;
        }
        for (final ClassType superType : this.superClasses) {
            if (superType.isA(other)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append(getName().toString());
        appendQuantification(b);
        /*if (!this.superClasses.isEmpty()) {
            b.append(" : ");
            final Iterator<ClassType> it = this.superClasses.iterator();
            while (it.hasNext()) {
                final ClassType superType = it.next();
                b.append(superType.getName());
                superType.appendQuantification(b);
                if (it.hasNext()) {
                    b.append(", ");
                }
            }
        }*/
        return b.toString();
    }

    private void appendQuantification(StringBuilder b) {
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
