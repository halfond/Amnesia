package dk.brics.string.flow;

import dk.brics.string.mlfa.UnaryOperation;

/** Unary operation node. */
public class UnaryNode extends Node 
{
    UnaryOperation op;
    Use arg;

    UnaryNode(UnaryOperation op) 
    {
	this.op = op;
	arg = new Use(this);
    }

    /** Returns <tt>Use</tt> for argument. */
    public Use getArg()
    {
	return arg;
    }

    /** Visitor. */
    public void visitBy(NodeVisitor v) 
    {
	v.visitUnaryNode(this);
    }
}
