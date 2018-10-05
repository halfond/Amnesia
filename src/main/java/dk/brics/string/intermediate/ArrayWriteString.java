
package dk.brics.string.intermediate;

/** Writing a string into an array. */
public class ArrayWriteString extends ArrayStatement {
    /** The string being written */
    public Variable from;

    public ArrayWriteString(Variable to, Variable from) {
	super(to);
	this.from = from;
    }

    public void visitBy(StatementVisitor v) {
	v.visitArrayWriteString(this);
    }

}
