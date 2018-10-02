package dk.brics.string.operations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dk.brics.string.mlfa.CharSet;
import dk.brics.string.mlfa.UnaryOperation;

/** Automaton operation for {@link String#replace(char,char)} where neither character is known. */
public class Replace4 extends UnaryOperation
{
    /** Constructs new operation object. */
    public Replace4() {}

    /** 
     * Automaton operation. 
     * Constructs new automaton as copy of <tt>a</tt> where all transitions are 
     * replaced by Sigma transitions.
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
		transitions.remove(t);
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
	return "replace4";
    }

    public int getPriority()
    {
	return 7;
    }

    public CharSet charsetTransfer(CharSet a)
    {
	return CharSet.makeAnychars();
    }

    public int hashCode() 
    {
	return getClass().hashCode();
    }

    public boolean equals(Object obj) 
    {
	return obj instanceof Replace4;
    }
}
