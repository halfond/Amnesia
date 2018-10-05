
package dk.brics.string.intermediate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** Alias analysis performed on a set of methods. */
public class AliasAnalysis implements FlowAnalysis, StatementVisitor {
    private Map/*<Statement,AliasInfo>*/ flow_info;

    private LivenessAnalysis la;

    private AliasInfo before;
    private AliasInfo after;

    private boolean changed;

    private WorkList worklist;

    public AliasAnalysis(Method[] methods, LivenessAnalysis la) {
	this.la = la;
	flow_info = new HashMap();
	worklist = new WorkList(this);
	for (int i = 0 ; i < methods.length ; i++) {
	    worklist.addAll(methods[i]);
	}
	worklist.iterate();
    }

    public void transfer(Statement s) {
	before = getInfoBefore(s);

	// For every successor
	Iterator si = s.getSuccs().iterator();
	while (si.hasNext()) {
	    Statement ss = (Statement)si.next();
	    after = getInfoBefore(ss);
	    changed = false;
	    s.visitBy(this);
	    if (changed) worklist.add(ss);
	}
    }

    /** Returns the alias information as inferred just before the given statement.
     *  @param s the statement.
     *  @return the alias information.
     */
    public AliasInfo getInfoBefore(Statement s) {
	if (!flow_info.containsKey(s)) {
	    flow_info.put(s, new AliasInfo(la.getInfoAfter(s)));
	}
	return (AliasInfo)flow_info.get(s);
    }


    public void visitArrayAssignment(ArrayAssignment s)
    { transferFilter(s.to); transferAssign(s.to, s.from); }
    public void visitArrayCorrupt(ArrayCorrupt s)
    { transferIdentity(); transferCorrupt(s.to); }
    public void visitArrayFromArray(ArrayFromArray s)
    { transferFilter(s.to); transferAssign(s.to, s.from); }
    public void visitArrayNew(ArrayNew s)
    { transferFilter(s.to); }
    public void visitArrayWriteArray(ArrayWriteArray s)
    { transferIdentity(); transferAssign(s.to, s.from); }
    public void visitArrayWriteString(ArrayWriteString s)
    { transferIdentity(); transferAssign(s.to, s.from); }
    public void visitCall(Call s)
    { transferFilter(s.retvar); }
    public void visitMethodHead(MethodHead s)
    { 
	Variable[] pa = s.getMethod().getParamAlias();
	for (int i = 0 ; i < s.params.length ; i ++) {
	    after.addAlias(s.params[i], s.params[i]);
	    if (pa[i] != null) {
		for (int j = 0  ; j < s.params.length ; j++) {
		    if (s.params[i].type == s.params[j].type) {
			after.addAlias(s.params[i], s.params[j]);
			after.addAlias(pa[i], s.params[j]);
			if (i != j) {
			    after.addMaybe(s.params[i], s.params[j]);
			    after.addMaybe(pa[i], s.params[j]);
			}
		    }
		}
	    }
	}
    }
    public void visitNop(Nop s)
    { transferIdentity(); }
    public void visitReturn(Return s)
    { /* Nothing */}
    public void visitStringAssignment(StringAssignment s)
    { transferIdentity(); }
    public void visitStringBufferAppend(StringBufferAppend s)
    { transferIdentity(); }
    public void visitStringBufferAssignment(StringBufferAssignment s)
    { transferFilter(s.to); transferAssign(s.to, s.from); }
    public void visitStringBufferBinaryOp(StringBufferBinaryOp s)
    { transferIdentity(); }
    public void visitStringBufferCorrupt(StringBufferCorrupt s)
    { transferIdentity(); transferCorrupt(s.to); }
    public void visitStringBufferInit(StringBufferInit s)
    { transferFilter(s.to); }
    public void visitStringBufferPrepend(StringBufferPrepend s)
    { transferIdentity(); }
    public void visitStringBufferUnaryOp(StringBufferUnaryOp s)
    { transferIdentity(); }
    public void visitStringConcat(StringConcat s)
    { transferIdentity(); }
    public void visitStringFromArray(StringFromArray s)
    { transferIdentity(); }
    public void visitStringFromStringBuffer(StringFromStringBuffer s)
    { transferIdentity(); }
    public void visitStringInit(StringInit s)
    { transferIdentity(); }

    private void transferIdentity() {
	changed |= after.mergeIdentity(before);
    }

    private void transferFilter(Variable a) {
	changed |= after.mergeFilter(before, a);
    }

    private void transferAssign(Variable a, Variable b) {
	changed |= after.mergeAssign(before, a, b);
    }

    private void transferCorrupt(Variable a) {
	changed |= after.mergeCorrupt(before, a);
    }

}
