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

    private void union(Type m, Type n) {

        final Type rep_m = find(m);
        final Type rep_n = find(n);

        final int equiv = getEquivalenceClass(m);

        final Type representative = chooseRepresentative(rep_m, rep_n);
        final Type other = representative == rep_n
                ? rep_m
                : rep_n;
        makeEquivalent(equiv, representative, other);
    }

    private Type chooseRepresentative(Type rep_m, Type rep_n) {
        if (rep_m.isVariable() && rep_n.isVariable()) {
            return chooseFree(rep_m.asVariable(), rep_n.asVariable());
        } else if (rep_m.isVariable()) {
            return rep_n;
        } else if (rep_n.isVariable()) {
            return rep_m;
        }
        return rep_m;
    }

    private Type chooseFree(TypeVariable m, TypeVariable n) {
        if (this.context.isFree(m)) {
            return m;
        }
        if (this.context.isFree(n)) {
            return n;
        }

        // none is free, no matter which becomes representative
        // XXX: there is actually something magic with returning n which makes
        // the test work. needs to be observed
        return n;
    }

    private void makeEquivalent(int equivClass, Type representative,
            Type other) {

        this.classToType.put(equivClass, representative);
        this.typeToClass.put(representative, equivClass);
        this.typeToClass.put(other, equivClass);
    }

    public Unification unify(Type first, Type second) {
        final boolean success = unifyInternal(first, second, true);
        if (success) {
            final Map<TypeVariable, Type> subst = buildSubstitutions();
            return Unification.successful(subst);
        }
        return Unification.failed();
    }

    private Map<TypeVariable, Type> buildSubstitutions() {
        final Map<TypeVariable, Type> result = new HashMap<>();
        this.typeToClass.forEach((type, cls) -> {
            if (type.isVariable()) {
                final Type representative = this.classToType.get(cls);

                // no need to add identity mapping
                if (representative != type) {
                    result.put(type.asVariable(), representative);
                }
            }
        });
        return result;
    }

    private boolean unifyInternal(Type m, Type n, boolean allowSubtyping) {
        final Type s = find(m);
        final Type t = find(n);

        // checking if s isA t
        if (s == t || t == CoreTypes.TOP || s == CoreTypes.BOT) {
            // everything isA TOP, BOT isA everything
            // this also covers the case that both types are VOID
            return true;
        } else if (s instanceof ClassType && t instanceof ClassType) {
            final ClassType cts = s.asClass();
            final ClassType ctt = t.asClass();

            return isA(cts, ctt, allowSubtyping);
        } else if (s instanceof Function && t instanceof Function) {
            union(s, t);
            final Function fs = s.asFunction();
            final Function ft = t.asFunction();

            if (!unifyInternal(fs.getReturnType(), ft.getReturnType(), allowSubtyping)) {
                return false;
            }

            if (!unifyInternal(fs.getParameters(), ft.getParameters(), allowSubtyping)) {
                return false;
            }

            return true;
        } else if (s instanceof Product && t instanceof Product) {
            union(s, t);
            final Product ps = s.asProduct();
            final Product pt = t.asProduct();

            if (ps.getComponents().size() != pt.getComponents().size()) {
                return false;
            }

            final Iterator<Type> pIt = pt.getComponents().iterator();
            for (final Type ts : ps.getComponents()) {
                if (!unifyInternal(ts, pIt.next(), allowSubtyping)) {
                    return false;
                }
            }
            return true;
        } else if (canUnifyVariables(s, t)) {
            union(s, t);
            return true;
        } else {
            return false;
        }
    }

    private boolean canUnifyVariables(Type s, Type t) {
        if (s.isVariable() && t.isVariable()) {
            return !this.context.isFree(s.asVariable()) ||
                !this.context.isFree(t.asVariable());
        } else if (s.isVariable()) {
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
     * @return Whether {@code is} is an instance of {@code a}.
     */
    private boolean isA(ClassType is, ClassType a, boolean allowSubtyping) {
        if (is == a) {
            return true;
        } else if (is.getName().equals(a.getName())) {

            assert is.getTypeParameters().size() == a.getTypeParameters().size();

            final Iterator<Type> otherIt = a.getTypeParameters().iterator();
            for (final Type typeParam : is.getTypeParameters()) {
                if (!unifyInternal(typeParam, otherIt.next(), false)) {
                    return false;
                }
            }
            return true;
        }

        if (allowSubtyping) {
            for (final ClassType superType : is.getSuperClasses()) {
                if (isA(superType, a, allowSubtyping)) {
                    return true;
                }
            }
        }
        return false;
    }
}
