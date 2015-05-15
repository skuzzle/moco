package de.uni.bremen.monty.moco.util;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.AbstractTypedASTNode;
import de.uni.bremen.monty.moco.ast.Package;
import de.uni.bremen.monty.moco.ast.PackageBuilder;
import de.uni.bremen.monty.moco.ast.Position;
import de.uni.bremen.monty.moco.ast.declaration.ModuleDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.TypeVariableDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Typed;
import de.uni.bremen.monty.moco.exception.MontyBaseException;
import de.uni.bremen.monty.moco.util.astsearch.SearchAST;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;
import de.uni.bremen.monty.moco.visitor.CodeGenerationVisitor;
import de.uni.bremen.monty.moco.visitor.ControlFlowVisitor;
import de.uni.bremen.monty.moco.visitor.DeclarationVisitor;
import de.uni.bremen.monty.moco.visitor.DotVisitor;
import de.uni.bremen.monty.moco.visitor.NameManglingVisitor;
import de.uni.bremen.monty.moco.visitor.SetParentVisitor;
import de.uni.bremen.monty.moco.visitor.typeinf.QuantumTypeErasor9k;
import de.uni.bremen.monty.moco.visitor.typeinf.QuantumTypeResolver3000;

public class CompileRule implements TestRule {

    /** Path to folder where generated .monty files will be stored */
    private static final String TEST_OUTPUT = "target/test-output/monty/";
    /** Path to folder where generated .dot files will be stored */
    private static final String DOT_OUTPUT = "target/test-output/dot/";
    /** Path to folder where generated ast pdfs will be stored */
    private static final String PDF_OUTPUT = "target/test-output/ast";
    /** Path to folder where genreated llvm code will be stored */
    private static final String LLVM_OUTPUT = "target/test-output/llvm/";
    /** Path to resource folder from which test monty files are read */
    private static final String TEST_DIR = "testTypeInference/";

    private static final long DOT_TIME_OUT = 30; // seconds

    private static final String DOT_EXECUTABLE = "monty.tests.dot";
    private static final String SKIP_GENERATE_RESOURCES = "monty.tests.skipResources";

    private ASTNode ast;
    private Monty monty;
    private TestResource montyResource;
    private String testName;
    private String namespace;

    @Override
    public Statement apply(Statement base, Description description) {
        this.namespace = getNameSpace(description.getTestClass().getSimpleName());
        this.monty = description.getAnnotation(Monty.class);
        this.montyResource = description.getAnnotation(TestResource.class);

        if (this.monty != null && this.montyResource != null) {
            fail("Can not specify @Monty and @TestResource on same test");
        } else if (this.monty == null && this.montyResource == null) {
            fail("No Monty input given. Specify either @Monty or @TestResource");
        }
        this.testName = description.getMethodName();
        return base;
    }

    private String getNameSpace(String className) {
        for (int i = 1; i < className.length(); ++i) {
            final char c = className.charAt(i);
            if (Character.isUpperCase(c)) {
                return className.substring(0, i);
            }
        }
        return className;
    }

    public <T extends ASTNode> T searchFor(Class<T> nodeType, Predicate<T> p) {
        return SearchAST.forNode(nodeType).where(p).in(this.ast).get();
    }

    public ASTNode typeCheck() throws Exception {
        final Params params = createParams();
        this.ast = createAST(params, Collections.emptyList());
        return this.ast;
    }

    public ASTNode typeCheckAndErase() throws Exception {
        final Params params = createParams();
        this.ast = createAST(params,
                Collections.singletonList(new QuantumTypeErasor9k()));
        return this.ast;
    }

    public ASTNode compile() throws Exception {
        final Params params = createParams();
        final List<BaseVisitor> additional = Arrays.asList(
                new QuantumTypeErasor9k(),
                new ControlFlowVisitor(),
                new NameManglingVisitor(),
                new CodeGenerationVisitor(params)
                );

        this.ast = createAST(params, additional);
        return this.ast;
    }

    public ASTNode getAst() {
        if (this.ast == null) {
            throw new IllegalStateException("code not compiled");
        }
        return this.ast;
    }

