package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.Location;
import de.uni.bremen.monty.moco.ast.Position;
import de.uni.bremen.monty.moco.ast.declaration.FunctionDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;

public class Function extends Type {

    // Builder classes

    public static class Returning {
        private final String name;
        private final Location location;
        private final Type returnType;
        private final List<Type> parameters;
        private final List<TypeVariable> quantification;

        private Returning(String name, Location location, Type returnType) {
            super();
            this.name = name;
            this.location = location;
            this.returnType = returnType;
            this.parameters = new ArrayList<>();
            this.quantification = new ArrayList<>();
        }

        public Returning quantifiedBy(TypeVariable... types) {
            if (types == null) {
                throw new IllegalArgumentException("types is null");
            }
            return quantifiedBy(Arrays.asList(types));
        }

        public Returning quantifiedBy(List<TypeVariable> types) {
            if (types == null) {
                throw new IllegalArgumentException("types is null");
            }
            this.quantification.addAll(types);
            return this;
        }

        public Returning andParameter(Type type) {
            return andParameters(type);
        }

        public Returning andParameters(Product product) {
            if (product == null) {
                throw new IllegalArgumentException("product is null");
            } else if (!this.parameters.isEmpty()) {
                throw new IllegalArgumentException("other types than product present");
            }
            return andParameters(product.getComponents());
        }

        public Returning andParameters(Type... types) {
            if (types == null) {
                throw new IllegalArgumentException("types is null");
            }

            return andParameters(Arrays.asList(types));
        }

        public Returning andParameters(List<Type> types) {
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

    public static class Named {
        private final String name;
        private Location location;

        private Named(String name) {
            this.name = name;
            this.location = UNKNOWN_LOCATION;
        }

        public Named atLocation(Location location) {
            if (location == null) {
                throw new IllegalArgumentException("location is null");
            }

            this.location = location;
            return this;
        }

        public Returning returning(Type returnType) {
            if (returnType == null) {
                throw new IllegalArgumentException("returnType is null");
            }
            return new Returning(this.name, this.location, returnType);
        }

        public Returning returningVoid() {
            return returning(CoreTypes.VOID);
        }
    }

    public static Named anonymous() {
        return named("<anonymous>");
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
        return new Named(identifier.getSymbol());
    }

    public static Named from(Function function) {
        return named(function.getName())
                .atLocation(function);
    }

    /**
     * Converts the given {@link ProcedureDeclaration} or
     * {@link FunctionDeclaration} into a {@link Function}.
     *
     * @param procedure The declaration to convert.
     * @return The resulting type.
     */
    public static Function from(ProcedureDeclaration procedure) {
        final List<Type> parameters = new ArrayList<>(procedure.getParameter().size());
        for (final VariableDeclaration decl : procedure.getParameter()) {
            parameters.add(decl.getType());
        }
        final Type returnType = procedure instanceof FunctionDeclaration
                ? ((FunctionDeclaration) procedure).getReturnType()
                : CoreTypes.VOID;
        return named(procedure.getIdentifier())
                .atLocation(procedure)
                .returning(returnType)
                .andParameters(parameters)
                .createType();
    }

    private final Type returnType;
    private final Product parameterTypes;
    private final List<TypeVariable> quantification;

    private Function(Position position, Identifier identifier,
            Type returnType, List<Type> parameterTypes, List<TypeVariable> quantification) {
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

    public List<TypeVariable> getQuantification() {
        return this.quantification;
    }

    @Override
    Function apply(Unification unification) {
        final Type newReturnType = this.returnType.apply(unification);
        final Product newParameters = this.parameterTypes.apply(unification);
        return Function
                .from(this)
                .returning(newReturnType)
                .andParameters(newParameters)
                .createType();
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
        b.append(getName().getSymbol()).append(": ");
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
