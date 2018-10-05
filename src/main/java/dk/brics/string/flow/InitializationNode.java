package dk.brics.string.flow;

import dk.brics.automaton.Automaton;

/** Initialization node. */
public class InitializationNode extends Node 
{
    Automaton reg;

    InitializationNode(Automaton reg) 
    {
	this.reg = reg;
    }

    /** Visitor. */
    public void visitBy(NodeVisitor v) 
    {
	v.visitInitializationNode(this);
    }
}
