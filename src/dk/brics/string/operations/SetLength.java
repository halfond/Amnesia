package dk.brics.string.operations;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.StatePair;
import dk.brics.automaton.Transition;
import dk.brics.string.mlfa.CharSet;
import dk.brics.string.mlfa.UnaryOperation;

/** Automaton operation for {@link StringBuffer#setLength(int)}. */
public class SetLength extends UnaryOperation
{
    /** Constructs new operation object. */
    public SetLength() {}

    /** 
     * Automaton operation. 
     * Constructs new automaton as copy of <tt>a</tt> where a fresh state is
     * the only accept state, it has a 0-transition to itself, all original accept states has
     * an epsilon edge to it, and every state reachable from the initial state becomes an
     * accept state.
     * @param a input automaton
     * @return resulting automaton
     */
    public Automaton op(Automaton a) 
    {
	Automaton b = (Automaton) a.clone();
	State pad = new State();
	pad.setAccept(true);
	pad.addTransition(new Transition('\u0000', pad));
	Set epsilons = new HashSet();
	Iterator i = b.getStates().iterator();
       	while (i.hasNext()) {
	    State s = (State) i.next();
	    if (s.isAccept())
		epsilons.add(new StatePair(s, pad));
	    else
		s.setAccept(true);
	}
	b.setDeterministic(false);
	b.addEpsilons(epsilons);
	b.minimize();
	return b;
    }

    public String toString()
    {
	return "setLength";
    }

    public int getPriority()
    {
	return 6;
    }

    public CharSet charsetTransfer(CharSet a)
    {
	return a;
    }

    public int hashCode() 
    {
	return getClass().hashCode();
    }

    public boolean equals(Object obj) 
    {
	return obj instanceof SetLength;
    }
}
