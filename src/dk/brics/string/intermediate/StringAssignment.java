
package dk.brics.string.intermediate;

/** Assignment between string variables. */
public class StringAssignment extends StringStatement {
    /** The string variable being assigned from */
    public Variable from;

    public StringAssignment(Variable to, Variable from) {
	super(to);
	this.from = from;
    }

    public void visitBy(StatementVisitor v) {
	v.visitStringAssignment(this);
    }

}
