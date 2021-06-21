package pins.phase.synan;

import pins.common.report.*;
import pins.data.ast.tree.*;
import pins.data.ast.tree.decl.*;
import pins.data.ast.tree.expr.*;
import pins.data.ast.tree.stmt.*;
import pins.data.ast.tree.type.*;
import pins.phase.lexan.*;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Vector;

public class SynAn {

    private static Symbol currSymbol = null;
    private static LexAn lexan;

    private void test(Symbol symbol, String s) {
        if (!symbol.getType().equals(s) && !symbol.getType().equals("EOF")) {
            System.out.println("Syntax Error " + symbol.getType() + " on " + symbol.getRow() + " " + symbol.getCharStart() + " : " + symbol.getCharEnd());
            System.out.println("Expected: " + s + ", got: " + symbol.getType());
            System.out.println();
        } else {
            currSymbol = lexan.nextSymbol();
        }
    }

    public AstTree parseProgram(String srcFile) throws FileNotFoundException {
        lexan = new LexAn(srcFile);
        currSymbol = lexan.nextSymbol();
//        System.out.println("Prg -> Decls");
        return parseDecls();
    }

    private AstTree parseDecls() {
//        System.out.println("Decls -> Decl Decls'");
        Vector<AstTree> decls = new Vector<>();
        decls.add(parseDecl());
        decls.addAll(parseDeclsRest());
        return new AstTrees<>(decls);
    }

    private Vector<AstTree> parseDeclsRest() {
        switch (currSymbol.getType()) {
            case "keyTyp":
            case "keyVar":
            case "keyFun":
                Vector<AstTree> decls = new Vector<>();
                decls.add(parseDecl());
                decls.addAll(parseDeclsRest());
                return decls;
            case "EOF":
            case "closeBrace":
//                System.out.println("Decls' ->");
                return new Vector<>();
            default:
//                System.out.println("Decls' -> Stmts ");
                return new Vector<>(parseStmts());
//                return new Vector<>();
        }
    }

    private AstDecl parseDecl() {
        Location location;
        int start, startRow, end, endRow;
        String name;
        AstType type;
        switch (currSymbol.getType()) {
            case "keyTyp":
//                System.out.println("Decl -> typ Id = Type ; ");
                start = currSymbol.getCharStart();
                startRow = currSymbol.getRow();
                test(currSymbol, "keyTyp");
                name = currSymbol.getName();
                test(currSymbol, "id");
                test(currSymbol, "assign");
                type = parseType();
                end = currSymbol.getCharEnd();
                endRow = currSymbol.getRow();
                test(currSymbol, "semic");

                location = new Location(startRow, start, endRow, end);
                return new AstTypeDecl(location, name, type);
            case "keyVar":
//                System.out.println("Decl -> var Id : Type ; ");
                start = currSymbol.getCharStart();
                startRow = currSymbol.getRow();
                test(currSymbol, "keyVar");
                name = currSymbol.getName();
                test(currSymbol, "id");
                test(currSymbol, "colon");
                type = parseType();
                end = currSymbol.getCharEnd();
                endRow = currSymbol.getRow();
                test(currSymbol, "semic");

                location = new Location(startRow, start, endRow, end);
                return new AstVarDecl(location, name, type);
            case "keyFun":
//                System.out.println("Decl -> fun Id ( Params ) : Type = Expr ;");
                start = currSymbol.getCharStart();
                startRow = currSymbol.getRow();
                test(currSymbol, "keyFun");
                name = currSymbol.getName();
                test(currSymbol, "id");
                test(currSymbol, "openParent");
                AstTrees<AstParDecl> pars = parseParams();
                test(currSymbol, "closeParent");
                test(currSymbol, "colon");
                type = parseType();
                test(currSymbol, "assign");
                AstExpr expr = parseExpr();
                end = currSymbol.getCharEnd();
                endRow = currSymbol.getRow();
                test(currSymbol, "semic");

                location = new Location(startRow, start, endRow, end);
                return new AstFunDecl(location, name, pars, type, expr);
            default:
                System.out.println("Error");
                return null;
        }
    }

