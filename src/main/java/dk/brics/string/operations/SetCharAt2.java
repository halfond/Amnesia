package dk.brics.string.operations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dk.brics.string.mlfa.CharSet;
import dk.brics.string.mlfa.UnaryOperation;

/** Automaton operation for {@link StringBuffer#setCharAt(int,char)} where the character is unknown. */
public class SetCharAt2 extends UnaryOperation
{
    /** Constructs new operation object. */
    public SetCharAt2() {}

    /** 
     * Automaton operation. 
     * Constructs new automaton from two copies, <i>a1</i> and <i>a2</i>, of <tt>a</tt>.  
     * The initial state is the one from <i>a1</i>, the accept states
     * are those in <i>a2</i>. Extra Sigma transitions are added from each state <i>q</i> in <i>a1</i> to the 
     * the states in <i>a2</i> that are reachable in exactly one step from the one that corresponds to <i>q</i>.
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
	i = a1s.iterator();
	while (i.hasNext()) {
	    State s = (State) i.next();
	    Iterator k = ((State) map.get(s)).getTransitions().iterator();
	    while (k.hasNext()) {
		Transition t = (Transition) k.next();
		s.addTransition(new Transition(Character.MIN_VALUE, Character.MAX_VALUE, t.getDest()));
	    }
	}
	a1.minimize();
	return a1;
    }

    public String toString()
    {
	return "setCharAt2";
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
	return obj instanceof SetCharAt2;
    }
}
