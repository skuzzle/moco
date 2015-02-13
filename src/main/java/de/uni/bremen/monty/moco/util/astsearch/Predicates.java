package de.uni.bremen.monty.moco.util.astsearch;

import java.util.function.Predicate;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.NamedNode;
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

    public static <C extends ASTNode> Predicate<C> isExact(Class<? extends ASTNode> type) {
        return c -> c.getClass() == type;
    }
}
