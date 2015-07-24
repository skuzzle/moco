package de.uni.bremen.monty.moco.visitor;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.uni.bremen.monty.moco.exception.InvalidControlFlowException;
import de.uni.bremen.monty.moco.util.CompileRule;
import de.uni.bremen.monty.moco.util.Monty;

public class ControlFlowTest {

    @Rule
    public final CompileRule compiler = new CompileRule();
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @Test
    @Monty(
    "String foo(Bool cond):\n" +
    "    if cond:\n" +
    "        return \"a\""
    )
    public void testMissingElseBranch() throws Exception {
        exception.expect(InvalidControlFlowException.class);
        compiler.compile();
    }
    
    @Test
    @Monty(
    "String foo(Bool cond):\n" +
    "    if cond:\n" +
    "        pass\n" +
    "    else:\n" +
    "        return \"a\""
    )
    public void testMissingIfBranch() throws Exception {
        exception.expect(InvalidControlFlowException.class);
        compiler.compile();
    }
    
    @Test
    @Monty(
    "String foo(Bool cond):\n" +
    "    if cond:\n" +
    "        if not cond:\n" +
    "            return \"c\"\n" +
    "        else:\n" +
    "            pass\n"+
    "    else:\n" +
    "        return \"a\""
    )
    public void testMissingInNestedElseStatements() throws Exception {
        exception.expect(InvalidControlFlowException.class);
        compiler.compile();
    }
    
    @Test
    @Monty(
    "String foo(Bool cond):\n" +
    "    if cond:\n" +
    "        if not cond:\n" +
    "            pass\n"+
    "        else:\n" +
    "            return \"c\"\n" +
    "    else:\n" +
    "        return \"a\""
    )
    public void testMissingInNestedIfStatements() throws Exception {
        exception.expect(InvalidControlFlowException.class);
        compiler.compile();
    }
    
    @Test
    @Monty(
    "String foo(Bool cond):\n" +
    "    if cond:\n" +
    "        return \"a\"\n" +
    "    else:\n" +
    "        if not cond:\n" +
    "            pass\n"+
    "        else:\n" +
    "            return \"c\"\n"
    )
    public void testMissingInNestedElseStatement() throws Exception {
        exception.expect(InvalidControlFlowException.class);
        compiler.compile();
    }
    
    @Test
    @Monty(
    "String foo(Bool cond):\n" +
    "    if cond:\n" +
    "        if not cond:\n" +
    "            return \"b\"\n"+
    "        else:\n" +
    "            return \"c\"\n" +
    "    else:\n" +
    "        return \"a\""
    )
    public void testNestedIfStatementsCorrectFlow() throws Exception {
        compiler.compile();
    }
}
