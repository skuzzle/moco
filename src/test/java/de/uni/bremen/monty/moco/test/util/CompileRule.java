package de.uni.bremen.monty.moco.test.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.AbstractTypedASTNode;
import de.uni.bremen.monty.moco.ast.Package;
import de.uni.bremen.monty.moco.ast.PackageBuilder;
import de.uni.bremen.monty.moco.ast.Position;
import de.uni.bremen.monty.moco.exception.MontyBaseException;
import de.uni.bremen.monty.moco.util.Params;
import de.uni.bremen.monty.moco.util.astsearch.SearchAST;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;
import de.uni.bremen.monty.moco.visitor.DeclarationVisitor;
import de.uni.bremen.monty.moco.visitor.DotVisitor;
import de.uni.bremen.monty.moco.visitor.SetParentVisitor;
import de.uni.bremen.monty.moco.visitor.typeinf.QuantumTypeResolver3000;


public class CompileRule implements TestRule {

    /** Path to folder where generated .monty files will be stored -´ */
    private static final String TEST_OUTPUT = "target/test-output/type-inf/";

    private static final String DOT_OUTPUT = "target/dot/type-inf/";

    private ASTNode ast;

    @Override
    public Statement apply(Statement base, Description description) {
        final Monty monty = description.getAnnotation(Monty.class);
        if (monty == null) {
            return base;
        }
        return new CompileStatement(monty, base, description.getMethodName());
    }

    private class CompileStatement extends Statement {

        private final Monty monty;
        private final Statement base;
        private final String testMethod;

        public CompileStatement(Monty monty, Statement base, String testMethod) {
            this.monty = monty;
            this.base = base;
            this.testMethod = testMethod;
        }

        @Override
        public void evaluate() throws Throwable {
            try {
                CompileRule.this.ast = getASTFromString(this.testMethod,
                        this.monty.value());
                assertIfNoException();
            } catch (Exception e) {
                assertIfException(e);
            }
            this.base.evaluate();
        }

        private void assertIfNoException() {
            if (this.monty.expect() != None.class) {
                Assert.fail(String.format("Expected <%s>", this.monty.expect().getName()));
            }
        }

        private void assertIfException(Exception e) throws Exception {
            if (this.monty.expect() == None.class) {
                throw e;
            } else if (!this.monty.expect().isInstance(e)) {
                Assert.fail(String.format("Unexpected exception <%s>. Expected: <%s>",
                        e.getClass().getName(), this.monty.expect().getName()));
            } else if (!this.monty.matching().isEmpty()) {
                final String pattern = ".*" + this.monty.matching() + ".*";
                final Pattern regex = Pattern.compile(pattern, Pattern.DOTALL);
                if (e.getMessage() == null || !regex.matcher(e.getMessage()).matches()) {
                    Assert.fail(String.format(
                            "Expected message matching <%s> but was <%s>",
                            this.monty.matching(), e.getMessage()));
                }
            }
        }
    }

    public <T extends ASTNode> T searchFor(Class<T> nodeType, Predicate<T> p) {
        return SearchAST.forNode(nodeType).where(p).in(this.ast).get();

    }

    public ASTNode getAst() {
        return this.ast;
    }

    /**
     * Creates an AST by parsing the given {@code code}.
     *
     * @param testFileName Name for the generated dot file.
     * @param code The code to parse.
     * @return Root of the AST.
     * @throws Exception
     */
    private ASTNode getASTFromString(String testFileName, String code) throws Exception {
        final Params params = new Params();
        params.setInputCode(code);
        return createAST(testFileName, params);
    }

    private ASTNode createAST(String testFilename, Params params) throws Exception {
        final PackageBuilder builder = new PackageBuilder(params);
        final Package mainPackage = builder.buildPackage();
        executeVisitorChain(testFilename, params.getInputCode(), mainPackage);
        return mainPackage;
    }

    private void executeVisitorChain(String testFileName, String code, ASTNode root)
            throws Exception {
        final BaseVisitor[] visitors = new BaseVisitor[] {
                new SetParentVisitor(),
                new DeclarationVisitor(),
                new QuantumTypeResolver3000()
        };
        Exception error = null;
        for (final BaseVisitor bv : visitors) {
            try {
                bv.setStopOnFirstError(true);
                bv.visitDoubleDispatched(root);
            } catch (MontyBaseException e) {
                error = e;
            } catch (Exception e) {
                final Position pos = AbstractTypedASTNode.UNKNOWN_POSITION;
                final String message = pos + " :\n"
                    + e.getMessage();
                error = new Exception(message, e);
                break;
            }
        }
        writeDotFile(testFileName, root);
        if (error != null) {
            throw error;
        } else if (code != null) {
            writeTestMontyFile(testFileName, code);
        }
    }

    private void writeTestMontyFile(String testFileName, String content)
            throws IOException {
        final String originalFileName = getFileName(testFileName);
        final String montyFileName = originalFileName + ".monty";
        final File targetFile = new File(TEST_OUTPUT, montyFileName);

        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }

        try (PrintWriter w = new PrintWriter(targetFile)) {
            w.println(content);
        }
    }

    private void writeDotFile(String testFileName, ASTNode root) throws IOException {
        final String originalFileName = getFileName(testFileName);
        final String dotFileName = originalFileName + ".dot";
        final File targetDotFile = new File(DOT_OUTPUT, dotFileName);

        if (!targetDotFile.getParentFile().exists()) {
            targetDotFile.getParentFile().mkdirs();
        }

        try (final DotVisitor dotVisitor = DotVisitor.toFile(targetDotFile, false)) {
            dotVisitor.visitDoubleDispatched(root);
        }
    }

    private String getFileName(String nameWithExtension) {
        final int dotIdx = nameWithExtension.lastIndexOf('.');
        if (dotIdx < 0) {
            // there is no dot in file name
            return nameWithExtension;
        }
        return nameWithExtension.substring(0, dotIdx);
    }
}