    private AstTrees<AstParDecl> parseParams() {
//        System.out.println("Params -> Param Params'");
        Vector<AstParDecl> params = new Vector<>();
        int start = currSymbol.getCharStart(), startRow = currSymbol.getRow();
        params.add(parseParam());
        params.addAll(parseParamsRest());
        int end = currSymbol.getCharEnd() - 1, endRow = currSymbol.getRow();
        Location location = new Location(startRow, start, endRow, end);
        return new AstTrees<>(location, params);
    }

    private Vector<AstParDecl> parseParamsRest() {
        switch(currSymbol.getType()) {
            case "comma":
                Vector<AstParDecl> params = new Vector<>();
//                System.out.println("Params' -> , Param Params'");
                test(currSymbol, "comma");
                params.add(parseParam());
                params.addAll(parseParamsRest());
                return params;
            default:
//                System.out.println("Params' -> ");
                return new Vector<>();
        }
    }

    private AstParDecl parseParam() {
        switch (currSymbol.getType()) {
            case "id":
//                System.out.println("Param -> Id : Type");
                String name = currSymbol.getName();
                int start = currSymbol.getCharStart();
                int startRow = currSymbol.getRow();
                int end = currSymbol.getCharEnd();
                int endRow = currSymbol.getRow();
                test(currSymbol, "id");
                test(currSymbol, "colon");
                Location location = new Location(startRow, start, endRow, end);
                return new AstParDecl(location, name, parseType());
            default:
//                System.out.println("Param -> ");
                return null;
        }
    }

    private AstType parseType() {
        Location location;
        switch (currSymbol.getType()) {
            case "keyVoid":
//                System.out.println("Type -> Void ");
                location = new Location(currSymbol.getRow(), currSymbol.getCharStart(), currSymbol.getRow(), currSymbol.getCharEnd());
                test(currSymbol, "keyVoid");
                return new AstAtomType(location, AstAtomType.Type.VOID);
            case "keyChar":
//                System.out.println("Type -> Char ");
                location = new Location(currSymbol.getRow(), currSymbol.getCharStart(), currSymbol.getRow(), currSymbol.getCharEnd());
                test(currSymbol, "keyChar");
                return new AstAtomType(location, AstAtomType.Type.CHAR);
            case "keyInt":
//                System.out.println("Type -> Int ");
                location = new Location(currSymbol.getRow(), currSymbol.getCharStart(), currSymbol.getRow(), currSymbol.getCharEnd());
                test(currSymbol, "keyInt");
                return new AstAtomType(location, AstAtomType.Type.INT);
            case "id":
//                System.out.println("Type -> Id ");
                location = new Location(currSymbol.getRow(), currSymbol.getCharStart(), currSymbol.getRow(), currSymbol.getCharEnd());
                String name = currSymbol.getName();
                test(currSymbol, "id");
                return new AstNameType(location, name);
            case "openBracket":
//                System.out.println("Type -> [ Expr ] Type");
                test(currSymbol, "openBracket");
                parseExpr();
                test(currSymbol, "closeBracket");
                parseType();
                return null;
            case "hat":
//                System.out.println("Type -> ^ Type " );
                int start = currSymbol.getCharStart();
                int startRow = currSymbol.getRow();
                test(currSymbol, "hat");
                AstType type = parseType();
                int end = currSymbol.getCharEnd();
                int endRow = currSymbol.getRow();
                location = new Location(startRow, start, endRow, end);
                return new AstPtrType(location, type);
            case "openParent":
//                System.out.println("Type -> ( Type )");
                test(currSymbol, "openParent");
                parseType();
                test(currSymbol, "closeParent");
                return null;
            default:
                System.out.println("Error");
                return null;
        }
    }

    private Vector<AstStmt> parseStmts() {
//        System.out.println("Stmts -> Stmt Stmts'");
        Vector<AstStmt> stmts = new Vector<>();
        stmts.add(parseStmt());
        stmts.addAll(parseStmtsRest());
        return stmts;
    }
 
