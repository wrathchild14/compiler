package pins.phase.seman;

import java.util.*;

import pins.common.report.*;
import pins.data.ast.tree.*;
import pins.data.ast.tree.decl.*;
import pins.data.ast.tree.expr.*;
import pins.data.ast.tree.stmt.*;
import pins.data.ast.tree.type.*;
import pins.data.ast.visitor.*;
import pins.data.imc.code.expr.ImcCONST;
import pins.data.mem.MemRelAccess;
import pins.data.typ.*;
import pins.phase.memory.Memory;


/**
 * Type resolver.
 * <p>
 * Type resolver computes the values of {@link SemAn#declaresType},
 * {@link SemAn#isType}, and {@link SemAn#ofType}.
 */
public class TypeResolver extends AstFullVisitor<SemType, TypeResolver.Mode> {

	public enum Mode {
		HEAD, BODY
	}

	private boolean equiv(SemType type1, SemType type2, HashMap<SemType, HashSet<SemType>> equivs) {

		if ((type1 instanceof SemName) && (type2 instanceof SemName)) {
			if (equivs == null)
				equivs = new HashMap<SemType, HashSet<SemType>>();

			if (equivs.get(type1).contains(type2) && equivs.get(type2).contains(type1))
				return true;
			else {
				HashSet<SemType> types;
				types = equivs.get(type1);
				types.add(type2);
				equivs.put(type1, types);
				types = equivs.get(type2);
				types.add(type1);
				equivs.put(type2, types);
			}
		}

		type1 = type1.actualType();
		type2 = type2.actualType();

		if (type1 instanceof SemVoid)
			return (type2 instanceof SemVoid);
		if (type1 instanceof SemBool)
			return (type2 instanceof SemBool);
		if (type1 instanceof SemChar)
			return (type2 instanceof SemChar);
		if (type1 instanceof SemInt)
			return (type2 instanceof SemInt);

		if (type1 instanceof SemArr) {
			if (!(type2 instanceof SemArr))
				return false;
			SemArr arr1 = (SemArr) type1;
			SemArr arr2 = (SemArr) type2;
			if (arr1.numElems != arr2.numElems)
				return false;
			return equiv(arr1.elemType, arr2.elemType, equivs);
		}

		if (type1 instanceof SemPtr) {
			if (!(type2 instanceof SemPtr))
				return false;
			SemPtr ptr1 = (SemPtr) type1;
			SemPtr ptr2 = (SemPtr) type2;
			if ((ptr1.baseType.actualType() instanceof SemVoid) || (ptr2.baseType.actualType() instanceof SemVoid))
				return true;
			return equiv(ptr1.baseType, ptr2.baseType, equivs);
		}

		throw new InternalError();
	}

	public SemType visit(AstTrees<? extends AstTree> trees, Mode mode) {
		for (AstTree tree : trees)
			if (tree != null)
				tree.accept(this, Mode.HEAD);
		for (AstTree tree : trees)
			if (tree != null)
				tree.accept(this, Mode.BODY);
		return null;
	}

	public SemType visit(AstTypeDecl typeDecl, Mode mode) {
		if (mode == Mode.HEAD) {
			SemAn.declaresType.put(typeDecl, new SemName(typeDecl.name));
		} else if (mode == Mode.BODY) {
//			SemType type = typeDecl.type.accept(this, mode);
//			SemAn.isType.put(typeDecl.type, type);
//			SemAn.declaresType.get(typeDecl).define(type);

			SemAn.declaresType.get(typeDecl).define(typeDecl.type.accept(this, mode));

		}
//		return typeDecl.type.accept(this, mode);
		return null;
	}

	public SemType visit(AstVarDecl varDecl, Mode mode) {
		return varDecl.type.accept(this, mode);
	}

	public SemType visit(AstAtomType atomType, Mode mode) {
//		if (mode == Mode.BODY) {
		SemType newType = null;
		switch (atomType.type) {
			case INT -> newType = new SemInt();
			case BOOL -> newType = new SemBool();
			case CHAR -> newType = new SemChar();
			case VOID -> newType = new SemVoid();
		}
		SemAn.isType.put(atomType, newType);
//		}
		return newType;
	}

