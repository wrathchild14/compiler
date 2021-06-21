package pins.phase.memory;

import pins.data.ast.attribute.*;
import pins.data.ast.tree.decl.*;
import pins.data.ast.tree.expr.*;
import pins.data.mem.*;
import pins.phase.*;

/**
 * Computing memory layout: frames and accesses.
 */
public class Memory extends Phase {

	/** Maps function declarations to frames. */
	public static final AstAttribute<AstFunDecl, MemFrame> frames = new AstAttribute<AstFunDecl, MemFrame>(0);

	/** Maps variable declarations to accesses. */
	public static final AstAttribute<AstMemDecl, MemAccess> accesses = new AstAttribute<AstMemDecl, MemAccess>(0);

	/** Maps string constants to accesses. */
	public static final AstAttribute<AstAtomExpr, MemAbsAccess> strings = new AstAttribute<AstAtomExpr, MemAbsAccess>(0);

	/**
	 * Constructs a new phase for computing layout.
	 */
	public Memory() {
		super("memory");
	}

}
