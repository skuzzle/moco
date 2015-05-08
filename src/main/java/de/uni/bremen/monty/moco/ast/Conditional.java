package de.uni.bremen.monty.moco.ast;

import de.uni.bremen.monty.moco.ast.expression.ConditionalExpression;
import de.uni.bremen.monty.moco.ast.expression.Expression;
import de.uni.bremen.monty.moco.ast.statement.ConditionalStatement;
import de.uni.bremen.monty.moco.ast.statement.WhileLoop;

/**
 * Interface for nodes which hold a condition like the
 * {@link ConditionalStatement}, {@link ConditionalExpression} or
 * {@link WhileLoop}
 *
 * @author Simon Taddiken
 */
public interface Conditional {

    public Expression getCondition();

}