	public SemType visit(AstNameExpr nameExpr, Mode mode) {
		// temp solution pls
		SemType type = new SemInt();
		AstDecl a = SemAn.declaredAt.get(nameExpr);
		if (a != null) {
			type = a.accept(this, mode);
		}
		SemAn.ofType.put(nameExpr, type);
//		AstParDecl parDecl;
//		AstVarDecl varDecl;
//
//		try {
//			parDecl = (AstParDecl) SemAn.declaredAt.get(nameExpr);
//			type = parDecl.type.accept(this, mode);
//			SemAn.ofType.put(nameExpr, type);
//		} catch (ClassCastException|NullPointerException e) {
//			varDecl = (AstVarDecl) SemAn.declaredAt.get(nameExpr);
//			type = varDecl.type.accept(this, mode);
//			SemAn.ofType.put(nameExpr, type);
//		}

		return type;
	}

	public SemType visit(AstNameType nameType, Mode mode) {
		if (mode == Mode.BODY) {
			AstTypeDecl decl = (AstTypeDecl) SemAn.declaredAt.get(nameType);
			SemType type = SemAn.declaresType.get(decl);
			SemAn.isType.put(nameType, type);
			return type;
		}
		return null;
	}

	public SemType visit(AstFunDecl funDecl, Mode mode) {
		if (mode == Mode.BODY) {
			SemType type = funDecl.pars.accept(this, mode);
			SemType type2 = funDecl.type.accept(this, mode);
			if (funDecl.expr != null)
				funDecl.expr.accept(this, mode);
			return type2;
		}
		return null;
	}

	public SemType visit(AstAtomExpr atomExpr, Mode mode) {
//		if (mode == Mode.BODY) {
		SemType newType = null;
		switch (atomExpr.type) {
			case INT -> newType = new SemInt();
			case BOOL -> newType = new SemBool();
			case CHAR -> newType = new SemChar();
			case VOID -> newType = new SemVoid();
		}
		SemAn.ofType.put(atomExpr, newType);
		return newType;
//		}
	}

	public SemType visit(AstCallExpr callExpr, Mode mode) {
		AstFunDecl decl = (AstFunDecl) SemAn.declaredAt.get(callExpr);
		SemType type = decl.type.accept(this, mode).actualType();
		for (AstExpr expr : callExpr.args)
			if (expr != null)
				expr.accept(this, mode);
//		callExpr.args.accept(this, mode);
		SemAn.ofType.put(callExpr, type);
		return type;
	}

	public SemType visit(AstBinExpr binExpr, Mode mode) {
		SemType first = binExpr.fstExpr.accept(this, mode);
		if (first != null) first = first.actualType();
		SemType second = binExpr.sndExpr.accept(this, mode);
		if (second != null) second = second.actualType();
		SemAn.ofType.put(binExpr, first);
		return first;
	}

	public SemType visit(AstIfStmt ifStmt, Mode mode) {
		ifStmt.cond.accept(this, mode);
		if (ifStmt.elseStmt != null)
			ifStmt.elseStmt.accept(this, mode);
		ifStmt.thenStmt.accept(this, mode);
		return new SemVoid();
	}

	public SemType visit(AstWhileStmt whileStmt, Mode mode) {
		whileStmt.bodyStmt.accept(this, mode);
		whileStmt.cond.accept(this, mode);
		return new SemVoid();
	}

	// ???
	public SemType visit(AstAssignStmt assignStmt, Mode mode) {
		SemType dst = assignStmt.dst.accept(this, mode);
		if (dst != null) dst.actualType();
		SemType src = assignStmt.src.accept(this, mode);
		if (src != null) src.actualType();
		return new SemVoid();
	}

	public SemType visit(AstStmtExpr stmtExpr, Mode mode) {
		for (AstTree stmt : stmtExpr.stmts)
			stmt.accept(this, mode);
		return new SemVoid();
	}

	public SemType visit(AstParDecl parDecl, Mode mode) {
		return parDecl.type.accept(this, mode);
	}

	public SemType visit(AstCompDecl compDecl, Mode mode) {
		return compDecl.type.accept(this, mode);
	}

	public SemType visit(AstExprStmt exprStmt, Mode mode) {
		if (exprStmt.expr != null)
			exprStmt.expr.accept(this, mode);
		return new SemVoid();
	}

//	public SemType visit(AstCastExpr castExpr, Mode mode) {
//		return super.visit(castExpr, mode);
//	}

	public SemType visit(AstWhereExpr whereExpr, Mode mode) {
//		if (mode == Mode.HEAD) {
//			for (AstTree decl : whereExpr.decls) {
//				decl.accept(this, mode);
//			}
//		if (mode == Mode.HEAD)
//		whereExpr.decls.accept(this, mode);
		for (AstDecl decl : whereExpr.decls)
			decl.accept(this, mode);
		whereExpr.expr.accept(this, mode);
//		}
		return new SemVoid();
	}
}
