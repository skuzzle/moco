package de.uni.bremen.monty.moco.util;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
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

    /** Path to folder where generated .monty files will be stored -´ */
    private static final String TEST_OUTPUT = "target/test-output/type-inf/";
    private static final String DOT_OUTPUT = "target/dot/type-inf/";
    private static final String LLVM_OUTPUT = "target/test-output/llvm/";
    /** Path to resource folder from which test monty files are read */
    private static final String TEST_DIR = "testTypeInference/";

    private ASTNode ast;
    private Monty monty;
    private TestResource montyResource;
    private String testName;

    @Override
    public Statement apply(Statement base, Description description) {
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

    public <T extends ASTNode> T searchFor(Class<T> nodeType, Predicate<T> p) {
        return SearchAST.forNode(nodeType).where(p).in(this.ast).get();

    }

    public ASTNode typeCheck() throws Exception {
        this.ast = createAST(false);
        return this.ast;
    }

    public ASTNode compile() throws Exception {
        this.ast = createAST(true);
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

    private ASTNode createAST(boolean full) throws Exception {
        final Params params = new Params();
        if (this.montyResource != null) {
            final File file = asFile(this.montyResource.value());
            final File inputFolder = file.getParentFile();
            params.setInputFile(file.getAbsolutePath());
            params.setInputFolder(inputFolder.getAbsolutePath());
        } else {
            params.setInputCode(this.monty.value());
        }
        final PackageBuilder builder = new PackageBuilder(params);
        final Package mainPackage = builder.buildPackage();
        executeVisitorChain(this.testName, params, full, mainPackage);
        return mainPackage;
    }


    private void executeVisitorChain(String testFileName, Params params,
            boolean full, ASTNode root) throws Exception {

        final String llvmOutput = LLVM_OUTPUT + getFileName(testFileName) + ".ll";
        final File llvmDir = new File(LLVM_OUTPUT);
        if (!llvmDir.exists()) {
            llvmDir.mkdirs();
        }
        params.setGenerateOnlyLLVM(true);
        params.setOutputFile(llvmOutput);

        final List<BaseVisitor> visitors = new ArrayList<>();
        visitors.add(new SetParentVisitor());
        visitors.add(new DeclarationVisitor());
        visitors.add(new QuantumTypeResolver3000());
        visitors.add(new QuantumTypeErasor9k());

        if (full) {
            visitors.add(new ControlFlowVisitor());
            visitors.add(new NameManglingVisitor());
            visitors.add(new CodeGenerationVisitor(params));
        }
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
        } else if (params.getInputCode() != null) {
            writeTestMontyFile(testFileName, params.getInputCode());
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
