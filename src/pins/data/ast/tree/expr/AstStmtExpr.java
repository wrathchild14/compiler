package pins.data.ast.tree.expr;

import pins.common.report.*;
import pins.data.ast.tree.*;
import pins.data.ast.tree.stmt.*;
import pins.data.ast.visitor.*;

/**
 * Statement expression.
 */
public class AstStmtExpr extends AstNode implements AstExpr {

	/** The statements. */
	public final AstTrees<AstStmt> stmts;

	/**
	 * Constructs a where expression.
	 * 
	 * @param location The location.
	 * @param stmts    The statements.
	 */
	public AstStmtExpr(Location location, AstTrees<AstStmt> stmts) {
		super(location);
		this.stmts = stmts;
	}

	@Override
	public <Result, Arg> Result accept(AstVisitor<Result, Arg> visitor, Arg arg) {
		return visitor.visit(this, arg);
	}

}
