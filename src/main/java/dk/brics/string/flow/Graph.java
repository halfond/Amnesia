package dk.brics.string.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.string.grammar.Grammar;
import dk.brics.string.mlfa.BinaryOperation;
import dk.brics.string.mlfa.UnaryOperation;
import dk.brics.string.operations.Basic;

/** 
 * Flow graph. 
 * Nodes represent definitions (variables or expressions). 
 * Edges represent data flow.
 * @author Anders M&oslash;ller &lt;<a href="mailto:amoeller@brics.dk">amoeller@brics.dk</a>&gt;
 * @author Aske Simon Christensen &lt;<a href="mailto:aske@brics.dk">aske@brics.dk</a>&gt;
 */
public class Graph
{
    private Set/*<Node>*/ nodes = new HashSet();

    /** Constructs new empty flow graph. */
    public Graph() {};

    /**
     * Returns set of nodes in this graph. 
     * @return set of <tt>Node</tt> objects, should not be modified by caller
     */
    public Set getNodes()
    {
	return nodes;
    }

    /** 
     * Adds new assignment node to this flow graph. 
     * @return new node
     */
    public AssignmentNode addAssignmentNode()
    {
	AssignmentNode n = new AssignmentNode();
	nodes.add(n);
	return n;
    }

    /** 
     * Adds new concatenation node to this flow graph. 
     * @return new node
     */
    public ConcatenationNode addConcatenationNode()
    {
	ConcatenationNode n = new ConcatenationNode();
	nodes.add(n);
	return n;
    }

    /** 
     * Adds new initialization node to this flow graph. 
     * @param reg regular language representing initialization values
     * @return new node
     */
    public InitializationNode addInitializationNode(Automaton reg)
    {
	InitializationNode n = new InitializationNode(reg);
	nodes.add(n);
	return n;
    }

    /**
     * Adds new unary operation node to this flow graph. 
     * @param op unary operation
     * @return new node
     */
    public UnaryNode addUnaryNode(UnaryOperation op)
    {
	UnaryNode n = new UnaryNode(op);
	nodes.add(n);
	return n;
    }

    /**
     * Adds new binary operation node to this flow graph. 
     * @param op binary operation
     * @return new node
     */
    public BinaryNode addBinaryNode(BinaryOperation op)
    {
	BinaryNode n = new BinaryNode(op);
	nodes.add(n);
	return n;
    }

    void visitNodes(NodeVisitor v)
    {
	Iterator i = nodes.iterator();
	while (i.hasNext())
	    ((Node) i.next()).visitBy(v);
    }

    static void redirectUses(Node n1, Node n2) 
    {
	Iterator ui = n1.getUses().iterator();
	while (ui.hasNext()) {
	    Use use = (Use) ui.next();
	    use.defs.remove(n1);
	    use.defs.add(n2);
	    n2.uses.add(use);
	}
	n1.uses.clear();
    }

    static void redirectDefs(Use u1, Use u2) 
    {
	Iterator di = u1.defs.iterator();
	while (di.hasNext()) {
	    Node d = (Node) di.next();
	    d.uses.remove(u1);
	    d.uses.add(u2);
	    u2.defs.add(d);
	}
	u1.defs.clear();
    }

    /**
     * Normalized this graph.
     * In a normalized graph, only assignment nodes may have multiple incoming edges.
     * (Concat nodes and binary operation nodes have at most one edge for each argument.)
     */
    public void normalize()
    {
	Iterator ni = new ArrayList(nodes).iterator();
	while (ni.hasNext()) {
	    Node n = (Node) ni.next();
	    if (n instanceof ConcatenationNode) {
		Use left = ((ConcatenationNode) n).left;
		Use right = ((ConcatenationNode) n).right;
		if (left.defs.size()>1) {
		    AssignmentNode a = addAssignmentNode();
		    redirectDefs(left, a.from);
		    left.addDef(a);
		}
		if (right.defs.size()>1) {
		    AssignmentNode a = addAssignmentNode();
		    redirectDefs(right, a.from);
		    right.addDef(a);
		}
	    } else if (n instanceof UnaryNode) {
		Use arg = ((UnaryNode) n).arg;
		if (arg.defs.size()>1) {
		    AssignmentNode a = addAssignmentNode();
		    redirectDefs(arg, a.from);
		    arg.addDef(a);
		}
	    } else if (n instanceof BinaryNode) {
		Use arg1 = ((BinaryNode) n).arg1;
		Use arg2 = ((BinaryNode) n).arg2;
		if (arg1.defs.size()>1) {
		    AssignmentNode a = addAssignmentNode();
		    redirectDefs(arg1, a.from);
		    arg1.addDef(a);
		}
		if (arg2.defs.size()>1) {
		    AssignmentNode a = addAssignmentNode();
		    redirectDefs(arg2, a.from);
		    arg2.addDef(a);
		}
	    } 
	}
    }

