package dk.brics.string.operations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.StatePair;
import dk.brics.automaton.Transition;
import dk.brics.string.mlfa.CharSet;
import dk.brics.string.mlfa.UnaryOperation;

/** Automaton operation for {@link StringBuffer#reverse()}. */
public class Reverse extends UnaryOperation
{
    /** Constructs new operation object. */
    public Reverse() {}

    /** 
     * Automaton operation. 
     * Constructs new automaton as copy of <tt>a</tt> with a fresh initial state, epsilon transitions
     * from that state to all old accept states, the old initial state as only new accept state, and all
     * transitions reversed.
     * @param a input automaton
     * @return resulting automaton
     */
    public Automaton op(Automaton a) 
    {
	Automaton b = new Automaton();
        Map map = new HashMap();
	Iterator i = a.getStates().iterator();
       	while (i.hasNext()) {
	    State s = (State) i.next();
	    State ss = new State();
	    map.put(s, ss);
	}	
	State initial = new State();
	b.setInitialState(initial);
	((State) map.get(a.getInitialState())).setAccept(true);
	Set epsilons = new HashSet();
	i = a.getStates().iterator();
       	while (i.hasNext()) {
	    State s = (State) i.next();
	    State ss = (State) map.get(s);
	    if (s.isAccept())
		epsilons.add(new StatePair(initial, ss));
	    Iterator j = s.getTransitions().iterator();
	    while (j.hasNext()) {
		Transition t = (Transition) j.next();
		State pp = (State) map.get(t.getDest());
		pp.addTransition(new Transition(t.getMin(), t.getMax(), ss));
	    }
	}
	b.setDeterministic(false);
	b.addEpsilons(epsilons);
	b.minimize();
	return b;
    }

    public String toString()
    {
	return "reverse";
    }

    public int getPriority()
    {
	return 1;
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
	return obj instanceof Reverse;
    }
}
