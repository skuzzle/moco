package de.uni.bremen.monty.moco.ast.declaration;

import de.uni.bremen.monty.moco.ast.ASTNode;
import de.uni.bremen.monty.moco.ast.Identifier;
import de.uni.bremen.monty.moco.ast.Position;
import de.uni.bremen.monty.moco.visitor.BaseVisitor;

public class TypeVariable extends TypeDeclaration {

	public static final String NAME = "$VAR_";
	private static int counter;

	public static String nextName() {
		return NAME.concat(String.valueOf(counter++));
	}

	public static boolean isNameATypeVariable(Identifier id) {
		return id.getSymbol().startsWith(NAME);
	}

	private TypeDeclaration resolvedType;

	public TypeVariable(Position position, Identifier identifier) {
		super(position, identifier);
	}

	@Override
	public TypeDeclaration unwrapVariable() {
		if (this.resolvedType instanceof TypeVariable) {
			return resolvedType.unwrapVariable();
		}
		return getResolvedType();
	}

	public void setResolvedType(TypeDeclaration resolvedType) {
		this.resolvedType = resolvedType;
	}

	public TypeDeclaration getResolvedType() {
	    if (!isResolved()) {
	        throw new IllegalStateException(String.format(
	                "Variable '%s' has not been resolved", this.getIdentifier()));
	    }
		return this.resolvedType;
	}

	public boolean isResolved() {
		return this.resolvedType != null;
	}

	@Override
	public void setParentNode(ASTNode parentNode) {
		super.setParentNode(parentNode);
	}

	@Override
	public boolean matchesType(TypeDeclaration other) {
		if (isResolved()) {
			return this.resolvedType.matchesType(other);
		}
		return true;
	}

	@Override
	public void visit(BaseVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void visitChildren(BaseVisitor visitor) {
	}

}
