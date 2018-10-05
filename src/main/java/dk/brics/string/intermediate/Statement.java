
package dk.brics.string.intermediate;

import java.util.Collection;
import java.util.LinkedList;

/** Superclass of all statements.
 *  <p>
 *  A statements belongs to the body of some method.
 *  It has control flow edges to and from other statements. 
 */
public abstract class Statement {
    private Collection/*<Statement>*/ succs;
    private Collection/*<Statement>*/ preds;
    private Method method;
    private int index;

    public Statement() {
	succs = new LinkedList/*<Statement>*/();
	preds = new LinkedList/*<Statement>*/();
    }

    /** Adds a control flow edge from this statement to the given.
     *  @param s the target statement of the edge.
     */
    public void addSucc(Statement s) {
	succs.add(s);
	s.addPred(this);
    }

    void addPred(Statement s) {
	preds.add(s);
    }

    /** Returns all targets of control flow edges
     *  originating from this node.
     *  @return a collection of {@link dk.brics.string.intermediate.Statement} objects.
     */
    public Collection/*<Statement>*/ getSuccs() {
	return succs;
    }

    /** Returns all origins of control flow edges
     *  going to this node.
     *  @return a collection of {@link dk.brics.string.intermediate.Statement} objects.
     */
    public Collection/*<Statement>*/ getPreds() {
	return preds;
    }

    void setMethod(Method m) {
	method = m;
    }

    /** Returns the method whose body contains this statement.
     *  @return the method.
     */
    public Method getMethod() {
	return method;
    }

    void setIndex(int index) {
	this.index = index;
    }

    /** Returns the index of this statement, indicating the sequence
     *  number in which the statement was added to its method.
     *  @return the index.
     */
    public int getIndex() {
	return index;
    }

    /** Returns a string representation of this statement.
     *  This is handled by a {@link dk.brics.string.intermediate.ToStringVisitor}.
     *  @return the statement as a string.
     */
    public String toString() {
	ToStringVisitor tsv = new ToStringVisitor();
	visitBy(tsv);
	return tsv.result;
    }

    /** Visit this statement by the given statement visitor.
     *  This will invoke the corresponding method in the visitor.
     *  @param v the visitor.
     */
    public abstract void visitBy(StatementVisitor v);
}
