package pins.data.ast.tree.stmt;

import pins.common.report.*;
import pins.data.ast.tree.*;
import pins.data.ast.tree.expr.*;
import pins.data.ast.visitor.*;

/**
 * If statement.
 */
public class AstIfStmt extends AstNode implements AstStmt {

	/** The condition. */
	public final AstExpr cond;

	/** The statement in the then branch. */
	public final AstStmt thenStmt;

	/** The statement in the else branch. */
	public final AstStmt elseStmt;

	/**
	 * Constructs an if statement.
	 * 
	 * @param location The location.
	 * @param cond     The condition.
	 * @param thenStmt The statement in the then branch.
	 * @param elseStmt The statement in the else branch.
	 */
	public AstIfStmt(Location location, AstExpr cond, AstStmt thenStmts, AstStmt elseStmts) {
		super(location);
		this.cond = cond;
		this.thenStmt = thenStmts;
		this.elseStmt = elseStmts;
	}

	@Override
	public <Result, Arg> Result accept(AstVisitor<Result, Arg> visitor, Arg arg) {
		return visitor.visit(this, arg);
	}

}
