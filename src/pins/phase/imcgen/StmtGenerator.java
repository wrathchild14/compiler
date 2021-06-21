package pins.phase.imcgen;

import java.util.*;

import pins.data.ast.tree.stmt.*;
import pins.data.ast.visitor.*;
import pins.data.imc.code.stmt.*;
import pins.data.mem.*;

public class StmtGenerator implements AstVisitor<ImcStmt, Stack<MemFrame>> {

	@Override
	public ImcStmt visit(AstAssignStmt assignStmt, Stack<MemFrame> frames) {
		ImcStmt code = new ImcMOVE(assignStmt.dst.accept(new ExprGenerator(), frames),
				assignStmt.src.accept(new ExprGenerator(), frames));
		ImcGen.stmtImc.put(assignStmt, code);
		return code;
	}

	@Override
	public ImcStmt visit(AstExprStmt exprStmt, Stack<MemFrame> frames) {
		ImcStmt code = null;
		if (exprStmt.expr != null) {
			code = new ImcESTMT(exprStmt.expr.accept(new ExprGenerator(), frames));
			ImcGen.stmtImc.put(exprStmt, code);
		}
		return code;
	}

	@Override
	public ImcStmt visit(AstIfStmt ifStmt, Stack<MemFrame> frames) {
		ImcStmt code = null;
		Vector<ImcStmt> stmtsCode = new Vector<ImcStmt>();
		MemLabel posLabel = new MemLabel();
		MemLabel negLabel = new MemLabel();
		MemLabel exitLabel = new MemLabel();
		stmtsCode.add(new ImcCJUMP(ifStmt.cond.accept(new ExprGenerator(), frames), posLabel, negLabel));
		stmtsCode.add(new ImcLABEL(posLabel));
		stmtsCode.add(ifStmt.thenStmt.accept(this, frames));
		stmtsCode.add(new ImcJUMP(exitLabel));
		stmtsCode.add(new ImcLABEL(negLabel));
		if (ifStmt.elseStmt != null)
			stmtsCode.add(ifStmt.elseStmt.accept(this, frames));
		stmtsCode.add(new ImcLABEL(exitLabel));
		code = new ImcSTMTS(stmtsCode);
		ImcGen.stmtImc.put(ifStmt, code);
		return code;
	}

	@Override
	public ImcStmt visit(AstWhileStmt whileStmt, Stack<MemFrame> frames) {
		ImcStmt code = null;
		Vector<ImcStmt> stmtsCode = new Vector<ImcStmt>();
		MemLabel condLabel = new MemLabel();
		MemLabel stmtLabel = new MemLabel();
		MemLabel exitLabel = new MemLabel();
		stmtsCode.add(new ImcLABEL(condLabel));
		stmtsCode.add(new ImcCJUMP(whileStmt.cond.accept(new ExprGenerator(), frames), stmtLabel, exitLabel));
		stmtsCode.add(new ImcLABEL(stmtLabel));
		stmtsCode.add(whileStmt.bodyStmt.accept(this, frames));
		stmtsCode.add(new ImcJUMP(condLabel));
		stmtsCode.add(new ImcLABEL(exitLabel));
		code = new ImcSTMTS(stmtsCode);
		ImcGen.stmtImc.put(whileStmt, code);
		return code;
	}

}
