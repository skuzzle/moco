package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.uni.bremen.monty.moco.ast.CoreTypes;

final class Unifier {

    private final Map<Type, Integer> typeToClass;
    private final Map<Integer, Type> classToType;
    private int classes;
    private final TypeContext context;

    Unifier(TypeContext context) {
        this.typeToClass = new HashMap<>();
        this.classToType = new HashMap<>();
        this.classes = 0;
        this.context = context;
    }

    private int getEquivalenceClass(Type t) {
        Integer ec = this.typeToClass.get(t);
        if (ec == null) {
            ec = this.classes++;
            this.typeToClass.put(t, ec);
        }
        return ec;
    }

    private Type find(Type s) {
        final int cls = getEquivalenceClass(s);
        Type representative = this.classToType.get(cls);
        if (representative == null) {
            representative = s;
            this.classToType.put(cls, representative);
        }
        return representative;
    }

    private void union(Type m, Type n,
            Map<TypeVariable, Type> subst) {

        final Type rep_m = find(m);
        final Type rep_n = find(n);

        final int equiv = getEquivalenceClass(m);

        final Type representative = chooseRepresentative(rep_m, rep_n);
        final Type other = representative == rep_n
                ? rep_m
                : rep_n;
        makeEquivalent(equiv, representative, other);

        if (other instanceof TypeVariable) {
            subst.put((TypeVariable) other, representative);
        }
    }

    private Type chooseRepresentative(Type rep_m, Type rep_n) {
        // preferably choose type which is *not* a type var, or, if both are a
        // type var,
        // preferably choose the one which is not an 'inferred' variable
        if (rep_m.isVariable() && rep_n.isVariable()) {
            return rep_m.getName().isTypeVariableIdentifier()
                    ? rep_n
                    : rep_m;
        } else if (rep_m.isVariable()) {
            return rep_n;
        } else {
            return rep_m;
        }
    }

    private void makeEquivalent(int equivClass, Type representative,
            Type other) {

        this.classToType.put(equivClass, representative);
        this.typeToClass.put(representative, equivClass);
        this.typeToClass.put(other, equivClass);
    }

    public Unification unify(Type first, Type second) {
        final Map<TypeVariable, Type> subst = new HashMap<>();
        final boolean success = unifyInternal(first, second, subst);
        return success
                ? Unification.successful(subst)
                : Unification.failed();
    }

    private boolean unifyInternal(Type m, Type n, Map<TypeVariable, Type> subst) {
        final Type s = find(m);
        final Type t = find(n);

        // checking if s isA t
        if (s == t || t == CoreTypes.TOP || s == CoreTypes.BOT) {
            // everything isA TOP, BOT isA everything
            // this also covers the case that both types are VOID
            return true;
        } else if (s instanceof MonoType && t instanceof MonoType) {
            return s.isA(t);
        } else if (s instanceof ClassType && t instanceof ClassType) {
            final ClassType cts = s.asClass();
            final ClassType ctt = t.asClass();

            return isA(cts, ctt, subst);
        } else if (s instanceof Function && t instanceof Function) {
            union(s, t, subst);
            final Function fs = s.asFunction();
            final Function ft = t.asFunction();

            if (!unifyInternal(fs.getReturnType(), ft.getReturnType(), subst)) {
                return false;
            }

            if (!unifyInternal(fs.getParameters(), ft.getParameters(), subst)) {
                return false;
            }

            return true;
        } else if (s instanceof Product && t instanceof Product) {
            union(s, t, subst);
            final Product ps = s.asProduct();
            final Product pt = t.asProduct();

            if (ps.getComponents().size() != pt.getComponents().size()) {
                return false;
            }

            final Iterator<Type> pIt = pt.getComponents().iterator();
            for (final Type ts : ps.getComponents()) {
                if (!unifyInternal(ts, pIt.next(), subst)) {
                    return false;
                }
            }
            return true;
        } else if (canUnifyVariables(s, t)) {
            union(s, t, subst);
            return true;
        } else {
            return false;
        }
    }

    private boolean canUnifyVariables(Type s, Type t) {
        if (s.isVariable()) {
            return !this.context.isFree(s.asVariable());
        } else if (t.isVariable()) {
            return !this.context.isFree(t.asVariable());
        }
        return false;
    }

    /**
     * Defines the subtyping relation between two {@link ClassType ClassTypes}.
     *
     * @param is The first type.
     * @param a The type to check whether the first is an instance of.
     * @param subst Substitution map.
     * @return Whether {@code is} is an instance of {@code a}.
     */
    private boolean isA(ClassType is, ClassType a, Map<TypeVariable, Type> subst) {
        if (is == a) {
            return true;
        } else if (is.getName().equals(a.getName())) {

            assert is.getTypeParameters().size() == a.getTypeParameters().size();

            final Iterator<Type> otherIt = a.getTypeParameters().iterator();
            for (final Type typeParam : is.getTypeParameters()) {
                if (!unifyInternal(typeParam, otherIt.next(), subst)) {
                    return false;
                }
            }
            return true;
        }

        boolean result = false;
        for (final ClassType superType : is.getSuperClasses()) {
            result |= isA(superType, a, subst);
        }
        return result;
    }
}
