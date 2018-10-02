package dk.brics.string.operations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dk.brics.string.mlfa.CharSet;
import dk.brics.string.mlfa.UnaryOperation;

/** Automaton operation for {@link String#replace(char,char)} where both characters are known. */
public class Replace1 extends UnaryOperation
{
    char c, d;

    /** Constructs new operation object. */
    public Replace1(char c, char d) 
    {
	this.c = c;
	this.d = d;
    }

    /** 
     * Automaton operation. 
     * Constructs new automaton as copy of <tt>a</tt> where all <tt>c</tt> transitions are
     * replaced by <tt>d</tt> transitions.
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
		if (min<=c && c<=max) {
		    transitions.remove(t);
		    transitions.add(new Transition(d, dest));
		    if (min<c)
			transitions.add(new Transition(min, (char) (c-1), dest));
		    if (c<max)
			transitions.add(new Transition((char) (c+1), max, dest));
		}
	    }
	}
	b.setDeterministic(false);
	b.reduce();
	b.minimize();
	return b;
    }

    public String toString()
    {
	return "replace1["+Basic.escapeChar(c)+","+Basic.escapeChar(d)+"]";
    }

    public int getPriority()
    {
	return 3;
    }

    public CharSet charsetTransfer(CharSet a)
    {
	if (a.contains(c))
	    return a.remove(c).add(d);
	else
	    return a;
    }

    public int hashCode() 
    {
	return getClass().hashCode()+c+d;
    }

    public boolean equals(Object obj) 
    {
	if (obj instanceof Replace1) {
	    Replace1 o = (Replace1) obj;
	    return c==o.c && d==o.d;
	} else 
	    return false;
    }
}
