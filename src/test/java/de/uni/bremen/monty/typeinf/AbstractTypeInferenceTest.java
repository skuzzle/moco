package de.uni.bremen.monty.typeinf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.declaration.ModuleDeclaration;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Type;
import de.uni.bremen.monty.moco.ast.declaration.typeinf.Typed;
import de.uni.bremen.monty.moco.util.CompileRule;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;
import de.uni.bremen.monty.moco.visitor.typeinf.TypeInferenceException;

public class AbstractTypeInferenceTest {

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
                if (node instanceof ModuleDeclaration) {
                    // TODO!
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

    protected final CompileRule compiler = new CompileRule();
    protected final ExpectedException exception = ExpectedException.none();

    @Rule
    public RuleChain rules = RuleChain.emptyRuleChain()
            .around(this.compiler)
            .around(this.exception);

    protected CompileRule compile() throws Exception {
        compiler.compile();
        return compiler;
    }

    protected void typeCheckAndExpectFailure() throws Exception {
        this.exception.expect(TypeInferenceException.class);
        this.compiler.typeCheck();
    }

    protected void typeCheckAndExpectFailure(String subString) throws Exception {
        this.exception.expect(TypeInferenceException.class);
        this.exception.expectMessage(subString);
        this.compiler.typeCheck();
    }
}
