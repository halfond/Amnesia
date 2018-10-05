
package dk.brics.string.intermediate;

/** A simple flow analysis framework.
 *  @see dk.brics.string.intermediate.WorkList
 */
public interface FlowAnalysis {

    /** The transfer function.
     *  @param s the statement to transfer through.
     */
    public void transfer(Statement s);

}
