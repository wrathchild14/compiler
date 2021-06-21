package pins.data.ast.tree.stmt;

import pins.common.report.*;
import pins.data.ast.tree.*;
import pins.data.ast.tree.expr.*;
import pins.data.ast.visitor.*;

/**
 * Expression statement.
 */
public class AstExprStmt extends AstNode implements AstStmt {

	/** The expression. */
	public final AstExpr expr;

	/**
	 * Constructs an expression statement.
	 * 
	 * @param location The location.
	 * @param expr     The expression.
	 */
	public AstExprStmt(Location location, AstExpr expr) {
		super(location);
		this.expr = expr;
	}

	@Override
	public <Result, Arg> Result accept(AstVisitor<Result, Arg> visitor, Arg arg) {
		return visitor.visit(this, arg);
	}

}
