package de.uni.bremen.monty.typeinf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.Package;
import de.uni.bremen.monty.moco.ast.PackageBuilder;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Typed;
import de.uni.bremen.monty.moco.exception.MontyBaseException;
import de.uni.bremen.monty.moco.util.Params;
import de.uni.bremen.monty.moco.util.astsearch.InClause;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;
import de.uni.bremen.monty.moco.util.astsearch.SearchAST;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;
import de.uni.bremen.monty.moco.visitor.DeclarationVisitor;
import de.uni.bremen.monty.moco.visitor.DotVisitor;
import de.uni.bremen.monty.moco.visitor.SetParentVisitor;
import de.uni.bremen.monty.moco.visitor.typeinf.FirstPassTypeResolver;
import de.uni.bremen.monty.moco.visitor.typeinf.SecondPassTypeResolver;

public class AbstractTypeInferenceTest {

    /** Path to resource folder from which test monty files are read */
    private static final String TEST_DIR = "testTypeInference/";

    private static final String DOT_OUT_PUT = "target/dot/type-inf/";

    private final String testFileName;

    protected AbstractTypeInferenceTest(String testFileName) {
        this.testFileName = testFileName;
    }

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
    protected <C extends ASTNode> InClause<C> searchFor(Class<C> type) {
        return SearchAST.forNode(type).where(Predicates.inFile(this.testFileName));
    }

    public ASTNode getTypeCheckedAST() throws Exception {
        return getTypeCheckedAST(this.testFileName);
    }

    private ASTNode getTypeCheckedAST(String fileName) throws Exception {
        final File file = asFile(fileName);
        final File inputFolder = file.getParentFile();
        final Params params = new Params();
        params.setInputFile(file.getAbsolutePath());
        params.setInputFolder(inputFolder.getAbsolutePath());

        final PackageBuilder builder = new PackageBuilder(params);
        final Package mainPackage = builder.buildPackage();
        executeVisitorChain(fileName, mainPackage);
        return mainPackage;
    }

    private void executeVisitorChain(String testFileName, ASTNode root)
            throws Exception {
        final BaseVisitor[] visitors = new BaseVisitor[] {
                new SetParentVisitor(),
                new DeclarationVisitor(),
                new FirstPassTypeResolver(),
                new SecondPassTypeResolver()
        };
        for (final BaseVisitor bv : visitors) {
            try {
                bv.setStopOnFirstError(true);
                bv.visitDoubleDispatched(root);
            } catch (MontyBaseException e) {
                final String message = e.getLocation().getPosition() + " :\n"
                        + e.getMessage();
                throw new Exception(message, e);
            }
        }
        writeDotFile(testFileName, root);
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