    public void assertAllTypesResolved() {
        getAst().visit(new BaseVisitor() {
            {
                setStopOnFirstError(true);
            }

            @Override
            protected void onEnterChildrenEachNode(ASTNode node) {
                if (node instanceof ModuleDeclaration) {
                    // TODO: handle moduledeclaration correctly
                    return;
                } else if (node instanceof Typed) {
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

    public void assertAllTypesErased() {
        getAst().visit(new BaseVisitor() {
            {
                setStopOnFirstError(true);
            }

            @Override
            protected void onEnterEachNode(ASTNode node) {
                if (node instanceof Typed) {
                    final Typed typed = (Typed) node;
                    if (typed.getTypeDeclaration() instanceof TypeVariableDeclaration) {
                        fail(String.format("Type variable not erased: <%s>", node));
                    }
                }
            }
        });
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

    private Params createParams() {
        final Params params = new Params();
        params.setGenerateOnlyLLVM(true);
        if (this.montyResource != null) {
            final File file = asFile(this.montyResource.value());
            final File inputFolder = file.getParentFile();
            params.setInputFile(file.getAbsolutePath());
            params.setInputFolder(inputFolder.getAbsolutePath());
        } else {
            params.setInputCode(this.monty.value());
        }
        return params;
    }

    private ASTNode createAST(Params params, List<BaseVisitor> additionalVisitors)
            throws Exception {
        final PackageBuilder builder = new PackageBuilder(params);
        final Package mainPackage = builder.buildPackage();
        executeVisitorChain(this.testName, params, additionalVisitors, mainPackage);
        return mainPackage;
    }

    private void executeVisitorChain(String testFileName, Params params,
            List<BaseVisitor> additionalVisitors, ASTNode root)
            throws Exception {

        testFileName = this.namespace + "_" + testFileName;
        if (skipGenerateResources()) {

        } else {
            final String llvmOutput = LLVM_OUTPUT + getFileName(testFileName) + ".ll";
            final File llvmDir = new File(LLVM_OUTPUT);
            if (!llvmDir.exists()) {
                llvmDir.mkdirs();
            }
            params.setOutputFile(llvmOutput);
        }

        final List<BaseVisitor> visitors = new ArrayList<>();
        visitors.add(new SetParentVisitor());
        visitors.add(new DeclarationVisitor());
        visitors.add(new QuantumTypeResolver3000());
        visitors.addAll(additionalVisitors);

        Exception error = null;
        for (final BaseVisitor bv : visitors) {
            try {
                bv.setStopOnFirstError(true);
                bv.visitDoubleDispatched(root);
            } catch (MontyBaseException e) {
                error = e;
                break;
            } catch (Exception e) {
                final Position pos = AbstractTypedASTNode.UNKNOWN_POSITION;
                final String message = pos + " :\n"
                    + e.getMessage();
                error = new Exception(message, e);
                break;
            }
        }
        writeDotFile(testFileName, root);
        if (params.getInputCode() != null) {
            writeTestMontyFile(testFileName, params.getInputCode());
        }

        if (error != null) {
            throw error;
        }
    }

    private void writeTestMontyFile(String testFileName, String content)
            throws IOException {
        if (skipGenerateResources()) {
            return;
        }
        final File targetFile = getNewFile(testFileName, TEST_OUTPUT, ".monty");

        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }

        try (PrintWriter w = new PrintWriter(targetFile)) {
            w.println(content);
        }
    }

    private void writeDotFile(String testFileName, ASTNode root)
            throws IOException, InterruptedException {
        if (skipGenerateResources()) {
            return;
        }
        final File targetDotFile = getNewFile(testFileName, DOT_OUTPUT, ".dot");

        if (!targetDotFile.getParentFile().exists()) {
            targetDotFile.getParentFile().mkdirs();
        }

        try (final DotVisitor dotVisitor = DotVisitor.toFile(targetDotFile, false)) {
            dotVisitor.visitDoubleDispatched(root);
        }

        createASTasPdf(testFileName, targetDotFile);
    }

    private String getFileName(String nameWithExtension) {
        final int dotIdx = nameWithExtension.lastIndexOf('.');
        if (dotIdx < 0) {
            // there is no dot in file name
            return nameWithExtension;
        }
        return nameWithExtension.substring(0, dotIdx);
    }

    private File getNewFile(String testFileName, String rootDir, String newExtension) {
        newExtension = newExtension.startsWith(".")
                ? newExtension
                : "." + newExtension;
        final File root = new File(rootDir);
        if (!root.exists()) {
            root.mkdirs();
        }
        final String originalFileName = getFileName(testFileName);
        final String dotFileName = originalFileName + newExtension;
        return new File(root, dotFileName);
    }

    private void createASTasPdf(String testFileName, File dotFile)
            throws IOException, InterruptedException {
        final File targetFile = getNewFile(testFileName, PDF_OUTPUT, ".pdf");
        final String dotExec = getDotExecutable();
        new ProcessBuilder(
                dotExec,
                "-Tpdf",
                "-o " + targetFile.getName(),
                dotFile.getAbsolutePath())
        .directory(targetFile.getParentFile())
        .inheritIO()
        .start();
    }

    private boolean skipGenerateResources() {
        return Boolean.parseBoolean(System.getProperty(SKIP_GENERATE_RESOURCES, "false"));
    }

    private String getDotExecutable() {
        return System.getProperty(DOT_EXECUTABLE,
                "C:\\Program Files (x86)\\Graphviz 2.28\\bin\\dot.exe");
    }
}
