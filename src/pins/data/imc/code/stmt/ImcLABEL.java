package pins.data.imc.code.stmt;

import pins.common.logger.*;
import pins.data.imc.visitor.*;
import pins.data.mem.*;

/**
 * Label.
 * 
 * Does nothing.
 */
public class ImcLABEL extends ImcStmt {

	/** The label. */
	public MemLabel label;

	/**
	 * Constructs a label.
	 * 
	 * @param label The label.
	 */
	public ImcLABEL(MemLabel label) {
		this.label = label;
	}

	@Override
	public <Result, Arg> Result accept(ImcVisitor<Result, Arg> visitor, Arg accArg) {
		return visitor.visit(this, accArg);
	}

	@Override
	public void log(Logger logger) {
		logger.begElement("imc");
		logger.addAttribute("instruction", "LABEL(" + label.name + ")");
		logger.endElement();
	}

	@Override
	public String toString() {
		return "LABEL(" + label.name + ")";
	}

}
