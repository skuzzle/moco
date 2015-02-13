package de.uni.bremen.monty.moco.ast.declaration.typeinf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

final class Unifier {

    private final Map<Type, Integer> typeToClass;
    private final Map<Integer, Type> classToType;
    private int classes;

    Unifier() {
        this.typeToClass = new HashMap<>();
        this.classToType = new HashMap<>();
        this.classes = 0;
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
        // preferably choose type which is *not* a type var
        final Type representative = rep_m.isVariable()
                ? rep_n
                : rep_m;
        final Type other = representative == rep_n
                ? rep_m
                : rep_n;
        makeEquivalent(equiv, representative, other);

        if (other instanceof TypeVariable) {
            subst.put((TypeVariable) other, representative);
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

        if (s instanceof ClassType && t instanceof ClassType) {
            final ClassType cts = (ClassType) s;
            final ClassType ctt = (ClassType) t;

            return cts.isA(ctt);
        } else if (s == CoreTypes.VOID && t == CoreTypes.VOID) {
            return true;
        } else if (s instanceof Function && t instanceof Function) {
            union(s, t, subst);
            final Function fs = (Function) s;
            final Function ft = (Function) t;

            if (fs.getParameterTypes().size() != ft.getParameterTypes().size()) {
                return false;
            }

            if (!unifyInternal(fs.getReturnType(), ft.getReturnType(), subst)) {
                return false;
            }

            final Iterator<Type> tIt = ft.getParameterTypes().iterator();
            for (final Type param : fs.getParameterTypes()) {
                if (!unifyInternal(param, tIt.next(), subst)) {
                    return false;
                }
            }
            return true;
        } else if (s instanceof Product && t instanceof Product) {
            union(s, t, subst);
            final Product ps = (Product) s;
            final Product pt = (Product) t;

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
        } else if (s instanceof TypeVariable || t instanceof TypeVariable) {
            union(s, t, subst);
            return true;
        } else {
            return false;
        }
    }

}
