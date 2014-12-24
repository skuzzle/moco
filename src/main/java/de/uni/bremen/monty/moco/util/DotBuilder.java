package de.uni.bremen.monty.moco.util;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Typed;

public class DotBuilder implements Closeable {

    protected final PrintStream out;
    protected int nodeIdx;
    protected final Map<ASTNode, Integer> nodes;

    public DotBuilder(PrintStream out) {
        this.out = out;
        this.nodes = new HashMap<ASTNode, Integer>();
        this.out.println("graph \"name\"");
        this.out.println("{");
        this.out.println("node [shape=Mrecord];");
        this.out.println("graph[bgcolor=white, ordering=out, concentrate=true];");
    }

    public void finish() {
        this.out.println("}");
    }

    public void printNode(ASTNode node, String... attributes) {
        Integer i = this.nodes.get(node);
        if (i != null) {
            return;
        }
        i = this.nodeIdx++;
        this.nodes.put(node, i);
        this.out.print("n");
        this.out.print(i);
        this.out.print("[shape=Mrecord, fontname=\"Consolas\", label=\"{");

        final List<String> attributeList = new ArrayList<>(Arrays.asList(attributes));
        attributeList.add(String.format("Scope: %s", node.getScope().getParentScope()));
        if (node instanceof Typed) {
            final Typed typed = (Typed) node;
            final Type unique = typed.getType();
            attributeList.add(String.format("Type: %s",
                    typed.isTypeResolved()
                            ? unique
                            : "unknown"));

            for (final Type type : typed.getTypes()) {
                attributeList.add(type.toString());
            }
        }

        for (int j = 0; j < attributeList.size(); ++j) {
            this.out.print(simpleEscape(attributeList.get(j)));
            if (j < attributeList.size() - 1) {
                this.out.print("|");
            }
        }


        this.out.println("}\"];");
    }

    public void printEdge(ASTNode start, ASTNode target, String label) {
        this.printEdge(start, target, label, "solid", true);
    }

    public void printEdge(ASTNode start, ASTNode target, String label,
            String style,
            boolean constraint) {
        final Integer startIdx = this.nodes.get(start);
        final Integer targetIdx = this.nodes.get(target);

        if (startIdx == null) {
            throw new IllegalStateException("no start for node " + start);
        } else if (targetIdx == null) {
            throw new IllegalStateException("no target for node " + target);
        }

        this.out.print("n");
        this.out.print(startIdx);
        this.out.print("--");
        this.out.print("n");
        this.out.print(targetIdx);
        this.out.print(" [fontname=\"Consolas\", headport=n,tailport=s,constraint=");
        this.out.print(constraint);
        this.out.print(",label=\"");
        this.out.print(simpleEscape(label));
        this.out.print("\", style=\"");
        this.out.print(style);
        this.out.println("\"];");
    }

    private final static char[] ESCAPES = { '?', '<', '>' };
    private final static String[] REPLACEMENTS = { "\\?", "&lt;", "&gt;" };

    private String simpleEscape(String s) {
        return replace(s, ESCAPES, REPLACEMENTS);
    }

    private static String replace(String original, char[] characters,
            String[] replacement) {
        if (characters.length != replacement.length) {
            throw new IllegalArgumentException("different array size");
        }
        final StringBuilder b = new StringBuilder(original.length() + 16);
        for (int i = 0; i < original.length(); ++i) {
            final char c = original.charAt(i);
            boolean mustAppend = true;

            for (int j = 0; j < characters.length; ++j) {
                final char c2 = characters[j];
                if (c == c2) {
                    b.append(replacement[j]);
                    mustAppend = false;
                    break;
                }
            }

            if (mustAppend) {
                b.append(c);
            }
        }
        return b.toString();
    }

    @Override
    public void close() throws IOException {
        this.out.close();
    }
}
