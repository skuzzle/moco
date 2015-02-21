package de.uni.bremen.monty.typeinf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.AbstractTypedASTNode;
import de.uni.bremen.monty.moco.ast.Package;
import de.uni.bremen.monty.moco.ast.PackageBuilder;
import de.uni.bremen.monty.moco.ast.Position;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Typed;
import de.uni.bremen.monty.moco.exception.MontyBaseException;
import de.uni.bremen.monty.moco.util.CodeStringBuilder;
import de.uni.bremen.monty.moco.util.Params;
import de.uni.bremen.monty.moco.util.astsearch.InClause;
import de.uni.bremen.monty.moco.util.astsearch.SearchAST;
import de.uni.bremen.monty.moco.util.astsearch.WhereClause;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;
import de.uni.bremen.monty.moco.visitor.DeclarationVisitor;
import de.uni.bremen.monty.moco.visitor.DotVisitor;
import de.uni.bremen.monty.moco.visitor.SetParentVisitor;
import de.uni.bremen.monty.moco.visitor.typeinf.QuantumTypeResolver3000;

public class AbstractTypeInferenceTest {

    /** Path to resource folder from which test monty files are read */
    private static final String TEST_DIR = "testTypeInference/";

    private static final String DOT_OUT_PUT = "target/dot/type-inf/";

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
     * Asserts that the given node's possible types contain the given type.
     *
     * @param possibleType The type to search for.
     * @param node The node to check for.
     */
    protected static void assertHasPossibleType(Type possibleType, Typed node) {
        assertTrue(String.format("Node does not contain the type <%s>", possibleType),
                node.getTypes().contains(possibleType));
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
        executeVisitorChain(testFilename, mainPackage);
        return mainPackage;
    }

    private void executeVisitorChain(String testFileName, ASTNode root)
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
            } catch (Exception e) {
                final Position pos;
                if (e instanceof MontyBaseException) {
                    final MontyBaseException mbe = (MontyBaseException) e;
                    pos = mbe.getLocation() != null
                            ? mbe.getLocation().getPosition()
                            : AbstractTypedASTNode.UNKNOWN_POSITION;
                } else {
                    pos = AbstractTypedASTNode.UNKNOWN_POSITION;
                }
                final String message = pos + " :\n"
                        + e.getMessage();
                error = new Exception(message, e);
                break;
            }
        }
        writeDotFile(testFileName, root);
        if (error != null) {
            throw error;
        }
    }

    private void writeDotFile(String testFileName, ASTNode root) throws IOException {
        final String originalFileName = getFileName(testFileName);
        final String dotFileName = originalFileName + ".dot";
        final File targetDotFile = new File(DOT_OUT_PUT, dotFileName);

        if (!targetDotFile.getParentFile().exists()) {
            targetDotFile.getParentFile().mkdirs();
        }

        try (final DotVisitor dotVisitor = DotVisitor.toFile(targetDotFile)) {
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
