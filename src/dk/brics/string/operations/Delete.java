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

/** Automaton operation for {@link StringBuffer#delete(int,int)}. */
public class Delete extends UnaryOperation
{
    /** Constructs new operation object. */
    public Delete() {}

    /** 
     * Automaton operation. 
     * Constructs new automaton from two copies, <i>a1</i> and <i>a2</i>, of <tt>a</tt>.
     * The initial state is the one from <i>a1</i>, and the accept states are those from <i>a2</i>.
     * From each state <i>q</i> in <i>a1</i>, epsilon transitions are added from <i>q</i> to all states
     * in <i>a2</i> that are reachable from the one corresponding to <i>q</i>.
     * @param a input automaton
     * @return resulting automaton
     */
    public Automaton op(Automaton a) 
    {
	Automaton a1 = (Automaton) a.clone();
	Map map = new HashMap();
	Set a1s = a1.getStates();
	Iterator i = a1s.iterator();
	while (i.hasNext()) {
	    State s = (State) i.next();
	    State p = new State();
	    map.put(s, p);
	    if (s.isAccept()) {
		p.setAccept(true);
		s.setAccept(false);
	    }
	}
	i = a1s.iterator();
	while (i.hasNext()) {
	    State s = (State) i.next();
	    State p = (State) map.get(s);
	    Iterator k = s.getTransitions().iterator();
	    while (k.hasNext()) {
		Transition t = (Transition) k.next();
		p.addTransition(new Transition(t.getMin(), t.getMax(), (State) map.get(t.getDest())));
	    }
	}
	a1.setDeterministic(false);
	Set epsilons = new HashSet();
	i = a1s.iterator();
	while (i.hasNext()) {
	    State s = (State) i.next();
	    Set reachable = Basic.findReachableStates((State) map.get(s));
	    Iterator j = reachable.iterator();
	    while (j.hasNext()) {
		State p = (State) j.next();
		epsilons.add(new StatePair(s, p));
	    }
	}
	a1.addEpsilons(epsilons);
	a1.minimize();
	return a1;
    }

    public String toString()
    {
	return "delete";
    }

    public int getPriority()
    {
	return 4;
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
	return obj instanceof Delete;
    }
}
