
package dk.brics.string.intermediate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import dk.brics.automaton.Automaton;
import dk.brics.string.flow.Graph;
import dk.brics.string.flow.Node;
import dk.brics.string.operations.Basic;

/** Translates a set of methods into a flow graph with no edges.
 *  Edges are added by a {@link dk.brics.string.intermediate.FlowGraphConnector}.
 */
public class Translator implements StatementVisitor {
    private Graph g;
    private Map/*<Statement,Map<Variable,Node>>*/ map;
    private Map/*<Statement,Node>*/ trans_map;

    private AliasAnalysis aa;
    private DefinesVisitor dv;
    private DefinesVisitor dva;
    private Automaton empty = Basic.makeNoString();
    private Automaton any = Basic.makeAnyString();

    private Statement s;
    private Variable v;

    public Translator(Method[] methods, AliasAnalysis aa) {
	this.aa = aa;
	dv = new DefinesVisitor();
	dva = new DefinesVisitor(aa);
	g = new Graph();
	map = new HashMap();
	trans_map = new HashMap();

	for (int i = 0 ; i < methods.length ; i++) {
	    Method m = methods[i];
	    // For all statements
	    Iterator si = m.getStatements().iterator();
	    while (si.hasNext()) {
		s = (Statement)si.next();
		translate();
	    }
	}
    }

    public Graph getGraph() {
	return g;
    }

    public Map/*<Statement,Map<Variable,Node>>*/ getMap() {
	return map;
    }

    public Map/*<Statement,Node>*/ getTranslationMap() {
	return trans_map;
    }

    private void translate() {
	map.put(s, new HashMap());
	Iterator dvi = dva.definedVars(s).iterator();
	while (dvi.hasNext()) {
	    v = (Variable)dvi.next();
	    s.visitBy(this);
	    if (dv.defines(s, v)) {
		trans_map.put(s, ((Map)map.get(s)).get(v));
	    }
	}
    }

    private void addNode(Node n) {
	((Map)map.get(s)).put(v, n);
    }


    // Called for every defined variable

    public void visitArrayAssignment(ArrayAssignment s)
    { addNode(g.addAssignmentNode()); }

    public void visitArrayCorrupt(ArrayCorrupt s)
    { addNode(g.addInitializationNode(any)); }

    public void visitArrayFromArray(ArrayFromArray s)
    { addNode(g.addAssignmentNode()); }

    public void visitArrayNew(ArrayNew s)
    { addNode(g.addInitializationNode(empty)); }

    public void visitArrayWriteArray(ArrayWriteArray s)
    { addNode(g.addAssignmentNode()); }

    public void visitArrayWriteString(ArrayWriteString s)
    { addNode(g.addAssignmentNode()); }

    public void visitCall(Call s)
    { addNode(g.addAssignmentNode()); }

    public void visitMethodHead(MethodHead s)
    { addNode(g.addAssignmentNode()); }

    public void visitNop(Nop s)
    { /* Never called */ }

    public void visitReturn(Return s)
    { /* Never called */ }

    public void visitStringAssignment(StringAssignment s)
    { addNode(g.addAssignmentNode()); }

    public void visitStringBufferAppend(StringBufferAppend s)
    { addNode(g.addConcatenationNode()); }

    public void visitStringBufferAssignment(StringBufferAssignment s)
    { addNode(g.addAssignmentNode()); }

    public void visitStringBufferBinaryOp(StringBufferBinaryOp s)
    { addNode(g.addBinaryNode(s.op)); }

    public void visitStringBufferCorrupt(StringBufferCorrupt s)
    { addNode(g.addInitializationNode(any)); }

    public void visitStringBufferInit(StringBufferInit s)
    { addNode(g.addAssignmentNode()); }

    public void visitStringBufferPrepend(StringBufferPrepend s)
    { addNode(g.addConcatenationNode()); }

    public void visitStringBufferUnaryOp(StringBufferUnaryOp s)
    { addNode(g.addUnaryNode(s.op)); }

    public void visitStringConcat(StringConcat s)
    { addNode(g.addConcatenationNode()); }

    public void visitStringFromArray(StringFromArray s) {
	if (aa.getInfoBefore(s).isCorrupt(s.from)) {
	    addNode(g.addInitializationNode(any));
	} else {
	    addNode(g.addAssignmentNode());
	}
    }

    public void visitStringFromStringBuffer(StringFromStringBuffer s) {
	if (aa.getInfoBefore(s).isCorrupt(s.from)) {
	    addNode(g.addInitializationNode(any));
	} else {
	    addNode(g.addAssignmentNode());
	}
    }

    public void visitStringInit(StringInit s)
    { addNode(g.addInitializationNode(s.regexp)); }

}
