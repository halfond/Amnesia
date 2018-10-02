package dk.brics.string.mlfa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.StatePair;

/** 
 * MLFA operations. 
 * @author Anders M&oslash;ller &lt;<a href="mailto:amoeller@brics.dk">amoeller@brics.dk</a>&gt;
 */
public class MLFA
{
    Map memo; // Map<MLFAStatePair,Automaton>
    Set seen; // Set<MLFAStatePair>
    Set states;

    /** Constructs new empty MLFA. */
    public MLFA() 
    {
	states = new HashSet();
	memo = new HashMap();
    }

    /** 
     * Adds new state to this MLFA. 
     * @return new state
     */
    public MLFAState newState()
    {
	MLFAState s = new MLFAState();
	states.add(s);
	return s;
    }

    /**
     * Adds new identity transition to this MLFA. 
     * @param p source state
     * @param q target state
     * @param r pair of initial and final state at lower level
     */
    public void addIdentityTransition(MLFAState p, MLFAState q, MLFAStatePair r)
    {
	p.addTransition(new IdentityTransition(r.getFirstState(), r.getSecondState()), q);
    }

    /**
     * Adds new automaton transition to this MLFA. 
     * @param p source state
     * @param q target state
     * @param a (minimized) automaton at lower level
     */
    public void addAutomatonTransition(MLFAState p, MLFAState q, Automaton a)
    {
	p.addTransition(new AutomatonTransition(a), q);
    }

    /**
     * Adds new unary operation transition to this MLFA. 
     * @param p source state
     * @param q target state
     * @param op operation to perform on lower level
     * @param r pair of initial and final state at lower level
     */
    public void addUnaryTransition(MLFAState p, MLFAState q, UnaryOperation op, MLFAStatePair r)
    {
	p.addTransition(new UnaryTransition(op, r.getFirstState(), r.getSecondState()), q);
    }

    /**
     * Adds new binary operation transition to this MLFA. 
     * @param p source state
     * @param q target state
     * @param op operation to perform on lower level
     * @param r first pair of initial and final state at lower level
     * @param s second pair of initial and final state at lower level
     */
    public void addBinaryTransition(MLFAState p, MLFAState q, BinaryOperation op, MLFAStatePair r, MLFAStatePair s)
    {
	p.addTransition(new BinaryTransition(op, r.getFirstState(), r.getSecondState(),
					     s.getFirstState(), s.getSecondState()), q);
    }

    /**
     * Adds new epsilon transition to this MLFA. 
     * @param p source state
     * @param q target state
     */
    public void addEpsilonTransition(MLFAState p, MLFAState q)
    {
	p.addTransition(new EpsilonTransition(), q);
    }

    /** Returns number of states in this MLFA. */
    public int getNumberOfStates()
    {
	return states.size();
    }

    /** Returns string representation of this MLFA. */
    public String toString()
    {
	StringBuffer b = new StringBuffer();
	Iterator i = states.iterator();
	while (i.hasNext()) {
	    MLFAState s = (MLFAState) i.next();
	    Iterator j = s.edges.iterator();
	    while (j.hasNext()) {
		Edge e = (Edge) j.next();
		b.append(s).append("--").append(e.t).append("-->").append(e.dest).append("\n");
	    }
	}
	return b.toString();
    }

    /**
     * Extracts standard minimal deterministic finite automaton from this MLFA. 
     * @param p pair representing initial and final state
     * @return minimal deterministic finite automaton, should not be modified by caller
     */
    public Automaton extract(MLFAStatePair p)
    {
	seen = new HashSet();
	setReachable();
	return extract(p.getFirstState(), p.getSecondState());
    }

