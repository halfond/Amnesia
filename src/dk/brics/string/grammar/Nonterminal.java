package dk.brics.string.grammar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import dk.brics.string.mlfa.CharSet;
import dk.brics.string.mlfa.MLFAState;
import dk.brics.string.mlfa.MLFAStatePair;

/** Grammar nonterminal. */
public class Nonterminal implements Comparable
{
    Set productions = new HashSet();

    Component component;
    Set nexts;
    int dfnumber, lowlink;
    boolean newmark, onstack;

    Nonterminal primed;
    Set oldproductions;

    MLFAState state;

    int number;

    CharSet charset;
    Set prevs;

    boolean need_epsilon;

    Nonterminal(int number) 
    {
	this.number = number;
    }

    public int compareTo(Object o)
    {
	return ((Nonterminal) o).number-number;
    }

    void resetComponentInfo()
    {
	component = null;
	newmark = true;
	onstack = false;
	charset = new CharSet();
	nexts = new HashSet();
	Iterator i = productions.iterator();
	while (i.hasNext()) {
	    Production p = (Production) i.next();
	    p.addNexts(nexts);
	} 
	prevs = new HashSet();
	need_epsilon = false;
    }

    void findPrevs()
    {
	Iterator i = productions.iterator();
	while (i.hasNext()) {
	    Production p = (Production) i.next();
	    p.addPrevs(this);
	} 

    }

    /** 
     * Returns MLFA state pair.
     * @return state pair representing initial and final state 
     *         for this grammar nonterminal
     */
    public MLFAStatePair getMLFAStatePair()
    {
	MLFAStatePair p;
	if (component.recursion==Component.RIGHT || 
	    component.recursion==Component.NONE)
	    p = new MLFAStatePair(state, component.state);
	else
	    p = new MLFAStatePair(component.state, state);
	return p;
    }

    /** Returns name of this nonterminal. */
    public String toString()
    {
	return "x"+number;
    }

    /** Updates charset according to productions. Returns true if any changes. */
    boolean updateCharset()
    {
	List c = new ArrayList();
	Iterator i = productions.iterator();
	while (i.hasNext()) {
	    Production p = (Production) i.next();
	    c.add(p.charsetTransfer());
	}
	CharSet newset = CharSet.union(c);
	boolean b;
	if (charset==null)
	    b = true;
	else
	    b = !newset.equals(charset);
	charset = newset;
	return b;
    }
}
