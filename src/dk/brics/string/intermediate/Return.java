
package dk.brics.string.intermediate;

/** Return from a method. */
public class Return extends Statement {
    /** The value to return */
    public Variable retvar;

    public Return(Variable retvar) {
	this.retvar = retvar;
    }

    public void visitBy(StatementVisitor v) {
	v.visitReturn(this);
    }
}
