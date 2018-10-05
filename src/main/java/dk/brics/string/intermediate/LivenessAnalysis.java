
package dk.brics.string.intermediate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/** Liveness analysis performed on a set of methods. */
public class LivenessAnalysis implements FlowAnalysis {
    private Map/*<Statement,Set<Variable>>*/ flow_info;

    private WorkList worklist;

    private UsesVisitor uv;
    private DefinesVisitor dv;

    public LivenessAnalysis(Method[] methods) {
	flow_info = new HashMap();
	uv = new UsesVisitor();
	dv = new DefinesVisitor();
	worklist = new WorkList(this);
	for (int i = 0 ; i < methods.length ; i++) {
	    worklist.addAll(methods[i]);
	}
	worklist.iterate();
    }

    public void transfer(Statement s) {
	Set/*<Variable>*/ after = getInfoAfter(s);
	Set/*<Variable>*/ live = new HashSet(after);
	live.removeAll(dv.definedVars(s));
	live.addAll(uv.usedVars(s));
	Iterator si = s.getPreds().iterator();
	while (si.hasNext()) {
	    Statement ps = (Statement)si.next();
	    Set/*<Variable>*/ before = getInfoAfter(ps);
	    if (before.addAll(live)) {
		worklist.add(ps);
	    }
	}
    }

    /** Returns the set of live variables as inferred just after the given statement.
     *  @param s the statement.
     *  @return a set of {@link dk.brics.string.intermediate.Variable} objects.
     */
    public Set/*<Variable>*/ getInfoAfter(Statement s) {
	if (!flow_info.containsKey(s)) {
	    flow_info.put(s, new HashSet());
	}
	return (Set)flow_info.get(s);
    }

}
