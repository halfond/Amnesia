package dk.brics.string.operations;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.StatePair;
import dk.brics.string.mlfa.CharSet;
import dk.brics.string.mlfa.UnaryOperation;


public class Prefix extends UnaryOperation
{
    /** Constructs new operation object. */
    public Prefix() {}

    /** 
     * Automaton operation. 
     * Constructs new automaton as copy of <tt>a</tt> with a new accept state <i>s</i>.
     * Epsilon transitions are added from every state to <i>s</i>.
     * @param a input automaton
     * @return resulting automaton
     */
    public Automaton op(Automaton a) 
    {
	Automaton b = (Automaton) a.clone();
	State accept = new State();
	accept.setAccept(true);
	Set epsilons = new HashSet();
	Iterator i = b.getStates().iterator();
	while (i.hasNext()) {
	    State s = (State) i.next();
	    epsilons.add(new StatePair(s, accept));
	}
	b.addEpsilons(epsilons);
	b.minimize();
	return b;
    }

    public String toString()
    {
	return "prefix";
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
	return obj instanceof Prefix;
    }
}
