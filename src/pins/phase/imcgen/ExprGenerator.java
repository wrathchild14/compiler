package pins.phase.imcgen;

import java.util.*;

import pins.data.ast.tree.*;
import pins.data.ast.tree.decl.*;
import pins.data.ast.tree.expr.*;
import pins.data.ast.tree.stmt.*;
import pins.data.ast.visitor.*;
import pins.data.imc.code.expr.*;
import pins.data.imc.code.stmt.*;
import pins.data.mem.*;
import pins.data.typ.*;
import pins.phase.imclin.ExprCanonizer;
import pins.phase.memory.*;
import pins.phase.seman.*;

public class ExprGenerator implements AstVisitor<ImcExpr, Stack<MemFrame>> {

	@Override
	public ImcExpr visit(AstWhereExpr whereExpr, Stack<MemFrame> frames) {
		whereExpr.decls.accept(new CodeGenerator(), frames);
		ImcExpr code = whereExpr.expr.accept(this, frames);
		ImcGen.exprImc.put(whereExpr, code);
		return code;
	}

	public ImcExpr visit(AstAtomExpr atomExpr, Stack<MemFrame> memFrames) {
		switch (atomExpr.type) {
			case VOID, POINTER -> // none or null
					ImcGen.exprImc.put(atomExpr, new ImcCONST(0));
			case CHAR -> ImcGen.exprImc.put(atomExpr, new ImcCONST(atomExpr.value.charAt(1)));
			case INT -> ImcGen.exprImc.put(atomExpr, new ImcCONST(Long.parseLong(atomExpr.value)));
		}
		return ImcGen.exprImc.get(atomExpr);
	}

	public ImcExpr visit(AstBinExpr binExpr, Stack<MemFrame> memFrames) {
		ImcExpr first = binExpr.fstExpr.accept(this, memFrames);
		ImcExpr second = binExpr.sndExpr.accept(this, memFrames);
		ImcBINOP.Oper oper = ImcBINOP.Oper.ADD;
		switch (binExpr.oper) {
			case DIV -> oper = ImcBINOP.Oper.DIV;
			case EQU -> oper = ImcBINOP.Oper.EQU;
			case GEQ -> oper = ImcBINOP.Oper.GEQ;
			case GTH -> oper = ImcBINOP.Oper.GTH;
			case LEQ -> oper = ImcBINOP.Oper.LEQ;
			case LTH -> oper = ImcBINOP.Oper.LTH;
			case MOD -> oper = ImcBINOP.Oper.MOD;
			case MUL -> oper = ImcBINOP.Oper.MUL;
			case NEQ -> oper = ImcBINOP.Oper.NEQ;
			case SUB -> oper = ImcBINOP.Oper.SUB;
			default -> {
			}
		}
		ImcBINOP bin = new ImcBINOP(oper, first, second);
		ImcGen.exprImc.put(binExpr, bin);
		return bin;
	}

	public ImcExpr visit(AstCallExpr callExpr, Stack<MemFrame> memFrames) {
		MemLabel label = Memory.frames.get((AstFunDecl) SemAn.declaredAt.get(callExpr)).label;

		Vector<ImcExpr> vector = new Vector<ImcExpr>();
		Vector<Long> offsets = new Vector<>();

//		ImcCALL call = new ImcCALL(label, offsets, vector);
		for (AstExpr expr : callExpr.args) {
			if (expr != null) {
				ImcExpr imcExpr = expr.accept(new ExprGenerator(), memFrames);
				vector.add(imcExpr);
			}
		}
		ImcCALL call = new ImcCALL(label, offsets, vector);
		ImcGen.exprImc.put(callExpr, call);

		return call;
	}

	public ImcExpr visit(AstNameExpr nameExpr, Stack<MemFrame> memFrames) {
		AstParDecl parDecl;
		AstVarDecl varDecl;
		MemRelAccess parAccess;
//		MemRelAccess varAccess;
		ImcCONST offset;

		try {
			parDecl = (AstParDecl) SemAn.declaredAt.get(nameExpr);
			parAccess = (MemRelAccess) Memory.accesses.get(parDecl);
			offset = new ImcCONST(parAccess.offset);
		} catch (ClassCastException e) {
			try {
				varDecl = (AstVarDecl) SemAn.declaredAt.get(nameExpr);
				MemAbsAccess varAccess = (MemAbsAccess) Memory.accesses.get(varDecl);
				offset = new ImcCONST(varAccess.size);

//				varAccess = (MemRelAccess) Memory.accesses.get(varDecl);
//				offset = new ImcCONST(varAccess.offset);
			} catch (ClassCastException f) {
//				varDecl = (AstVarDecl) SemAn.declaredAt.get(nameExpr);
//				MemAbsAccess varde = (MemAbsAccess) Memory.accesses.get(varDecl);
//				offset = new ImcCONST(varde.size);

				ImcNAME name = new ImcNAME(new MemLabel(nameExpr.name));
				ImcMEM mem = new ImcMEM(name);
				ImcGen.exprImc.put(nameExpr, mem);
				return mem;
			}
		}
		ImcTEMP tmp = new ImcTEMP(memFrames.peek().FP);
		ImcMEM mem = new ImcMEM(new ImcBINOP(ImcBINOP.Oper.ADD, tmp, offset));
		ImcGen.exprImc.put(nameExpr, mem);
		return mem;
	}

	// oof
	public ImcExpr visit(AstExprStmt exprStmt, Stack<MemFrame> memFrames) {
		return exprStmt.expr.accept(this, memFrames);
	}

	public ImcExpr visit(AstIfStmt ifStmt, Stack<MemFrame> memFrames) {
		return ifStmt.cond.accept(this, memFrames);
	}

	public ImcExpr visit(AstWhileStmt whileStmt, Stack<MemFrame> memFrames) {
		return whileStmt.cond.accept(this, memFrames);
	}

	public ImcExpr visit(AstAssignStmt assignStmt, Stack<MemFrame> memFrames) {
		return assignStmt.dst.accept(this, memFrames);
	}

	public ImcExpr visit(AstStmtExpr stmtExpr, Stack<MemFrame> memFrames) {
		ImcStmt code = null;
		Vector<ImcStmt> vecStmt = new Vector<>();

		ImcExpr imcExpr = new ImcCONST(0);

		AstStmt lastStmt = null;

		ImcStmt spr = null;
		for (AstStmt stmt : stmtExpr.stmts) {
			spr = stmt.accept(new StmtGenerator(), memFrames);
			if (spr != null) {
				vecStmt.add(spr);
				lastStmt = stmt;
			}
		}
		if (lastStmt != null) {
			imcExpr = lastStmt.accept(this, memFrames);
		}

		code = new ImcSTMTS(vecStmt);

		ImcSEXPR sexpr = new ImcSEXPR(code,  imcExpr);
		ImcGen.exprImc.put(stmtExpr, sexpr);
		return sexpr;
	}
}
