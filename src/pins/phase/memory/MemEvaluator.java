package pins.phase.memory;

import pins.data.ast.tree.*;
import pins.data.ast.tree.decl.*;
import pins.data.ast.tree.expr.*;
import pins.data.ast.tree.stmt.AstStmt;
import pins.data.ast.visitor.*;
import pins.data.mem.*;
import pins.data.typ.*;
import pins.phase.seman.*;

/**
 * Computing memory layout: frames and accesses.
 */
public class MemEvaluator extends AstFullVisitor<Object, MemEvaluator.Context> {

	/**
	 * The context {@link MemEvaluator} uses while computing function frames and
	 * variable accesses.
	 */
	protected abstract class Context {
	}

	/**
	 * Functional context, i.e., used when traversing function and building a new
	 * frame, parameter acceses and variable acceses.
	 */
	private class FunContext extends Context {
		public int depth = 0;
		public long locsSize = 0;
		public long argsSize = 0;
		public long parsSize = new SemPtr(new SemVoid()).size();
	}

	FunContext fc = new FunContext();

	public Object visit(AstTrees<? extends AstTree> trees, Context context) {
		for (AstTree tree : trees) {
			if (tree instanceof AstVarDecl) {
				AstVarDecl varDecl = (AstVarDecl) tree.accept(this, context);
				Memory.accesses.put(varDecl, new MemAbsAccess(SemAn.isType.get(varDecl.type).size(),
						new MemLabel(varDecl.name)));
			}
			if (tree instanceof AstFunDecl) {
				fc.locsSize = 0;
				fc.parsSize = 8;

				AstFunDecl funDecl = (AstFunDecl) tree.accept(this, context);
				fc.depth += 1;
				if (funDecl.expr != null)
					funDecl.expr.accept(this, context);
				fc.depth -= 1;
				MemLabel label = new MemLabel(funDecl.name);
				if (fc.depth > 1)
					label = new MemLabel();

				Memory.frames.put(funDecl, new MemFrame(label, fc.depth, fc.locsSize, fc.argsSize));
				fc.locsSize = 0;
				fc.argsSize = 0;
				fc.parsSize = 8;
			}
		}
		return trees;
	}

	public Object visit(AstStmtExpr stmtExpr, Context context) {
		for (AstStmt stmt : stmtExpr.stmts)
			stmt.accept(this, context);
		return stmtExpr;
	}

	public Object visit(AstWhereExpr whereExpr, Context context) {
		whereExpr.expr.accept(this, context);
		fc.locsSize = 0;
		fc.parsSize = 8;
		for (AstDecl decl : whereExpr.decls) {
			if (decl instanceof AstFunDecl) {
				AstFunDecl funDecl = (AstFunDecl) decl.accept(this, context);
				fc.depth += 1;
				funDecl.expr.accept(this, context);
				fc.depth -= 1;
				MemLabel label = new MemLabel(funDecl.name);
				if (fc.depth > 1)
					label = new MemLabel();

				Memory.frames.put(funDecl, new MemFrame(label, fc.depth, fc.locsSize, fc.argsSize));
				fc.locsSize = 0;
				fc.argsSize = 0;
				fc.parsSize = 8;
			}
			else
				decl.accept(this, context);
		}

		return whereExpr;
	}

	public Object visit(AstFunDecl funDecl, Context context) {
		for (AstParDecl parDecl : funDecl.pars) {
			if (parDecl != null) {
				long size = SemAn.isType.get(parDecl.type).size();
				fc.parsSize += size;
				Memory.accesses.put(parDecl, new MemRelAccess(size, fc.parsSize - size, fc.depth + 1));
			}
		}
		return funDecl;
	}

	public Object visit(AstVarDecl varDecl, Context context) {
		SemType type = SemAn.isType.get(varDecl.type);
		fc.locsSize += type.size();
		MemAccess access = null;
		if (fc.depth == 0) {
			access = new MemAbsAccess(type.size(), new MemLabel(varDecl.name));
		} else {
			access = new MemRelAccess(type.size(), -fc.locsSize, fc.depth);
		}

		Memory.accesses.put(varDecl, access);
		return varDecl;
	}


	public Object visit(AstCallExpr callExpr, Context context) {
		fc.argsSize = Math.max(fc.argsSize, callExpr.args.size() * 8 + 8);
		return callExpr;
	}
}
