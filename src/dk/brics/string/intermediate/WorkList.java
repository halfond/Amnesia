
package dk.brics.string.intermediate;

import java.util.LinkedList;

/** A worklist algorithm for performing flow analyses on
 *  a set of methods.
 */
public class WorkList {
    private LinkedList/*<Statement>*/ list;
    private FlowAnalysis fa;

    /** Creates a worklist for the given analysis.
     *  @param fa the flow analysis.
     */
    public WorkList(FlowAnalysis fa) {
	list = new LinkedList();
	this.fa = fa;
    }

    /** Adds all statements from the body of the given method
     *  to the worklist.
     *  @param m the method.
     */
    public void addAll(Method m) {
	list.addAll(m.getStatements());
    }

    /** Adds the given statement to the worklist.
     *  @param s the statement.
     */
    public void add(Statement s) {
	list.add(s);
    }

    /** Iterate through the worklist until no more statements remain.
     *  <p>
     *  In each iteration step, the {@link dk.brics.string.intermediate.FlowAnalysis#transfer transfer}
     *  method in the associated {@link dk.brics.string.intermediate.FlowAnalysis}
     *  is called, which will in turn call the {@link dk.brics.string.intermediate.WorkList#add add}
     *  method in the worklist for all statements affected by the change.
     */
    public void iterate() {
	// TODO: Better worklist strategy
	while (!list.isEmpty()) {
	    Statement s = (Statement)list.removeFirst();
	    fa.transfer(s);
	}
    }

}