    // FIX THIS
    private AstStmt transform(Vector<AstStmt> stmts) {
        return new AstExprStmt(new Location(currSymbol.getRow(), currSymbol.getCharStart()), new AstStmtExpr(new Location(currSymbol.getRow(), currSymbol.getCharStart()), new AstTrees<>(stmts)));
    }

    private Vector<AstStmt> parseStmtsRest() {
        switch (currSymbol.getType()) {
            case "semic":
//                System.out.println("Stmts' -> ; Stmts");
                test(currSymbol, "semic");
                return new Vector<>(parseStmts());
            default:
//                System.out.println("Stmts' ->");
                return new Vector<>();
        }
    }

    private AstStmt parseStmtRest(AstExpr expr) {
        switch (currSymbol.getType()) {
            case "assign":
//                System.out.println("Stmt' -> = Expr");
                test(currSymbol, "assign");
                return new AstAssignStmt(new Location(currSymbol.getRow(), currSymbol.getCharStart()), expr, parseExpr());
            default:
                return new AstExprStmt(new Location(currSymbol.getRow(), currSymbol.getCharStart()), expr);
        }
    }

    private AstStmt parseStmt() {
        AstExpr expr;
        AstStmt stmt;
        switch (currSymbol.getType()) {
            case "keyIf":
//                System.out.println("Stmt -> if Expr then Stmt Stmts Stmt''");
                test(currSymbol, "keyIf");
                expr = parseExpr();
                test(currSymbol, "keyThen");
                return new AstIfStmt(new Location(currSymbol.getRow(), currSymbol.getCharStart()), expr, transform(parseStmts()), parseStmtRestRest());
            case "keyWhile":
//                System.out.println("Stmt -> while Expr do Stmt Stmts end ;");
                test(currSymbol, "keyWhile");
                expr = parseExpr();
                test(currSymbol, "keyDo");
                stmt = new AstWhileStmt(new Location(currSymbol.getRow(), currSymbol.getCharStart()), expr, transform(parseStmts()));
                test(currSymbol, "keyEnd");
                return stmt;
            default:
//                System.out.println("Stmt -> Expr Stmt'");
                return parseStmtRest(parseExpr());
        }
    }

    private AstStmt parseStmtRestRest() {
        AstStmt stmt;
        switch (currSymbol.getType()) {
            case "keyEnd":
//                System.out.println("Stmt'' -> end ;");
                test(currSymbol, "keyEnd");
                test(currSymbol, "semic");
                return null;
            case "keyElse":
//                System.out.println("Stmt'' -> else Stmt Stmts end ;");
                test(currSymbol, "keyElse");
                stmt = transform(parseStmts());
                test(currSymbol, "keyEnd");
                test(currSymbol, "semic");
                return stmt;
            default:
                return null;
        }
    }

    private AstExpr parseExpr() {
        switch (currSymbol.getType()) {
            case "openBrace":
//                System.out.println("Expr -> { Stmt Stmts }");
                test(currSymbol, "openBrace");
                Vector<AstStmt> stmts = new Vector<>();
                stmts.add(parseStmt());
                stmts.addAll(parseStmts());
                test(currSymbol, "closeBrace");

                return new AstStmtExpr(new Location(currSymbol.getRow(), currSymbol.getCharStart()), new AstTrees<>(stmts));
            case "keyNew":
//                System.out.println("Expr -> new Expr");
                test(currSymbol, "keyNew");
                parseExpr();
                break;
            case "keyDel":
//                System.out.println("Expr -> del Expr");
                test(currSymbol, "keyDel");
                parseExpr();
                break;
            case "openParent":
//                System.out.println("Expr -> ( Expr T )");
                test(currSymbol, "openParent");
                AstExpr expr = parseT(parseExpr());
                test(currSymbol, "closeParent");
                return expr;
//                break;
            default:
//                System.out.println("Expr -> Expr0");
                return parseExpr0();
        }
        return null;
    }

