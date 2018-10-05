
package dk.brics.string.intermediate;

/** Superclass of all statements that manipulate arrays
 *  or array variables.
 */
public abstract class ArrayStatement extends Statement {
    /** The array variable being manipulated */
    public Variable to;

    public ArrayStatement(Variable to) {
	this.to = to;
    }
}