    Automaton extract(MLFAState s, MLFAState f)
    {
	MLFAStatePair p = new MLFAStatePair(s, f);
	Automaton a = (Automaton) memo.get(p);
	if (a!=null)
	    return a;
	if (seen.contains(p))
	    throw new RuntimeException("MLFA is non-rankable");
	seen.add(p);
	Set reachable = findReachable(s, f);
	if (((s!=f && reachable.size()==2) || (s==f && reachable.size()==1)) &&
	    s.edges.size()==1) {
	    MLFATransition t = ((Edge) s.edges.iterator().next()).t;
	    // handle special case with just one automaton/identity transition from s to f
	    if (t instanceof AutomatonTransition) {
		AutomatonTransition tt = (AutomatonTransition) t;
		a = tt.a;
	    } else if (t instanceof IdentityTransition) {
		IdentityTransition tt = (IdentityTransition) t;
		a = extract(tt.s, tt.f);
	    }
	}
	if (a==null) {
	    a = new Automaton();
	    // construct automaton states
	    Map statemap = new HashMap(); // map from MLFA states to corresponding Automaton states
	    Iterator i = reachable.iterator();
	    while (i.hasNext()) {
		MLFAState q = (MLFAState) i.next();
		State ss = new State();
		statemap.put(q, ss);
		if (q==s)
		    a.setInitialState(ss);
		if (q==f)
		    ss.setAccept(true);
	    }
	    // add transitions
	    Set epsilons = new HashSet();
	    i = reachable.iterator();
	    while (i.hasNext()) {
		MLFAState q = (MLFAState) i.next();
		Iterator j = q.edges.iterator();
		while (j.hasNext()) {
		    Edge e = (Edge) j.next();
		    if (reachable.contains(e.dest)) {
			State qq = (State) statemap.get(q);
			State pp = (State) statemap.get(e.dest);
			if (e.t instanceof EpsilonTransition)
			    epsilons.add(new StatePair(qq, pp));
			else {
			    Automaton b = null;
			    if (e.t instanceof AutomatonTransition) {
				AutomatonTransition tt = (AutomatonTransition) e.t;
				b = (Automaton) tt.a.clone();
			    } else if (e.t instanceof IdentityTransition) {
				IdentityTransition tt = (IdentityTransition) e.t;
				b = (Automaton) extract(tt.s, tt.f).clone();
			    } else if (e.t instanceof UnaryTransition) {
				UnaryTransition tt = (UnaryTransition) e.t;
				b = tt.op.op(extract(tt.s, tt.f));
			    } else if (e.t instanceof BinaryTransition) {
				BinaryTransition tt = (BinaryTransition) e.t;
				b = tt.op.op(extract(tt.s1, tt.f1), extract(tt.s2, tt.f2));
			    }
			    epsilons.add(new StatePair(qq, b.getInitialState()));
			    Iterator k = b.getAcceptStates().iterator();
			    while (k.hasNext()) {
				State rr = (State) k.next();
				rr.setAccept(false);
				epsilons.add(new StatePair(rr, pp));
			    }
			}
		    }
		}
	    }
	    a.addEpsilons(epsilons);
	    //a.minimize(); //FIXME: COmmented out temporarily
	}
	seen.remove(p);
	memo.put(p, a);
	return a;
    }

    Set findReachable(MLFAState p, MLFAState f)
    {
	Set reachable = new HashSet();
	Iterator i = p.reachable.iterator();
	while (i.hasNext()) {
	    MLFAState s = (MLFAState) i.next();
	    if (s.reachable.contains(f))
		reachable.add(s);
	}
	return reachable;
    }
    
    void setReachable()
    {
	// reset
	Iterator i = states.iterator();
	while (i.hasNext()) {
	    MLFAState s = (MLFAState) i.next();
	    s.reachable = new HashSet();
	    s.newmark = true;
	    s.onstack = false;
	}
	// find strongly connected components
	List components = new ArrayList();
	Stack stack = new Stack();
	i = states.iterator();
	while (i.hasNext()) {
	    MLFAState s = (MLFAState) i.next();
	    if (s.newmark)
		searchc(s, 0, components, stack);
	}
	// find connections between components using Aho-Hopcroft-Ullman
	i = components.iterator();
	while (i.hasNext()) {
	    Component c = (Component) i.next();
	    Iterator j = c.states.iterator();
	    while (j.hasNext()) {
		MLFAState s = (MLFAState) j.next();
		Iterator k = s.edges.iterator();
		while (k.hasNext()) {
		    Edge e = (Edge) k.next();
		    MLFAState w = e.dest;
		    if (w.component!=c)
			c.nexts.add(w.component);
		}
	    }
	}
	// propagate states of components from reachable components
	i = components.iterator();
	while (i.hasNext()) {
	    Component c = (Component) i.next();
	    Set done = new HashSet();
	    TreeSet pending = new TreeSet();
	    pending.add(c);
	    while (!pending.isEmpty()) {
		Component d = (Component) pending.first();
		pending.remove(d);
		done.add(d);
		Iterator j = d.nexts.iterator();
		while (j.hasNext()) {
		    Component e = (Component) j.next();
		    c.states.addAll(e.states);
		    if (!done.contains(e))
			pending.add(e);
		}
	    }
	}
    }

    private int searchc(MLFAState s, int count, List components, Stack stack)
    {
	s.newmark = false;
	s.lowlink = s.dfnumber = count++;
	stack.push(s);
	s.onstack = true;
	Iterator i = s.edges.iterator();
	while (i.hasNext()) {
	    Edge e = (Edge) i.next();
	    MLFAState w = e.dest;
	    if (w.newmark) {
		count = searchc(w, count, components, stack);
		if (w.lowlink<s.lowlink)
		    s.lowlink = w.lowlink;
	    } else if (w.dfnumber<s.dfnumber && w.onstack && w.dfnumber<s.lowlink)
		s.lowlink = w.dfnumber;
	}
	if (s.lowlink==s.dfnumber) {
	    Component c = new Component();
	    MLFAState x;
	    do {
		x = (MLFAState) stack.pop();
		x.onstack = false;
		x.reachable = c.states;
		c.states.add(x);
		x.component = c;
	    } while (x!=s);
	    components.add(c);
	}
	return count;
    }

}
