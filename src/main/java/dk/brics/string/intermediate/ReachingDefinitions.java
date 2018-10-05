
package dk.brics.string.intermediate;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/** Reaching definitions analysis performed on a set of methods. */
public class ReachingDefinitions {
    private Map/*<Statement,Map<Variable,Set<Statement>>>*/ flow_info;

    public ReachingDefinitions(Method[] methods, LivenessAnalysis la, AliasAnalysis aa) {
	flow_info = new HashMap();
	DefinesVisitor dvs = new DefinesVisitor(aa, true);
	DefinesVisitor dva = new DefinesVisitor(aa, false);

	// For all methods
	for (int i = 0 ; i < methods.length ; i++) {
	    Method m = methods[i];
	    // For all statements
	    Iterator si = m.getStatements().iterator();
	    while (si.hasNext()) {
		Statement s = (Statement)si.next();
		// For all defined variables
		Iterator vi = dva.definedVars(s).iterator();
		while (vi.hasNext()) {
		    Variable v = (Variable)vi.next();
		    LinkedList queue = new LinkedList();
		    Set seen = new HashSet();
		    queue.addAll(s.getSuccs());
		    while (!queue.isEmpty()) {
			Statement ss = (Statement)queue.removeFirst();
			if (!seen.contains(ss)) {
			    seen.add(ss);
			    putDef(ss, v, s);
			    if (!dvs.defines(ss, v) && la.getInfoAfter(ss).contains(v)) {
				queue.addAll(ss.getSuccs());
			    }
			}
		    }
		}
	    }
	}

    }

    private void putDef(Statement s, Variable v, Statement def) {
	if (!flow_info.containsKey(s)) {
	    flow_info.put(s, new HashMap());
	}
	if (!((Map)flow_info.get(s)).containsKey(v)) {
	    ((Map)flow_info.get(s)).put(v, new HashSet());
	}
	((Set)((Map)flow_info.get(s)).get(v)).add(def);
    }


    /** Returns the set of definitions of the given variable reaching the given statement.
     *  @param s the reached statement.
     *  @param v the variable.
     *  @return a set of {@link dk.brics.string.intermediate.Statement} objects.
     */
    public Set/*<Statement>*/ getReachingDefs(Statement s, Variable v) {
	if (flow_info.containsKey(s)) {
	    if (((Map)flow_info.get(s)).containsKey(v)) {
		return (Set)((Map)flow_info.get(s)).get(v);
	    }
	}
	return Collections.EMPTY_SET;
    }

}
