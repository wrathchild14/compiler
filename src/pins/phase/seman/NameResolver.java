package pins.phase.seman;

import pins.common.report.*;
import pins.data.ast.tree.*;
import pins.data.ast.tree.decl.*;
import pins.data.ast.tree.expr.*;
import pins.data.ast.tree.stmt.*;
import pins.data.ast.tree.type.*;
import pins.data.ast.visitor.*;

/**
 * Name resolver.
 * <p>
 * Name resolver connects each node of a abstract syntax tree where a name is
 * used with the node where it is declared. The only exceptions are a record
 * field names which are connected with its declarations by type resolver. The
 * results of the name resolver are stored in
 * {@link pins.phase.seman.SemAn#declaredAt}.
 */
public class NameResolver extends AstFullVisitor<Object, NameResolver.Mode> {

	public enum Mode {
		HEAD, BODY
	}

	private final SymbTable symbTable = new SymbTable();

	// GENERAL PURPOSE

	public Object visit(AstTrees<?> trees, Mode mode) {
		for (AstTree tree : trees)
			if (tree instanceof AstTypeDecl)
				tree.accept(this, Mode.HEAD);
		for (AstTree tree : trees)
			if (tree instanceof AstTypeDecl)
				tree.accept(this, Mode.BODY);
		for (AstTree tree : trees)
			if (tree instanceof AstVarDecl)
				tree.accept(this, mode);
		for (AstTree tree : trees)
			if (tree instanceof AstFunDecl)
				tree.accept(this, Mode.HEAD);
		for (AstTree tree : trees)
			if (tree instanceof AstFunDecl)
				tree.accept(this, Mode.BODY);
		return null;
	}

	// DECLARATIONS
	@Override
	public Object visit(AstVarDecl varDecl, Mode mode) {
		try {
			symbTable.ins(varDecl.name, varDecl);
//			System.out.println("VAR " + varDecl.name);
		} catch (SymbTable.CannotInsNameException __) {
			throw new Report.Error(varDecl, "Cannot redefine '" + (varDecl.name) + "' as a variable.");
		}
		varDecl.type.accept(this, null);
		return null;
	}

	public Object visit(AstTypeDecl typeDecl, Mode mode) {
		if (mode == Mode.HEAD) {
			try {
				symbTable.ins(typeDecl.name, typeDecl);
//				System.out.println("TYPE " + typeDecl.name);
			} catch (SymbTable.CannotInsNameException __) {
				throw new Report.Error(typeDecl, "Cannot redefine '" + (typeDecl.name) + "' as a variable.");
			}
		}
		typeDecl.type.accept(this, null);
		return null;
	}

	public Object visit(AstFunDecl funDecl, Mode mode) {
		if (mode == Mode.HEAD) {
			try {
				symbTable.ins(funDecl.name, funDecl);
//				System.out.println("FUN " + funDecl.name);
			} catch (SymbTable.CannotInsNameException __) {
				throw new Report.Error(funDecl, "Cannot redefine '" + (funDecl.name) + "' as a variable.");
			}
		} else if (mode == Mode.BODY){
			funDecl.type.accept(this, mode);
			symbTable.newScope();
			for (AstParDecl pardecl : funDecl.pars) {
				if (pardecl != null) {
					try {
						symbTable.ins(pardecl.name, pardecl);
					} catch (SymbTable.CannotInsNameException e) {
						throw new Report.Error(pardecl.location(), "SemanticError: '" + pardecl.name + "' is already declared");
					}
				}
			}
			if (funDecl.expr != null)
				funDecl.expr.accept(this, null);
			symbTable.oldScope();
		}
		return null;
	}

	public Object visit(AstNameType typName, Mode mode) {
		try {
			SemAn.declaredAt.put(typName, symbTable.fnd(typName.name));
//			System.out.println("I SAW " + typName.name);
		} catch (SymbTable.CannotFndNameException e) {
			throw new Report.Error(typName.location(), "Semantic Error: '" + typName.name + "' is not declared");
		}
		return null;
	}

	public Object visit(AstNameExpr nameExpr, Mode mode) {
		try {
			SemAn.declaredAt.put(nameExpr, symbTable.fnd(nameExpr.name));
//			System.out.println("I SAW " + nameExpr.name);
		} catch (SymbTable.CannotFndNameException e) {
			throw new Report.Error(nameExpr.location(), "Semantic Error: '" + nameExpr.name + "' is not declared");
		}
		return null;
	}

	public Object visit(AstCallExpr callExpr, Mode mode) {
		try {
			SemAn.declaredAt.put(callExpr, symbTable.fnd(callExpr.name));
			for (AstExpr expr : callExpr.args) // this was the error the whole time
				if (expr != null)
					expr.accept(this, mode);
//			System.out.println("I SAW " + callExpr.name);
		} catch (SymbTable.CannotFndNameException e) {
			throw new Report.Error(callExpr.location(), "Semantic Error: '" + callExpr.name + "' is not declared");
		}
		return null;
	}

	public Object visit(AstStmtExpr stmtExpr, Mode mode) {
		for (AstTree stmt : stmtExpr.stmts)
			stmt.accept(this, mode);
		return null;
	}

	public Object visit(AstWhereExpr whereExpr, Mode mode) {
//		for (AstDecl decl : whereExpr.decls)
//			decl.accept(this, mode);
		symbTable.newScope();
		whereExpr.decls.accept(this, mode);
		whereExpr.expr.accept(this, mode);
//			whereExpr.decls.accept(this, mode);
		symbTable.oldScope();
		return null;
	}
}
