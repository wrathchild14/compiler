package pins.phase.abstr;

import pins.data.ast.tree.*;
import pins.phase.*;

/**
 * Abstract syntax tree construction.
 */
public class Abstr extends Phase {

	// === STATIC ===

	/** The abstract syntax tree. */
	public static AstTree tree;
	
	// ==============
	
	/**
	 * Phase construction.
	 */
	public Abstr() {
		super("abstr");
	}

}
