package de.uni.bremen.monty.moco.util.astsearch;

import java.util.Arrays;
import java.util.function.Predicate;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.NamedNode;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Function;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.util.ASTUtil;

public class Predicates {

    public static <C extends ASTNode> Predicate<C> onLine(int line) {
        return c -> c.getPosition().getLineNumber() == line;
    }

    public static <C extends ASTNode> Predicate<C> inFile(String fileName) {
        return c -> fileName == null
                ? c.getPosition().getFileName() == null
                : c.getPosition().getFileName().equalsIgnoreCase(fileName);
    }

    public static <C extends ASTNode> Predicate<C> hasParent(
            Class<? extends ASTNode> parentType) {
        return c -> ASTUtil.findAncestor(c, parentType) != null;
    }

    public static <C extends NamedNode> Predicate<C> hasName(String name) {
        return c -> c.getIdentifier().getSymbol().equals(name);
    }

    public static <C extends NamedNode> Predicate<C> hasName(Identifier name) {
        return c -> c.getIdentifier().equals(name);
    }

    public static <C extends ASTNode> Predicate<C> isExact(Class<? extends ASTNode> type) {
        return c -> c.getClass() == type;
    }

    public static <C extends ProcedureDeclaration> Predicate<C> hasParameters(
            Type... types) {
        return c -> Function.from(c).getParameterTypes().equals(Arrays.asList(types));
    }
}
