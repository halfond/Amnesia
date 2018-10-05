package dk.brics.string.operations;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.StatePair;
import dk.brics.string.mlfa.BinaryOperation;
import dk.brics.string.mlfa.CharSet;

/** Automaton operation for {@link StringBuffer#insert(int,Object)} and related methods. */
public class Insert extends BinaryOperation
{
    /** Constructs new operation object. */
    public Insert() {}

    /** 
     * Automaton operation. 
     * Constructs new automaton as two copies, <i>a1</i> and <i>a2</i>, of <tt>a</tt> and 
     * one copy, <i>b</i>, of <tt>b</tt>.  The initial state is the one from <i>a1</i>, the accept states
     * are those in <i>a2</i>. Epsilon transitions are added from each state in <i>a1</i> to the 
     * initial state in <i>b</i> and from each accept state in <i>b</i> to each state in <i>a2</i>.
     * @param a first input automaton
     * @param b second input automaton
     * @return resulting automaton
     */
    public Automaton op(Automaton a, Automaton b) 
    {
	return pop(a, b);
    }

    static Automaton pop(Automaton a, Automaton b) 
    {
	Automaton a1 = (Automaton) a.clone();
	Automaton a2 = (Automaton) a.clone();
	Automaton bb = (Automaton) b.clone();
	Set epsilons = new HashSet();
	Iterator i = a1.getStates().iterator();
	while (i.hasNext()) {
	    State s = (State) i.next();
	    s.setAccept(false);
	    epsilons.add(new StatePair(s, bb.getInitialState()));
	}
	i = bb.getAcceptStates().iterator();
	while (i.hasNext()) {
	    State s = (State) i.next();
	    s.setAccept(false);
	    Iterator j = a2.getStates().iterator();
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
	return "insert";
    }

    public int getPriority()
    {
	return 8;
    }

    public CharSet charsetTransfer(CharSet a1, CharSet a2)
    {
	return a1.union(a2);
    }

    public int hashCode() 
    {
	return getClass().hashCode();
    }

    public boolean equals(Object obj) 
    {
	return obj instanceof Insert;
    }
}
