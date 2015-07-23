package de.uni.bremen.monty.moco.util.astsearch;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.NamedNode;
import de.uni.bremen.monty.moco.ast.declaration.ProcedureDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Typed;

public class Predicates {

    public static <C extends ASTNode> Predicate<C> is(Object obj) {
        return node -> node != null && node.equals(obj);
    }

    public static <C extends ASTNode> Predicate<C> onLine(int line) {
        return c -> c.getPosition().getLineNumber() == line;
    }

    public static <C extends NamedNode> Predicate<C> hasName(String name) {
        return c -> c.getIdentifier().getSymbol().equals(name);
    }

    public static <C extends ProcedureDeclaration> Predicate<C> hasParameters(
            Type... types) {
        return c -> Arrays.asList(types).equals(c.getParameter()
                .stream()
                .map(Typed::getType)
                .collect(Collectors.toList()));
    }

}
