
package dk.brics.string.intermediate;

/** Entry point of a method. */
public class MethodHead extends Statement {
    /** The parameter variables for the method */
    public Variable[] params;

    public MethodHead(Variable[] params) {
	this.params = params;
    }

    public void visitBy(StatementVisitor v) {
	v.visitMethodHead(this);
    }
}