    private AstExpr parseT(AstExpr expr) {
        switch (currSymbol.getType()) {
            case "colon":
                test(currSymbol, "colon");
                return new AstCastExpr(new Location(currSymbol.getRow(), currSymbol.getCharStart()), expr, parseType());
            case "keyWhere":
                test(currSymbol, "keyWhere");
                Vector<AstDecl> vec = new Vector<>();
                vec.add(parseDecl());
                vec.addAll(parseDeclsRestSecond());
                return new AstWhereExpr(new Location(currSymbol.getRow(), currSymbol.getCharStart()), expr,
                        new AstTrees<AstDecl>(vec));
            default:
                return null;
        }
    }

    private Vector<AstDecl> parseDeclsRestSecond() {
        switch (currSymbol.getType()) {
            case "keyTyp":
            case "keyVar":
            case "keyFun":
                Vector<AstDecl> decls = new Vector<>();
                decls.add(parseDecl());
                decls.addAll(parseDeclsRestSecond());
                return decls;
            case "EOF":
            case "closeBrace":
//                System.out.println("Decls' ->");
//                return new Vector<>();
            default:
//                System.out.println("Decls' -> Stmts ");
//                return new Vector<>(parseStmts());
                return new Vector<>();
        }
    }

    private void parseT() {
        switch (currSymbol.getType()) {
            case "colon":
//                System.out.println("T -> : Type");
                test(currSymbol, "colon");
                parseType();
                break;
            case "keyWhere":
//                System.out.println("T -> where Decl");
                test(currSymbol, "keyWhere");
                parseDecl();
                break;
            default:
//                System.out.println("T -> ");
                break;
        }
    }

    private AstExpr parseExpr0() {
//        System.out.println("Expr0 -> ExprRel Expr0'");
        return parseRel();
        // parseExpr0Rest();
    }

    private void parseExpr0Rest() {
        switch (currSymbol.getType()) {
            case "amper":
//                System.out.println("Expr0' -> & ExprRel Expr0'");
                test(currSymbol, "amper");
                parseRel();
                parseExpr0Rest();
                // parseExpr();
                break;
            case "bar":
//                System.out.println("Expr0' -> | ExprRel Expr0'");
                test(currSymbol, "bar");
                parseRel();
                parseExpr0Rest();
                // parseExpr();
                break;
            default:
//                System.out.println("Expr0' ->");
                break;
        }
    }

    private AstExpr parseRel() {
//        System.out.println("ExprRel -> ExprAdd ExprRel'");
        return parseRelRest(parseAdd());
    }

    private AstExpr parseRelRest(AstExpr add) {
        switch (currSymbol.getType()) {
            case "eq":
//                System.out.println("ExprRel' -> == ExprAdd ExprRel'");
                test(currSymbol, "eq");
                return new AstBinExpr(new Location(currSymbol.getRow(), currSymbol.getCharStart()), AstBinExpr.Oper.EQU, add, parseRelRest(parseAdd()));
            case "ne":
//                System.out.println("ExprRel' -> != ExprAdd ExprRel'");
                test(currSymbol, "ne");
                return new AstBinExpr(new Location(currSymbol.getRow(), currSymbol.getCharStart()), AstBinExpr.Oper.NEQ, add, parseRelRest(parseAdd()));
            case "lt":
//                System.out.println("ExprRel' -> < ExprAdd ExprRel'");
                test(currSymbol, "lt");
                return new AstBinExpr(new Location(currSymbol.getRow(), currSymbol.getCharStart()), AstBinExpr.Oper.GTH, add, parseRelRest(parseAdd()));
            case "gt":
//                System.out.println("ExprRel' -> > ExprAdd ExprRel'");
                test(currSymbol, "gt");
                return new AstBinExpr(new Location(currSymbol.getRow(), currSymbol.getCharStart()), AstBinExpr.Oper.LTH, add, parseRelRest(parseAdd()));
            case "le":
//                System.out.println("ExprRel' -> <= ExprAdd ExprRel'");
                test(currSymbol, "le");
                return new AstBinExpr(new Location(currSymbol.getRow(), currSymbol.getCharStart()), AstBinExpr.Oper.LEQ, add, parseRelRest(parseAdd()));
            case "ge":
//                System.out.println("ExprRel' -> >= ExprAdd ExprRel'");
                test(currSymbol, "ge");
                return new AstBinExpr(new Location(currSymbol.getRow(), currSymbol.getCharStart()), AstBinExpr.Oper.GEQ, add, parseRelRest(parseAdd()));
            default:
//                System.out.println("ExprRel' -> ");
                return add;
        }
    }

