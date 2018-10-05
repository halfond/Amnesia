
package dk.brics.string.intermediate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/** A Java method.
 *  <p>
 *  A method has a body consisting of a list of statements, the first of which
 *  is called the entry statement. The statements are connected by
 *  control flow edges, forming a directed graph.
 */
public class Method {
    private List/*<Statement>*/ sl;
    private Set/*<Return>*/ rs;
    private List/*<Call>*/ sites;

    private String name;
    private MethodHead entry;
    private Variable[] param_alias;

    /** Creates a new method with the given name and parameters.
     *  Only parameters of relevant types are represented.
     *  A {@link dk.brics.string.intermediate.MethodHead} object is created
     *  as the entry point for the method, inheriting the parameters.
     *  @param name the name of the method.
     *  @param params the parameter variables for the method.
     */
    public Method(String name, Variable[] params) {
	this.name = name;
	entry = new MethodHead(params);
	sl = new ArrayList();
	rs = new HashSet();
	sites = new LinkedList();
	param_alias = new Variable[params.length];
	for (int i = 0 ; i < params.length ; i++) {
	    int type = params[i].type;
	    switch (type) {
	    case Variable.TYPE_STRINGBUFFER:
	    case Variable.TYPE_ARRAY:
		param_alias[i] = new Variable(type);
		break;
	    }
	}
	addStatement(entry);
    }

    /** Adds the given statement to the list of statements for this method.
     *  @param s the statement to add.
     */
    public void addStatement(Statement s) {
	s.setIndex(sl.size());
	sl.add(s);
	s.setMethod(this);
	if (s instanceof Return) {
	    rs.add(s);
	}
	if (s instanceof Call) {
	    ((Call)s).target.sites.add(s);
	}
    }

    /** Removes the given {@link dk.brics.string.intermediate.Nop}
     *  statement from the body of this method. All control flow
     *  edges are updated accordingly.
     *  @param s the <code>Nop</code> statement to remove.
     */
    public void removeNop(Nop s) {
	Collection succs = s.getSuccs();
	Collection preds = s.getPreds();
	Iterator si = succs.iterator();
	while (si.hasNext()) {
	    Statement succ = (Statement) si.next();
	    succ.getPreds().remove(s);
	    succ.getPreds().addAll(preds);
	}
	Iterator pi = preds.iterator();
	while (pi.hasNext()) {
	    Statement pred = (Statement) pi.next();
	    pred.getSuccs().remove(s);
	    pred.getSuccs().addAll(succs);
	}
	sl.remove(s);
    }

    /** Returns a list of all call sites calling this method.
     *  @return a list of {@link dk.brics.string.intermediate.Call} objects.
     */
    public List/*<Call>*/ getCallSites() {
	return sites;
    }

    /** Returns the entry point of this method.
     *  @return the entry point.
     */
    public MethodHead getEntry() {
	return entry;
    }

    /** Returns the name of this method.
     *  @return the name.
     */
    public String getName() {
	return name;
    }

    /** Returns the list of statements for this method.
     *  @return a list of {@link dk.brics.string.intermediate.Statement}
     *          objects.
     */
    public List/*<Statement>*/ getStatements() {
	return sl;
    }

    /** Returns the list of return points for this method.
     *  @return a list of {@link dk.brics.string.intermediate.Return}
     *          objects.
     */
    public Set/*<Return>*/ getReturns() {
	return rs;
    }

    /** Returns the list aliases for the method parameters.
     *  @return alias variables corresponding to the parameters.
     */
    public Variable[] getParamAlias() {
	return param_alias;
    }
}