    /**
     * Simplifies this graph.
     * Performs the following optimizations:
     * <ul>
     * <li>Nodes that are of same type and have ingoing edges from the same nodes are merged.
     * <li>Sequences of assignments with only one ingoing edge are compressed.
     * <li>Concatenation nodes are bypassed if the first argument has exactly one edge and it comes 
     *     from an initialization node with the empty string.
     * <li>Self-loops on assignments are removed.
     * </ul>
     * @return a map from the original nodes to the corresponding
     *         node in the simplified graph
     */
    public Map simplify()
    {
	ArrayList old_nodes = new ArrayList(nodes);
	Map/*<Node,Node>*/ simp_map = new HashMap(); // map containing all simplifications
	Set/*<Node>*/ dirty_s = new HashSet(nodes);
	LinkedList/*<Node>*/ dirty_l = new LinkedList(nodes);
	Map/*<NodeEquivalence,Node>*/ equiv = new HashMap();
	while (!dirty_l.isEmpty()) {
	    Node n = (Node) dirty_l.removeFirst();
	    dirty_s.remove(n);
	    NodeEquivalence ne = new NodeEquivalence(n);
	    Node nn = null;
	    if (equiv.containsKey(ne)) {
		// already seen equivalent node
		nn = (Node) equiv.get(ne);
		redirectUses(n, nn);
		new RedirectDefs(n, nn);
	    } else if (n instanceof AssignmentNode) {
		Use from = ((AssignmentNode) n).from;
		if (from.defs.contains(n)) {
		    // found self-loop on assignment
		    from.defs.remove(n);
		    n.uses.remove(n);
		}
		if (from.defs.size() == 1) {
		    Node def = (Node) from.defs.iterator().next();
		    if (def != n) {
			// found assignment node with only one ingoing edge
			def.uses.remove(from);
			redirectUses(n, def);
			nn = def;
		    }
		}
	    } else if (n instanceof ConcatenationNode) {
		Use left = ((ConcatenationNode) n).left;
		Use right = ((ConcatenationNode) n).right;
		if (left.defs.size()==1) {
		    Node ld = (Node) left.defs.iterator().next();
		    if (ld instanceof InitializationNode &&
			((InitializationNode) ld).reg.equals(Basic.makeEmptyString())) {
			// found concat node where left arg is one init node with ""
			AssignmentNode a = addAssignmentNode();
			ld.uses.remove(left);
			redirectUses(n, a);
			redirectDefs(right, a.from);
			nn = a;
		    }
		}

	    }
	    if (nn != null) {
		// mark all uses dirty
		Iterator ui = nn.getUses().iterator();
		while (ui.hasNext()) {
		    Use use = (Use) ui.next();
		    Node un = use.getUser();
		    if (!dirty_s.contains(un)) {
			equiv.remove(new NodeEquivalence(un));
			dirty_l.addLast(un);
			dirty_s.add(un);
		    }
		}
		simp_map.put(n, nn);
		nodes.remove(n);
	    } else 
		equiv.put(ne, n);
	}
	// build final mapping
	Map trans_map = new HashMap();
	Iterator ni = old_nodes.iterator();
	while (ni.hasNext()) {
	    Node n = (Node) ni.next();
	    Node n2 = n;
	    while (simp_map.containsKey(n2))
		n2 = (Node) simp_map.get(n2);
	    trans_map.put(n, n2);
	}
	return trans_map;
    }

    /** 
     * Constructs grammar for this flow graph. 
     * @see Node#getNonterminal()
     */
    public Grammar toGrammar()
    {
	Grammar g = new Grammar();
	// make nonterminal for each node
	Iterator i = nodes.iterator();
	while (i.hasNext())
	    ((Node) i.next()).nonterminal = g.newNonterminal();
	// add productions
	visitNodes(new ProductionVisitor(g));
	return g;
    }

    /** Returns number of nodes in this graph. */
    public int getNumberOfNodes()
    {
	return nodes.size();
    }

    /** Returns number of edges in this graph. */
    public int getNumberOfEdges()
    {
	int x = 0;	
	Iterator i = nodes.iterator();
	while (i.hasNext())
	    x += ((Node) i.next()).getUses().size();
	return x;
    }

    /** Returns <a href="http://www.research.att.com/sw/tools/graphviz/" 
        target="_top">Graphviz Dot</a> representation of this graph. */
    public String toDot()
    {
	StringBuffer s = new StringBuffer();
	s.append("digraph FlowGraph {\n");
	visitNodes(new DotVisitor(s));
	s.append("}\n");
	return s.toString();
    }
}

class ProductionVisitor implements NodeVisitor
{
    private Grammar g;
    
    ProductionVisitor(Grammar g)
    {
	this.g = g;
    }
    
    public void visitAssignmentNode(AssignmentNode n)
    {
	Iterator i = n.getArg().getDefs().iterator();
	while (i.hasNext()) {
	    Node m = (Node) i.next();
	    g.addUnitProduction(n.nonterminal, m.nonterminal);
	}
    }

