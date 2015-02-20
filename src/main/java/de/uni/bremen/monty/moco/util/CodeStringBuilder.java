package de.uni.bremen.monty.moco.util;

public class CodeStringBuilder {

    private final StringBuilder builder;
    private int indent;
    private final int indentSize;
    private final char indentChar;

    public CodeStringBuilder() {
        this.builder = new StringBuilder();
        this.indentSize = 4;
        this.indentChar = ' ';
    }

    public CodeStringBuilder indent() {
        this.indent++;
        return this;
    }

    public CodeStringBuilder dedent() {
        this.indent--;
        return blankLine();
    }

    public CodeStringBuilder blankLine() {
        return append("");
    }

    public CodeStringBuilder append(String line) {
        appendIndentation();
        this.builder.append(line).append(System.lineSeparator());
        return this;
    }

    private void appendIndentation() {
        for (int i = 0; i < this.indent; ++i) {
            for (int j = 0; j < this.indentSize; ++j) {
                this.builder.append(this.indentChar);
            }
        }
    }

    @Override
    public String toString() {
        return this.builder.toString();
    }
}
