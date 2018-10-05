
package dk.brics.string.intermediate;

/** Calling another method. */
public class Call extends Statement {
    /** The variable into which the returned value is written */
    public Variable retvar;
    /** The target method */
    public Method target;
    /** The arguments given */
    public Variable[] args;

    public Call(Variable retvar, Method target, Variable[] args) {
	this.retvar = retvar;
	this.target = target;
	this.args = args;
    }

    public void visitBy(StatementVisitor v) {
	v.visitCall(this);
    }
}