    private AstExpr parseAdd() {
//        System.out.println("ExprAdd -> ExprMul ExprAdd'");
        return parseAddRest(parseMul());
    }

    private AstExpr parseAddRest(AstExpr mul) {
        switch (currSymbol.getType()) {
            case "add":
//                System.out.println("ExprAdd' -> + ExprMul ExprAdd'");
                test(currSymbol, "add");
                return new AstBinExpr(new Location(currSymbol.getRow(), currSymbol.getCharStart()), AstBinExpr.Oper.ADD, mul, parseAddRest(parseMul()));
            case "sub":
//                System.out.println("ExprAdd' -> - ExprMul ExprAdd");
                test(currSymbol, "sub");
                return new AstBinExpr(new Location(currSymbol.getRow(), currSymbol.getCharStart()), AstBinExpr.Oper.SUB, mul, parseAddRest(parseMul()));
            default:
//                System.out.println("ExprAdd' -> ");
                return mul;
        }
    }

    private AstExpr parseMul() {
//        System.out.println("ExprMul -> Prex ExprMul'");
        return parseMulRest(parsePrex());
    }

    private AstExpr parseMulRest(AstExpr prex) {
        switch (currSymbol.getType()) {
            case "mul":
//                System.out.println("ExprMul' -> * Prex ExprMul'");
                test(currSymbol, "mul");
                return new AstBinExpr(new Location(currSymbol.getRow(), currSymbol.getCharStart()), AstBinExpr.Oper.MUL, prex, parseMulRest(parsePrex()));
            case "div":
//                System.out.println("ExprMul' -> / Prex ExprMul'");
                test(currSymbol, "div");
                return new AstBinExpr(new Location(currSymbol.getRow(), currSymbol.getCharStart()), AstBinExpr.Oper.DIV, prex, parseMulRest(parsePrex()));
            case "percent":
//                System.out.println("ExprMul' -> % Prex ExprMul'");
                test(currSymbol, "percent");
                return new AstBinExpr(new Location(currSymbol.getRow(), currSymbol.getCharStart()), AstBinExpr.Oper.MOD, prex, parseMulRest(parsePrex()));
            default:
//                System.out.println("ExprMul' -> ");
                return prex;
        }
    }

    private AstExpr parsePrex() {
//        System.out.println("Prex -> Prex' Pfx");
//        return parsePrexRest(parsePfx());
        return parsePfx();
    }

    private AstExpr parsePrexRest(AstExpr prexRest) { // ULTRA WRONG
        switch (currSymbol.getType()) {
            case "exclamation":
//                System.out.println("Prex' -> ! Prex'");
                test(currSymbol, "exclamation");
                return new AstPfxExpr(new Location(currSymbol.getRow(), currSymbol.getCharStart()), AstPfxExpr.Oper.NOT, parsePrexRest(prexRest));
            case "add":
//                System.out.println("Prex' -> + Prex'");
                test(currSymbol, "add");
                return new AstPfxExpr(new Location(currSymbol.getRow(), currSymbol.getCharStart()), AstPfxExpr.Oper.ADD, parsePrexRest(prexRest));
            case "sub":
//                System.out.println("Prex' -> - Prex'");
                test(currSymbol, "sub");
                return new AstPfxExpr(new Location(currSymbol.getRow(), currSymbol.getCharStart()), AstPfxExpr.Oper.SUB, parsePrexRest(prexRest));
            case "hat":
//                System.out.println("Prex' -> ^ Prex'");
                test(currSymbol, "hat");
                return new AstPfxExpr(new Location(currSymbol.getRow(), currSymbol.getCharStart()), AstPfxExpr.Oper.PTR, parsePrexRest(prexRest));
            default:
//                System.out.println("Prex' -> ");
                return prexRest;
        }
    }

