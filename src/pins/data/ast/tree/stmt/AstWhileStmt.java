package pins.data.ast.tree.stmt;

import pins.common.report.*;
import pins.data.ast.tree.*;
import pins.data.ast.tree.expr.*;
import pins.data.ast.visitor.*;

/**
 * While statement.
 */
public class AstWhileStmt extends AstNode implements AstStmt {

	/** The condition. */
	public final AstExpr cond;

	/** The body statement. */
	public final AstStmt bodyStmt;

	/**
	 * Constructs a while statement.
	 * 
	 * @param location The location.
	 * @param cond     The condition.
	 * @param bodyStmt The body statement.
	 */
	public AstWhileStmt(Location location, AstExpr cond, AstStmt bodyStmt) {
		super(location);
		this.cond = cond;
		this.bodyStmt = bodyStmt;
	}

	@Override
	public <Result, Arg> Result accept(AstVisitor<Result, Arg> visitor, Arg arg) {
		return visitor.visit(this, arg);
	}

}
