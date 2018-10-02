package dk.brics.string.operations;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dk.brics.string.mlfa.CharSet;
import dk.brics.string.mlfa.UnaryOperation;

/** Automaton operation for {@link String#toUpperCase()}. */
public class ToUpperCase extends UnaryOperation
{
    /** Constructs new operation object. */
    public ToUpperCase() {}

    /** 
     * Automaton operation. 
     * Constructs new automaton as copy of <tt>a</tt> where all characters in all transitions are
     * converted to upper case, except that Sigma transitions are left unchanged.
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
		if (min!=Character.MIN_VALUE || max!=Character.MAX_VALUE) {
		    transitions.remove(t);
		    for (char c = min; c<=max; c++)
			transitions.add(new Transition(Character.toUpperCase(c), dest));
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
	return "toUpperCase";
    }

    public int getPriority()
    {
	return 2;
    }

    public CharSet charsetTransfer(CharSet a)
    {
	return a.toUpperCase();
    }

    public int hashCode() 
    {
	return getClass().hashCode();
    }

    public boolean equals(Object obj) 
    {
	return obj instanceof ToUpperCase;
    }
}
