package pins.phase.imcgen;

import java.util.*;

import pins.data.ast.tree.*;
import pins.data.ast.tree.decl.*;
import pins.data.ast.visitor.*;
import pins.data.mem.*;
import pins.phase.memory.*;

public class CodeGenerator extends AstNullVisitor<Object, Stack<MemFrame>> {

	public Object visit(AstTrees<?> trees, Stack<MemFrame> frames) {
		if (frames == null)
			frames = new Stack<MemFrame>();
		for (AstTree tree : trees)
			if (tree instanceof AstFunDecl)
				((AstFunDecl) tree).accept(this, frames);
		return null;
	}

	public Object visit(AstFunDecl funDecl, Stack<MemFrame> frames) {
		frames.push(Memory.frames.get(funDecl));
		if (funDecl.expr != null)
			funDecl.expr.accept(new ExprGenerator(), frames);
		frames.pop();
		return null;
	}

}
