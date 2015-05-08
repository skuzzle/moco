package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.Location;
import de.uni.bremen.monty.moco.ast.Position;
import de.uni.bremen.monty.moco.ast.Scope;

public class Function extends Type {

    // Builder classes

    public static class FunctionReturning {
        private final String name;
        private final Location location;
        private final Type returnType;
        private final List<Type> parameters;
        private final List<Type> quantification;

        private FunctionReturning(String name, Location location, Type returnType) {
            super();
            this.name = name;
            this.location = location;
            this.returnType = returnType;
            this.parameters = new ArrayList<>();
            this.quantification = new ArrayList<>();
        }

        public FunctionReturning quantifiedBy(Type... types) {
            if (types == null) {
                throw new IllegalArgumentException("types is null");
            }
            return quantifiedBy(Arrays.asList(types));
        }

        public FunctionReturning quantifiedBy(List<Type> types) {
            if (types == null) {
                throw new IllegalArgumentException("types is null");
            }
            this.quantification.addAll(types);
            return this;
        }

        public FunctionReturning andParameter(Type type) {
            return andParameters(type);
        }

        public FunctionReturning andParameters(Product product) {
            if (product == null) {
                throw new IllegalArgumentException("product is null");
            } else if (!this.parameters.isEmpty()) {
                throw new IllegalArgumentException("other types than product present");
            }
            return andParameters(product.getComponents());
        }

        public FunctionReturning andParameters(Type... types) {
            if (types == null) {
                throw new IllegalArgumentException("types is null");
            }

            return andParameters(Arrays.asList(types));
        }

        public FunctionReturning andParameters(List<Type> types) {
            if (types == null) {
                throw new IllegalArgumentException("types is null");
            }
            for (final Type type : types) {
                if (type instanceof Product) {
                    throw new IllegalArgumentException("can not add nested product type");
                }
            }
            this.parameters.addAll(types);
            return this;
        }

        public Function createType() {
            final Identifier id = new Identifier(this.name);
            return new Function(this.location.getPosition(), id,
                    this.returnType, this.parameters, this.quantification);
        }
    }

    public static class FunctionNamed {
        private final String name;
        private Location location;

        private FunctionNamed(String name) {
            this.name = name;
            this.location = UNKNOWN_LOCATION;
        }

        public FunctionNamed atLocation(Location location) {
            if (location == null) {
                throw new IllegalArgumentException("location is null");
            }

            this.location = location;
            return this;
        }

        public FunctionReturning returning(Type returnType) {
            if (returnType == null) {
                throw new IllegalArgumentException("returnType is null");
            }
            return new FunctionReturning(this.name, this.location, returnType);
        }

        public FunctionReturning returningVoid() {
            return returning(CoreClasses.voidType().getType());
        }
    }

    public static FunctionNamed anonymous() {
        return named("<anonymous>");
    }

    public static FunctionNamed named(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }

        return new FunctionNamed(name);
    }

    public static FunctionNamed named(Identifier identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException("identifier is null");
        }
        return new FunctionNamed(identifier.getSymbol());
    }

    public static FunctionNamed from(Function function) {
        return named(function.getName())
                .atLocation(function);
    }

    private final Type returnType;
    private final Product parameterTypes;
    private final List<Type> quantification;

    private Function(Position position, Identifier identifier,
            Type returnType, List<Type> parameterTypes, List<Type> quantification) {
        super(identifier, position);
        this.returnType = returnType;
        this.parameterTypes = Product.of(parameterTypes).createType();
        this.quantification = Collections.unmodifiableList(quantification);
    }

    public Type getReturnType() {
        return this.returnType;
    }

    public Product getParameters() {
        return this.parameterTypes;
    }

    public List<Type> getParameterTypes() {
        return this.parameterTypes.getComponents();
    }

    public List<Type> getQuantification() {
        return this.quantification;
    }

    @Override
    public Function fresh(Scope scope) {
        return Unification.fresh(this, scope);
    }

    @Override
    Function apply(Unification unification) {
        final Type newReturnType = this.returnType.apply(unification);
        final Product newParameters = this.parameterTypes.apply(unification);
        final List<Type> qunatification = new ArrayList<>(this.quantification.size());
        for (final Type var : this.quantification) {
            // add all type variables, that have no substitute
            qunatification.add(var.apply(unification));
        }
        return Function
                .from(this)
                .returning(newReturnType)
                .quantifiedBy(qunatification)
                .andParameters(newParameters)
                .createType();
    }

    @Override
    public boolean isA(Type other) {
        if (this == other) {
            return true;
        } else if (!other.isFunction()) {
            return false;
        }
        final Function fun = other.asFunction();
        return getParameters().isA(fun.getParameters()) &&
                getReturnType().isA(fun.getReturnType());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Function)) {
            return false;
        }
        final Function other = (Function) obj;
        return this.returnType.equals(other.returnType) &&
                this.parameterTypes.equals(other.parameterTypes);
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append(getName().getSymbol());
        final Iterator<Type> qunatIt = getQuantification().iterator();
        if (qunatIt.hasNext()) {
            b.append("<");
            while (qunatIt.hasNext()) {
                b.append(qunatIt.next().toString());
                if (qunatIt.hasNext()) {
                    b.append(", ");
                }
            }
            b.append(">");
        }
        final Iterator<Type> it = getParameterTypes().iterator();
        if (!it.hasNext()) {
            b.append("()");
        }
        while (it.hasNext()) {
            b.append(it.next());
            if (it.hasNext()) {
                b.append(" x ");
            }
        }
        b.append(" -> ");
        b.append(this.returnType);
        return b.toString();
    }
}
