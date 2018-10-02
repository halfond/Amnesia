package dk.brics.string.operations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dk.brics.string.mlfa.CharSet;
import dk.brics.string.mlfa.UnaryOperation;

/** Automaton operation for {@link String#trim()}. */
public class Trim extends UnaryOperation
{
    /** Constructs new operation object. */
    public Trim() {}

    /** 
     * Automaton operation. 
     * Constructs new automaton as copy of <tt>a</tt> with a new initial state and a new accept state.
     * Non-epsilon transitions are added from the new initial state to states that are reachable from the
     * old initial state in zero or more special chars followed by one non-special char, thereby skipping initial 
     * special chars. Similarly, non-epsilon transitions are added from states that can reach the old
     * final state in one non-special char followed by zero or more special chars to the new final state, 
     * thereby skipping final special chars. ("Special" chars are those with value less than or equal to 0x20.)
     * The new initial state is accepting if the old one could reach an accept state by zero or more special chars.
     * @param a input automaton
     * @return resulting automaton
     */
    public Automaton op(Automaton a) 
    {
	Automaton b = (Automaton) a.clone();
	b.setDeterministic(false);
	Map normal_prevs = new HashMap();
	Map special_prevs = new HashMap();
	findPrevs(b, normal_prevs, special_prevs);
	Set pre = findPreSet(b);
	Set post = findPostSet(b, special_prevs);
	boolean initial_accept = post.contains(b.getInitialState());
	State initial = new State();
	b.setInitialState(initial);
	Iterator i = pre.iterator();
	while (i.hasNext()) {
	    State s = (State) i.next();
	    Iterator j = new ArrayList(s.getTransitions()).iterator();
	    while (j.hasNext()) {
		Transition t = (Transition) j.next();
		char min = t.getMin();
		char max = t.getMax();
		if (min<='\u0020')
		    min = '\u0021';
		if (min<=max)
		    initial.addTransition(new Transition(min, max, t.getDest()));
	    }
	}
	State accept = new State();
	accept.setAccept(true);
	i = b.getAcceptStates().iterator();
	while (i.hasNext()) {
	    State s = (State) i.next();
	    s.setAccept(false);
	}
	if (initial_accept)
	    initial.setAccept(true);
	i = post.iterator();
	while (i.hasNext()) {
	    State s = (State) i.next();
	    Set prevset = (Set) normal_prevs.get(s);
	    if (prevset!=null) {
		Iterator j = prevset.iterator();
		while (j.hasNext()) {
		    State p = (State) j.next();
		    Iterator k = new ArrayList(p.getTransitions()).iterator();
		    while (k.hasNext()) {
			Transition t = (Transition) k.next();
			if (t.getDest()==s) {
			    char min = t.getMin();
			    char max = t.getMax();
			    if (min<='\u0020')
				min = '\u0021';
			    if (min<=max)
				p.addTransition(new Transition(min, max, accept));
			}
		    }
		}
	    }
	}
	b.minimize();
	return b;
    }

    private Set findPreSet(Automaton b)
    {
	Set pre = new HashSet();
	TreeSet pending = new TreeSet();
	pending.add(b.getInitialState());
	while (!pending.isEmpty()) {
	    State p = (State) pending.first();
	    pending.remove(p);
	    pre.add(p);
	    Iterator i = p.getTransitions().iterator();
	    while (i.hasNext()) {
		Transition t = (Transition) i.next();
		if (t.getMin()<='\u0020') {
		    State q = t.getDest();
		    if (!pre.contains(q))
			pending.add(q);
		}
	    }
	}
	return pre;
    }

    private Set findPostSet(Automaton b, Map special_prevs)
    {
	Set post = new HashSet();
	TreeSet pending = new TreeSet();
	pending.addAll(b.getAcceptStates());
	while (!pending.isEmpty()) {
	    State p = (State) pending.first();
	    pending.remove(p);
	    post.add(p);
	    Set prevset = (Set) special_prevs.get(p);
	    if (prevset!=null) {
		Iterator i = prevset.iterator();
		while (i.hasNext()) {
		    State q = (State) i.next();
		    if (!post.contains(q))
			pending.add(q);
		}
	    }
	}
	return post;
    }

    private void findPrevs(Automaton b, Map normal_prevs, Map special_prevs)
    {
	Iterator i = b.getStates().iterator();
	while (i.hasNext()) {
	    State s = (State) i.next();
	    Iterator j = s.getTransitions().iterator();
	    while (j.hasNext()) {
		Transition t = (Transition) j.next();
		char min = t.getMin();
		char max = t.getMax();
		State dest = t.getDest();
		if (min<='\u0020') {
		    Set prevset = (Set) special_prevs.get(dest);
		    if (prevset==null) {
			prevset = new HashSet();
			special_prevs.put(dest, prevset);
		    }
		    prevset.add(s);
		}
		if (max>'\u0020') {
		    Set prevset = (Set) normal_prevs.get(dest);
		    if (prevset==null) {
			prevset = new HashSet();
			normal_prevs.put(dest, prevset);
		    }
		    prevset.add(s);
		}
	    }
	}
    }

    public String toString()
    {
	return "trim";
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
	return obj instanceof Trim;
    }
}
