package de.uni.bremen.monty.moco.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public final class DebugUtil {

    private DebugUtil() {}

    public static String getCurrentLocation() {
        final Exception e = new Exception();
        final Writer target = new StringWriter();
        try (Writer w = target;
                final PrintWriter pw = new PrintWriter(w)) {
            e.printStackTrace(pw);
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
        return target.toString();
    }
}
