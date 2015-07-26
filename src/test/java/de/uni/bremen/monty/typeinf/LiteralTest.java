package de.uni.bremen.monty.typeinf;

import org.junit.Test;

import de.uni.bremen.monty.moco.ast.CoreClasses;
import de.uni.bremen.monty.moco.ast.declaration.ClassDeclaration;
import de.uni.bremen.monty.moco.ast.expression.literal.BooleanLiteral;
import de.uni.bremen.monty.moco.ast.expression.literal.CharacterLiteral;
import de.uni.bremen.monty.moco.ast.expression.literal.FloatLiteral;
import de.uni.bremen.monty.moco.ast.expression.literal.IntegerLiteral;
import de.uni.bremen.monty.moco.ast.expression.literal.LiteralExpression;
import de.uni.bremen.monty.moco.ast.expression.literal.StringLiteral;
import de.uni.bremen.monty.moco.util.Monty;
import de.uni.bremen.monty.moco.util.astsearch.Predicates;

public class LiteralTest extends AbstractTypeInferenceTest {

    @Test
    @Monty(
    "Int a := 123"
    )
    public void testIntLiteral() throws Exception {
        testLiteral(IntegerLiteral.class, CoreClasses.intType());
    }

    @Test
    @Monty(
    "Float a := 123.4"
    )
    public void testFloatLiteral() throws Exception {
        testLiteral(FloatLiteral.class, CoreClasses.floatType());
    }

    @Test
    @Monty(
    "String a := \"123.4\""
    )
    public void testStringLiteral() throws Exception {
        testLiteral(StringLiteral.class, CoreClasses.stringType());
    }

    @Test
    @Monty(
    "Bool a := true"
    )
    public void testBooleanLiteral() throws Exception {
        testLiteral(BooleanLiteral.class, CoreClasses.boolType());
    }

    @Test
    @Monty(
    "Char a := 'c'"
    )
    public void testCharacterLiteral() throws Exception {
        testLiteral(CharacterLiteral.class, CoreClasses.charType());
    }

    private <L extends LiteralExpression<?>> void testLiteral(Class<L> cls,
            ClassDeclaration coreClass) throws Exception {

        this.compile();
        final L lit = this.compiler.searchFor(cls, Predicates.onLine(1));

        assertUniqueTypeIs(coreClass.getType(), lit);
    }
}
