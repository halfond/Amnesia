
package dk.brics.string.intermediate;

/** Prepending a string to a string buffer. */
public class StringBufferPrepend extends StringBufferOperation {
    /** The string to prepend */
    public Variable from;

    public StringBufferPrepend(Variable to, Variable from) {
	super(to);
	this.from = from;
    }

    public void visitBy(StatementVisitor v) {
	v.visitStringBufferPrepend(this);
    }

}

