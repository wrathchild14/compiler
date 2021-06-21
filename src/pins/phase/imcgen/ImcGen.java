package pins.phase.imcgen;

import pins.data.ast.attribute.*;
import pins.data.ast.tree.expr.*;
import pins.data.ast.tree.stmt.*;
import pins.data.ast.visitor.*;
import pins.data.imc.code.expr.*;
import pins.data.imc.code.stmt.*;
import pins.phase.*;

/**
 * Intermediate code generation.
 */
public class ImcGen extends Phase implements AstVisitor<Object, Object> {

	/** Maps statements to intermediate code. */
	public static final AstAttribute<AstStmt, ImcStmt> stmtImc = new AstAttribute<AstStmt, ImcStmt>(0);

	/** Maps expressions to intermediate code. */
	public static final AstAttribute<AstExpr, ImcExpr> exprImc = new AstAttribute<AstExpr, ImcExpr>(0);

	/**
	 * Constructs a new phase for intermediate code generation.
	 */
	public ImcGen() {
		super("imcgen");
	}

}
