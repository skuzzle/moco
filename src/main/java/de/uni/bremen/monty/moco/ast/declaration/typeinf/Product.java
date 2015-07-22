package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.Location;
import de.uni.bremen.monty.moco.ast.Position;

public class Product extends Type {

    public static final class ProductBuilder {
        private Location location;
        private final List<Type> components;

        private ProductBuilder(Collection<? extends Type> components) {
            this.location = Location.UNKNOWN_LOCATION;
            this.components = new ArrayList<>(components);
        }

        public ProductBuilder and(Type... types) {
            if (types == null) {
                throw new IllegalArgumentException("types is null");
            }
            this.components.addAll(Arrays.asList(types));
            return this;
        }

        public ProductBuilder and(Collection<? extends Type> types) {
            if (types == null) {
                throw new IllegalArgumentException("types is null");
            }
            this.components.addAll(types);
            return this;
        }

        public ProductBuilder atLocation(Location location) {
            if (location == null) {
                throw new IllegalArgumentException("location is null");
            }
            this.location = location;
            return this;
        }

        public Product createType() {
            return new Product(new Identifier(""), this.location.getPosition(),
                    this.components);
        }
    }

    public static ProductBuilder of(Collection<? extends Type> types) {
        if (types == null) {
            throw new IllegalArgumentException("types is null");
        }
        return new ProductBuilder(types);
    }

    private final List<Type> components;

    private Product(Identifier name, Position positionHint, List<Type> components) {
        super(name, positionHint);
        this.components = Collections.unmodifiableList(components);
    }

    @Override
    public int distanceToObject() {
        throw new UnsupportedOperationException("distanceToObject on Product type");
    }

    public final List<Type> getComponents() {
        return this.components;
    }

    @Override
    Product apply(Unification unification) {
        final List<Type> resultComponents = new ArrayList<>(this.components.size());
        for (final Type type : this.components) {
            resultComponents.add(type.apply(unification));
        }
        return of(resultComponents).atLocation(this).createType();
    }

    @Override
    public boolean isA(Type other) {
        if (other == this) {
            return true;
        } else if (!other.isProduct()) {
            return false;
        }
        final Product prod = other.asProduct();
        final Iterator<Type> otherIt = prod.components.iterator();
        for (final Type type : this.components) {
            if (!type.isA(otherIt.next())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof Product)) {
            return false;
        }
        final Product other = (Product) obj;
        return this.components.equals(other.components);
    }

}
