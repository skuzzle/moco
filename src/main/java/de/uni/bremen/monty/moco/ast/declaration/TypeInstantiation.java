package de.uni.bremen.monty.moco.ast.declaration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.uni.bremen.monty.moco.ast.AbstractTypedASTNode;
import de.uni.bremen.monty.moco.ast.Location;
import de.uni.bremen.monty.moco.ast.NamedNode;
import de.uni.bremen.monty.moco.ast.Position;
import de.uni.bremen.monty.moco.ast.ResolvableIdentifier;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Unification;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;

public class TypeInstantiation extends AbstractTypedASTNode implements NamedNode {

    public static class Builder {
        private final ResolvableIdentifier typeName;
        private final List<TypeInstantiation> typeArguments;
        private Position position;

        private Builder(ResolvableIdentifier typeName) {
            Objects.requireNonNull(typeName);
            this.typeName = typeName;
            this.typeArguments = new ArrayList<>();
            this.position = UNKNOWN_POSITION;
        }

        public Builder atLocation(Location location) {
            Objects.requireNonNull(location);
            this.position = location.getPosition();
            return this;
        }

        public Builder atPosition(Position position) {
            Objects.requireNonNull(position);
            this.position = position;
            return this;
        }

        public Builder addTypeArgument(TypeInstantiation type) {
            Objects.requireNonNull(type);
            this.typeArguments.add(type);
            return this;
        }

        public TypeInstantiation create() {
            return new TypeInstantiation(this.position, this.typeName, this.typeArguments);
        }
    }

    public static Builder forTypeName(String typeName) {
        Objects.requireNonNull(typeName);
        return new Builder(ResolvableIdentifier.of(typeName));
    }

    private final ResolvableIdentifier typeName;
    private final List<TypeInstantiation> typeArguments;
    private Unification unification = Unification.EMPTY;

    TypeInstantiation(Position position, ResolvableIdentifier typeName,
            List<TypeInstantiation> typeArguments) {
        super(position);
        this.typeName = typeName;
        this.typeArguments = Collections.unmodifiableList(typeArguments);
    }

    @Override
    public ResolvableIdentifier getIdentifier() {
        return this.typeName;
    }

    public List<TypeInstantiation> getTypeArguments() {
        return this.typeArguments;
    }

    public void setUnification(Unification unification) {
        Objects.requireNonNull(unification);
        this.unification = unification;
    }

    public Unification getUnification() {
        return this.unification;
    }

    @Override
    public void visit(BaseVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void visitChildren(BaseVisitor visitor) {
        for (final TypeInstantiation child : this.typeArguments) {
            visitor.visitDoubleDispatched(child);
        }
    }
}
