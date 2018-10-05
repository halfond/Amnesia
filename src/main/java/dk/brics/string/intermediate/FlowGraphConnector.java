
package dk.brics.string.intermediate;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import dk.brics.string.flow.AssignmentNode;
import dk.brics.string.flow.BinaryNode;
import dk.brics.string.flow.ConcatenationNode;
import dk.brics.string.flow.Graph;
import dk.brics.string.flow.Node;
import dk.brics.string.flow.UnaryNode;
import dk.brics.string.flow.Use;

/** Creates all flow graph edges for a flow graph created
 *  by a {@link dk.brics.string.intermediate.Translator}.
 */
public class FlowGraphConnector implements StatementVisitor {

    private ReachingDefinitions rd;
    private AliasAnalysis aa;
    private Graph g;
    private Map/*<Statement,Map<Variable,Node>>*/ map;

    private Statement s;
    private Variable v;
    private Node n;

    private LinkedList queue_s;
    private LinkedList queue_v;

    public FlowGraphConnector(Method[] methods, AliasAnalysis aa, ReachingDefinitions rd, Graph g, Map map) {
	this.aa = aa;
	this.rd = rd;
	this.g = g;
	this.map = map;

	queue_s = new LinkedList();
	queue_v = new LinkedList();

	// Link all statements
	for (int i = 0 ; i < methods.length ; i++) {
	    Method m = methods[i];
	    Iterator si = m.getStatements().iterator();
	    while (si.hasNext()) {
		s = (Statement)si.next();
		Iterator vi = ((Map)map.get(s)).keySet().iterator();
		while (vi.hasNext()) {
		    v = (Variable)vi.next();
		    n = (Node)((Map)map.get(s)).get(v);

		    s.visitBy(this);
		}
	    }
	}

    }

    private void link(Use use, Statement s, Variable var) {
	Iterator dsi = rd.getReachingDefs(s, var).iterator();
	while (dsi.hasNext()) {
	    Statement ds = (Statement)dsi.next();
	    Node n = (Node)((Map)map.get(ds)).get(var);
	    use.addDef(n);
	}
    }

    // Linking a statement to its definitions
    // Given s: Statement, v: Variable, n: Node

    public void visitArrayAssignment(ArrayAssignment s)
    { link(((AssignmentNode)n).getArg(), s, s.from); }
    public void visitArrayCorrupt(ArrayCorrupt s)
    {}
    public void visitArrayFromArray(ArrayFromArray s)
    { link(((AssignmentNode)n).getArg(), s, s.from); }
    public void visitArrayNew(ArrayNew s)
    {}
    public void visitArrayWriteArray(ArrayWriteArray s)
    { link(((AssignmentNode)n).getArg(), s, v); link(((AssignmentNode)n).getArg(), s, s.from); }
    public void visitArrayWriteString(ArrayWriteString s)
    { link(((AssignmentNode)n).getArg(), s, v); link(((AssignmentNode)n).getArg(), s, s.from); }
    public void visitCall(Call s) {
	// Link returns to the defined vars
	if (v == s.retvar) {
	    // Link returns to the return variable
	    Iterator ri = s.target.getReturns().iterator();
	    while (ri.hasNext()) {
		Return r = (Return)ri.next();
		link(((AssignmentNode)n).getArg(), r, r.retvar);
	    }
	} else {
	    // Link returns to the alias variables
	    Variable[] pa = s.target.getParamAlias();
	    Set/*<Variable>*/ va = aa.getInfoBefore(s).getAliasesFor(v);
	    for (int i = 0 ; i < s.args.length ; i++) {
		if (va.contains(s.args[i])) {
		    // The variable corresponding to the
		    // current assignment node is aliased
		    // to the i'th argument. Add links from
		    // the i'th alias variable
		    Iterator ri = s.target.getReturns().iterator();
		    while (ri.hasNext()) {
			Return r = (Return)ri.next();
			link(((AssignmentNode)n).getArg(), r, pa[i]);
		    }
		}
	    }

	}
    }
    public void visitMethodHead(MethodHead s) {
	// Link params to args and aliases to params
	Variable[] pa = s.getMethod().getParamAlias();
	for (int i = 0 ; i < pa.length ; i++) {
	    if (v == s.params[i]) {
		Iterator ci = s.getMethod().getCallSites().iterator();
		while (ci.hasNext()) {
		    Call c = (Call)ci.next();
		    link(((AssignmentNode)n).getArg(), c, c.args[i]);
		}
	    }
	    if (v == pa[i]) {
		Variable p = s.params[i];
		AssignmentNode an = (AssignmentNode)((Map)map.get(s)).get(p);
		an.getArg().addDef(n);
	    }
	}
    }
    public void visitNop(Nop s)
    { /* Never called */ }
    public void visitReturn(Return s)
    { /* Never called */ }
    public void visitStringAssignment(StringAssignment s)
    { link(((AssignmentNode)n).getArg(), s, s.from); }
    public void visitStringBufferAppend(StringBufferAppend s)
    { link(((ConcatenationNode)n).getArg1(), s, v); link(((ConcatenationNode)n).getArg2(), s, s.from); }
    public void visitStringBufferAssignment(StringBufferAssignment s)
    { link(((AssignmentNode)n).getArg(), s, s.from); }
    public void visitStringBufferBinaryOp(StringBufferBinaryOp s)
    { link(((BinaryNode)n).getArg1(), s, v); link(((BinaryNode)n).getArg2(), s, s.from); }
    public void visitStringBufferCorrupt(StringBufferCorrupt s)
    {}
    public void visitStringBufferInit(StringBufferInit s)
    { link(((AssignmentNode)n).getArg(), s, s.from); }
    public void visitStringBufferPrepend(StringBufferPrepend s)
    { link(((ConcatenationNode)n).getArg1(), s, s.from); link(((ConcatenationNode)n).getArg2(), s, v); }
    public void visitStringBufferUnaryOp(StringBufferUnaryOp s)
    { link(((UnaryNode)n).getArg(), s, v); }
    public void visitStringConcat(StringConcat s)
    { link(((ConcatenationNode)n).getArg1(), s, s.left); link(((ConcatenationNode)n).getArg2(), s, s.right); }
    public void visitStringFromArray(StringFromArray s) {
	if (n instanceof AssignmentNode) {
	    link(((AssignmentNode)n).getArg(), s, s.from);
	}
    }
    public void visitStringFromStringBuffer(StringFromStringBuffer s) {
	if (n instanceof AssignmentNode) {
	    link(((AssignmentNode)n).getArg(), s, s.from);
	}
    }
    public void visitStringInit(StringInit s)
    {}
}
