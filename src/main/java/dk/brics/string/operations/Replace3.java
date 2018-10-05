package dk.brics.string.operations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dk.brics.string.mlfa.CharSet;
import dk.brics.string.mlfa.UnaryOperation;

/** Automaton operation for {@link String#replace(char,char)} where only the second character is known. */
public class Replace3 extends UnaryOperation
{
    char d;
    
    /** Constructs new operation object. */
    public Replace3(char d) 
    {
	this.d = d;
    }

    /** 
     * Automaton operation. 
     * Constructs new automaton as copy of <tt>a</tt> where all transitions are supplemented with <tt>d</tt>
     * transitions.
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
		State dest = t.getDest();
		transitions.add(new Transition(d, dest));
	    }
	}
	b.setDeterministic(false);
	b.reduce();
	b.minimize();
	return b;
    }

    public String toString()
    {
	return "replace3["+Basic.escapeChar(d)+"]";
    }

    public int getPriority()
    {
	return 6;
    }

    public CharSet charsetTransfer(CharSet a)
    {
	return a.add(d);
    }

    public int hashCode() 
    {
	return getClass().hashCode()+d;
    }

    public boolean equals(Object obj) 
    {
	if (obj instanceof Replace3) {
	    Replace3 o = (Replace3) obj;
	    return d==o.d;
	} else 
	    return false;
    }
}
