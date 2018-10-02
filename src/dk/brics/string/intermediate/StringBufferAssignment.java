
package dk.brics.string.intermediate;

/** Assignment between string buffer variables. */
public class StringBufferAssignment extends StringBufferStatement {
    /** The string buffer variable being assigned from. */
    public Variable from;

    public StringBufferAssignment(Variable to, Variable from) {
	super(to);
	this.from = from;
    }

    public void visitBy(StatementVisitor v) {
	v.visitStringBufferAssignment(this);
    }

}

