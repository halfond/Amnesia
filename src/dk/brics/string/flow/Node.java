package dk.brics.string.flow;

import java.util.HashSet;
import java.util.Set;

import dk.brics.string.grammar.Nonterminal;

/** Base class for flow graph nodes. A node represent a definition (a variable or expression). */
public abstract class Node 
{
    Set/*<Use>*/ uses = new HashSet();
    Nonterminal nonterminal;

    Node() {}

    /** Returns set of {@link Use} objects representing uses of this definition. */
    public Set getUses()
    {
	return uses;
    }

    /** 
     * Returns grammar nonterminal. 
     * Should only be invoked after {@link Graph#toGrammar}.
     */
    public Nonterminal getNonterminal()
    {
	return nonterminal;
    }

    /** Visitor. */
    public abstract void visitBy(NodeVisitor v);

    /** Returns name of this node. */
    public String toString()
    {
	String s = super.toString();
	return "N"+s.substring(s.indexOf('@')+1);
    }
}
