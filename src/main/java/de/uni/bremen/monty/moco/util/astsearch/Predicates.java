package de.uni.bremen.monty.moco.util.astsearch;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.NamedNode;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.VariableDeclaration.DeclarationType;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Typed;
import de.uni.bremen.monty.moco.util.ASTUtil;

public class Predicates {

    public static <C extends ASTNode> Predicate<C> is(Object obj) {
        return node -> node != null && node.equals(obj);
    }

    public static <C extends VariableDeclaration> Predicate<C> declarationTypeIs(
            DeclarationType type) {
        return c -> c.getDeclarationType() == type;
    }

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
        return c -> Arrays.asList(types).equals(c.getParameter()
                .stream()
                .map(Typed::getType)
                .collect(Collectors.toList()));
    }

}