    public void visitConcatenationNode(ConcatenationNode n)
    {
	Iterator i1 = n.getArg1().getDefs().iterator();
	while (i1.hasNext()) {
	    Node m1 = (Node) i1.next();
	    Iterator i2 = n.getArg2().getDefs().iterator();
	    while (i2.hasNext()) {
		Node m2 = (Node) i2.next();
		g.addPairProduction(n.nonterminal, m1.nonterminal, m2.nonterminal);
	    }
	}
    }

    public void visitInitializationNode(InitializationNode n)
    {
	g.addAutomatonProduction(n.nonterminal, n.reg);
    }

    public void visitUnaryNode(UnaryNode n)
    {
	Iterator i = n.getArg().getDefs().iterator();
	while (i.hasNext()) {
	    Node m = (Node) i.next();
	    g.addUnaryProduction(n.nonterminal, n.op, m.nonterminal);
	}
    }
    
    public void visitBinaryNode(BinaryNode n)
    {
	Iterator i1 = n.getArg1().getDefs().iterator();
	while (i1.hasNext()) {
	    Node m1 = (Node) i1.next();
	    Iterator i2 = n.getArg2().getDefs().iterator();
	    while (i2.hasNext()) {
		Node m2 = (Node) i2.next();
		g.addBinaryProduction(n.nonterminal, n.op, m1.nonterminal, m2.nonterminal);
	    }
	}
    }
}

class DotVisitor implements NodeVisitor
{
    private StringBuffer s;
    
    DotVisitor(StringBuffer s)
    {
	this.s = s;
    }

    static String escape(String s)
    {
	if (s==null)
	    return null;
	StringBuffer b = new StringBuffer();
	for (int i = 0; i<s.length(); i++) {
	    char c = s.charAt(i);
	    if (c=='"')
		b.append("\\\"");
	    else if (c=='\\')
		b.append("\\\\");
	    else
		b.append(c);
	}
	return b.toString();
    }
    
    public void visitAssignmentNode(AssignmentNode n)
    {
	s.append("  ").append(n).append(" [label=\"\",shape=circle]\n");
	Iterator i = n.getArg().getDefs().iterator();
	while (i.hasNext()) {
	    Node m = (Node) i.next();
	    s.append("  ").append(m).append(" -> ").append(n).append("\n");
	}
    }

    public void visitConcatenationNode(ConcatenationNode n)
    {
	s.append("  ").append(n).append(" [label=\"concat|<arg1>|<arg2>\",shape=record]\n");
	Iterator i = n.getArg1().getDefs().iterator();
	while (i.hasNext()) {
	    Node m = (Node) i.next();
	    s.append("  ").append(m).append(" -> ").append(n).append(":arg1\n");
	}
	i = n.getArg2().getDefs().iterator();
	while (i.hasNext()) {
	    Node m = (Node) i.next();
	    s.append("  ").append(m).append(" -> ").append(n).append(":arg2\n");
	}
    }

    public void visitInitializationNode(InitializationNode n)
    {
	s.append("  ").append(n).append(" [label=\""+escape((String) n.reg.getInfo())+"\"]\n");
    }

    public void visitUnaryNode(UnaryNode n)
    {
	s.append("  ").append(n).append(" [label=\""+n.op+"|<arg>\",shape=record]\n");
	Iterator i = n.getArg().getDefs().iterator();
	while (i.hasNext()) {
	    Node m = (Node) i.next();
	    s.append("  ").append(m).append(" -> ").append(n).append(":arg\n");
	}
    }
    
    public void visitBinaryNode(BinaryNode n)
    {
	s.append("  ").append(n).append(" [label=\""+n.op+"|<arg1>|<arg2>\",shape=record]\n");
	Iterator i = n.getArg1().getDefs().iterator();
	while (i.hasNext()) {
	    Node m = (Node) i.next();
	    s.append("  ").append(m).append(" -> ").append(n).append(":arg1\n");
	}
	i = n.getArg2().getDefs().iterator();
	while (i.hasNext()) {
	    Node m = (Node) i.next();
	    s.append("  ").append(m).append(" -> ").append(n).append(":arg2\n");
	}
    }
}

class RedirectDefs implements NodeVisitor 
{
    private Node n2;
    
    RedirectDefs(Node n1, Node n2) 
    {
	this.n2 = n2;
	n1.visitBy(this);
    }
    
    public void visitAssignmentNode(AssignmentNode n) 
    {
	Graph.redirectDefs(n.from, ((AssignmentNode) n2).from);
    }
    
    public void visitConcatenationNode(ConcatenationNode n) 
    {
	Graph.redirectDefs(n.left, ((ConcatenationNode) n2).left);
	Graph.redirectDefs(n.right, ((ConcatenationNode) n2).right);
    }
    
    public void visitInitializationNode(InitializationNode n) {}
    
    public void visitUnaryNode(UnaryNode n) 
    {
	Graph.redirectDefs(n.arg, ((UnaryNode) n2).arg);
    }
    
    public void visitBinaryNode(BinaryNode n) 
    {
	Graph.redirectDefs(n.arg1, ((BinaryNode) n2).arg1);
	Graph.redirectDefs(n.arg2, ((BinaryNode) n2).arg2);
    }
}