    private AstExpr parsePfx() {
//        System.out.println("Pfx -> endExpr Pfx'");
        return parseEndExpr();
        // parsePfxRest();
    }

    private void parsePfxRest() {
        switch (currSymbol.getType()) {
            case "openBracket":
//                System.out.println("Pfx' -> [ Expr ] Pfx'");
                test(currSymbol, "openBracket");
                parseExpr();
                test(currSymbol, "closeBracket");
                parsePfxRest();
                break;
            case "hat":
//                System.out.println("Pfx' -> ^ Pfx'");
                test(currSymbol, "hat");
                parsePfxRest();
                break;
            default:
//                System.out.println("Pfx' ->");
                break;
        }
    }

    private AstExpr parseEndExpr() {
        AstExpr expr;
        switch (currSymbol.getType()) {
            case "constNone":
//                System.out.println("endExpr -> constNone");
                expr = new AstAtomExpr(new Location(currSymbol.getRow(), currSymbol.getCharStart(), currSymbol.getRow(), currSymbol.getCharEnd()), AstAtomExpr.Type.VOID, currSymbol.getName());
                test(currSymbol, "constNone");
                return expr;
            case "constNil":
//                System.out.println("endExpr -> constNil");
                test(currSymbol, "constNil");
                return null;
            case "constInt":
//                System.out.println("endExpr -> Integer");
                expr = new AstAtomExpr(new Location(currSymbol.getRow(), currSymbol.getCharStart(), currSymbol.getRow(), currSymbol.getCharEnd()), AstAtomExpr.Type.INT, currSymbol.getName());
                test(currSymbol, "constInt");
                return expr;
            case "constChar":
//                System.out.println("endExpr -> constChar");
                expr = new AstAtomExpr(new Location(currSymbol.getRow(), currSymbol.getCharStart(), currSymbol.getRow(), currSymbol.getCharEnd()), AstAtomExpr.Type.CHAR, currSymbol.getName());
                test(currSymbol, "constChar");
                return expr;
            case "keyVoid":
//                System.out.println("endExpr -> Void");
                expr = new AstAtomExpr(new Location(currSymbol.getRow(), currSymbol.getCharStart(), currSymbol.getRow(), currSymbol.getCharEnd()), AstAtomExpr.Type.VOID, currSymbol.getName());
                test(currSymbol, "keyVoid");
                return expr;
            case "id":
//                System.out.println("endExpr -> Id // expr'");
                expr = new AstNameExpr(new Location(currSymbol.getRow(), currSymbol.getCharStart(), currSymbol.getRow(), currSymbol.getCharEnd()), currSymbol.getName());
                String name = currSymbol.getName();
                test(currSymbol, "id");
                return parseExprRest(expr, name);
            default:
                return null;
        }
    }

    private AstExpr parseExprRest(AstExpr expr, String name) {
        AstExpr returnExpr;
        switch (currSymbol.getType()) {
            case "openParent":
//                System.out.println("Expr' -> ( Exprs )");
                Location location = new Location(currSymbol.getRow(), currSymbol.getCharStart() - 1); // -1 because current symbol now is (
                test(currSymbol, "openParent");
                Vector<AstExpr> vec = new Vector<>(parseExprs());
                returnExpr = new AstCallExpr(location, name, new AstTrees<>(vec));
                test(currSymbol, "closeParent");
                return returnExpr;
            default:
                return expr;
        }
    }

    private Vector<AstExpr> parseExprs() {
//        System.out.println("Exprs -> Expr Exprs'");
        Vector<AstExpr> vec = new Vector<>();
        vec.add(parseExpr());
        vec.addAll(parseExprsRest());
        return vec;
    }

    private Vector<AstExpr> parseExprsRest() {
        Vector<AstExpr> vec = new Vector<>();
        switch (currSymbol.getType()) {
            case "comma":
//                System.out.println("Exprs' -> , Expr Exprs'");
                test(currSymbol, "comma");
                vec.add(parseExpr());
                vec.addAll(parseExprsRest());
                return vec;
            default:
//                System.out.println("Exprs' ->");
                return vec;
        }
    }
}
