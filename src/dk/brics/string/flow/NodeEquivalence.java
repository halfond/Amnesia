package dk.brics.string.flow;

/**
 * Node equivalence checker.
 * Two nodes are equivalent if they are of the same type and 
 * have ingoing edges from the same nodes.
 * It is assumed that InitializationNode instances are equal
 * if their automata both have associated info objects and
 * these info objects are equal.
 */
class NodeEquivalence 
{
    private Node node;
    private int hash;

    NodeEquivalence(Node n) 
    {
	node = n;
	node.visitBy(new HashCalculator());
    }

    Node getNode() 
    {
	return node;
    }

    public int hashCode() 
    {
	return hash;
    }

    /** Tests for equivalence. */
    public boolean equals(Object obj) 
    {
	if (obj instanceof NodeEquivalence) {
	    NodeComparator nc = new NodeComparator();
	    ((NodeEquivalence) obj).getNode().visitBy(nc);
	    return nc.result;
	} else 
	    return false;
    }

    private class HashCalculator implements NodeVisitor 
    {
	public void visitAssignmentNode(AssignmentNode n) 
	{
	    hash = n.from.defs.hashCode()*3;
	}

	public void visitConcatenationNode(ConcatenationNode n) 
	{
	    hash = n.left.defs.hashCode()*5+n.right.defs.hashCode()*7;
	}

	public void visitInitializationNode(InitializationNode n) 
	{
	    if (n.reg.getInfo() != null) {
		hash = n.reg.getInfo().hashCode();
	    } else {
		hash = n.hashCode();
	    } 
	}

	public void visitUnaryNode(UnaryNode n) 
	{
	    hash = n.op.hashCode()*17+n.arg.defs.hashCode()*19;
	}

	public void visitBinaryNode(BinaryNode n) 
	{
	    hash = n.op.hashCode()*23+n.arg1.defs.hashCode()*29+n.arg2.defs.hashCode()*31;
	}
    }

    private class NodeComparator implements NodeVisitor 
    {
	boolean result;

	public void visitAssignmentNode(AssignmentNode n) 
	{
	    result = (node instanceof AssignmentNode) &&
		((AssignmentNode) node).from.defs.equals(n.from.defs);
	}

	public void visitConcatenationNode(ConcatenationNode n) 
	{
	    result = (node instanceof ConcatenationNode) &&
		((ConcatenationNode) node).left.defs.equals(n.left.defs) &&
		((ConcatenationNode) node).right.defs.equals(n.right.defs);
	}

	public void visitInitializationNode(InitializationNode n) 
	{
	    if (node instanceof InitializationNode) {
		InitializationNode n2 = (InitializationNode) node;
		Object i1 = n.reg.getInfo();
		Object i2 = n2.reg.getInfo();
		result = (i1 != null && i2 != null && i1.equals(i2));
	    } else {
		result = false;
	    }

	}

	public void visitUnaryNode(UnaryNode n) 
	{
	    result = (node instanceof UnaryNode) &&
		((UnaryNode) node).op.equals(n.op) &&
		((UnaryNode) node).arg.defs.equals(n.arg.defs);
	}


	public void visitBinaryNode(BinaryNode n) 
	{
	    result = (node instanceof BinaryNode) &&
		((BinaryNode) node).op.equals(n.op) &&
		((BinaryNode) node).arg1.defs.equals(n.arg1.defs) &&
		((BinaryNode) node).arg2.defs.equals(n.arg2.defs);
	}
    }
}
