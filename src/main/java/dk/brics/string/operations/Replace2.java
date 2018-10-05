package dk.brics.string.operations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dk.brics.string.mlfa.CharSet;
import dk.brics.string.mlfa.UnaryOperation;

/** Automaton operation for {@link String#replace(char,char)} where only the first character is known. */
public class Replace2 extends UnaryOperation
{
    char c;

    /** Constructs new operation object. */
    public Replace2(char c) 
    {
	this.c = c;
    }

    /** 
     * Automaton operation. 
     * Constructs new automaton as copy of <tt>a</tt> where all <tt>c</tt> transitions are changed
     * to Sigma transitions.
     * @param a input automaton
     * @return resulting automaton
     */
    public Automaton op(Automaton a) 
    {
	Automaton b = (Automaton) a.clone();
	Iterator i = b.getStates().iterator();
	while (i.hasNext()) {
	    State s = (State) i.next();
	    Set transitions = s.getTransitions();
	    Iterator j = (new ArrayList(transitions)).iterator();
	    while (j.hasNext()) {
		Transition t = (Transition) j.next();
		char min = t.getMin();
		char max = t.getMax();
		State dest = t.getDest();
		if (min<=c && c<=max)
		    transitions.add(new Transition(Character.MIN_VALUE, Character.MAX_VALUE, dest));
	    }
	}
	b.setDeterministic(false);
	b.reduce();
	b.minimize();
	return b;
    }

    public String toString()
    {
	return "replace2["+Basic.escapeChar(c)+"]";
    }

    public int getPriority()
    {
	return 5;
    }

    public CharSet charsetTransfer(CharSet a)
    {
	if (a.contains(c))
	    return CharSet.makeAnychars();
	else
	    return a;
    }

    public int hashCode() 
    {
	return getClass().hashCode()+c;
    }

    public boolean equals(Object obj) 
    {
	if (obj instanceof Replace2) {
	    Replace2 o = (Replace2) obj;
	    return c==o.c;
	} else 
	    return false;
    }
}
