package pins.data.ast.tree;

import pins.common.report.*;
import pins.data.ast.visitor.*;

/**
 * Abstract syntax tree.
 */
public interface AstTree extends Cloneable, Locatable {

	/**
	 * Returns the unique id of this node.
	 * 
	 * @return The unique id of this node.
	 */
	public abstract int id();

	public abstract <Result, Arg> Result accept(AstVisitor<Result, Arg> visitor, Arg arg);

}
