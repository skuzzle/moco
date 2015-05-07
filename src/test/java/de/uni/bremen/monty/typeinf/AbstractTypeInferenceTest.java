package de.uni.bremen.monty.typeinf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.function.Consumer;

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.AbstractTypedASTNode;
import de.uni.bremen.monty.moco.ast.Package;
import de.uni.bremen.monty.moco.ast.PackageBuilder;
import de.uni.bremen.monty.moco.ast.Position;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Typed;
import de.uni.bremen.monty.moco.exception.MontyBaseException;
import de.uni.bremen.monty.moco.util.CodeStringBuilder;
import de.uni.bremen.monty.moco.util.CompileRule;
import de.uni.bremen.monty.moco.util.Params;
import de.uni.bremen.monty.moco.util.astsearch.InClause;
import de.uni.bremen.monty.moco.util.astsearch.SearchAST;
import de.uni.bremen.monty.moco.util.astsearch.WhereClause;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;
import de.uni.bremen.monty.moco.visitor.DeclarationVisitor;
import de.uni.bremen.monty.moco.visitor.DotVisitor;
import de.uni.bremen.monty.moco.visitor.SetParentVisitor;
import de.uni.bremen.monty.moco.visitor.typeinf.QuantumTypeErasor9k;
import de.uni.bremen.monty.moco.visitor.typeinf.QuantumTypeResolver3000;
import de.uni.bremen.monty.moco.visitor.typeinf.TypeInferenceException;

public class AbstractTypeInferenceTest {

    /** Path to resource folder from which test monty files are read */
    private static final String TEST_DIR = "testTypeInference/";

    /** Path to folder where generated .monty files will be stored -´ */
    private static final String TEST_OUTPUT = "target/test-output/type-inf/";

    private static final String DOT_OUTPUT = "target/dot/type-inf/";

    /**
     * Asserts that the given node's unique type is equal to the given expected
     * type.
     *
     * @param expected The (exact) expected type.
     * @param node The node to check for.
     */
    protected static void assertUniqueTypeIs(Type expected, Typed node) {
        if (!node.isTypeResolved()) {
            fail("No unique type resolved");
        }
        assertEquals(expected, node.getType());
    }

    /**
     * Asserts that every type-able node reachable from given root node has a
     * type assigned.
     *
     * @param root The root.
     */
    protected static void assertAllTypesResolved(ASTNode root) {
        root.visit(new BaseVisitor() {
            {
                setStopOnFirstError(true);
            }
            @Override
            protected void onEnterEachNode(ASTNode node) {
                if (node instanceof Typed) {
                    final Typed typed = (Typed) node;
                    if (!typed.isTypeResolved()) {
                        fail(String.format("Type not resolved on node: <%s>", node));
                    }
                    if (!typed.isTypeDeclarationResolved()) {
                        fail(String.format("TypeDeclaration not resolved on node: <%s>",
                                node));
                    }
                }
            }
        });
    }
    protected final CompileRule compiler = new CompileRule();
    protected final ExpectedException exception = ExpectedException.none();

    @Rule
    public RuleChain rules = RuleChain.emptyRuleChain()
            .around(this.compiler)
            .around(this.exception);

    protected void typeCheckAndExpectFailure(String subString) throws Exception {
        this.exception.expect(TypeInferenceException.class);
        this.exception.expectMessage(subString);
        this.compiler.typeCheck();
    }

    /**
     * Searches for AST nodes which reside in the file specified in the
     * constructor.
     *
     * @param type Type of the node to search for. Also matches sub types.
     * @return A {@link InClause} for specializing the search.
     */
    protected <C extends ASTNode> WhereClause<C> searchFor(Class<C> type) {
        return SearchAST.forNode(type);
    }

    /**
     * Creates an AST by parsing the given {@code code}.
     *
     * @param testFileName Name for the generated dot file.
     * @param code The code to parse.
     * @return Root of the AST.
     * @throws Exception
     */
    protected ASTNode getASTFromString(String testFileName, String code) throws Exception {
        final Params params = new Params();
        params.setInputCode(code);
        return createAST(testFileName, params);
    }

    protected ASTNode getASTFromString(String testFileName,
            Consumer<CodeStringBuilder> code) throws Exception {
        final CodeStringBuilder csb = new CodeStringBuilder();
        code.accept(csb);
        return getASTFromString(testFileName, csb.toString());
    }

    protected ASTNode getASTFromResource(String fileName) throws Exception {
        final File file = asFile(fileName);
        final File inputFolder = file.getParentFile();
        final Params params = new Params();
        params.setInputFile(file.getAbsolutePath());
        params.setInputFolder(inputFolder.getAbsolutePath());
        return createAST(fileName, params);
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
                new QuantumTypeResolver3000(),
                new QuantumTypeErasor9k()
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

    private File asFile(String fileName) {
        final ClassLoader cl = getClass().getClassLoader();
        final URL url = cl.getResource(TEST_DIR + fileName);
        if (url == null) {
            throw new IllegalArgumentException(
                    String.format("%s not found in %s", fileName, TEST_DIR));
        }
        return new File(url.getPath());
    }
}
