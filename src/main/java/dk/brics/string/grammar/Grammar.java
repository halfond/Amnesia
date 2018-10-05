package dk.brics.string.grammar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import dk.brics.automaton.Automaton;
import dk.brics.string.mlfa.BinaryOperation;
import dk.brics.string.mlfa.CharSet;
import dk.brics.string.mlfa.MLFA;
import dk.brics.string.mlfa.MLFAState;
import dk.brics.string.mlfa.Operation;
import dk.brics.string.mlfa.UnaryOperation;

/** 
 * Context-free grammar with regular operations. 
 * @author Anders M&oslash;ller &lt;<a href="mailto:amoeller@brics.dk">amoeller@brics.dk</a>&gt;
 */
public class Grammar
{
    Set nonterminals;
    List components;
    int next_number;

    /** Constructs new empty grammar. */
    public Grammar()
    {
	nonterminals = new HashSet();
    }

    /** Adds new nonterminal. */
    public Nonterminal newNonterminal()
    {
	Nonterminal n = new Nonterminal(++next_number);
	nonterminals.add(n);
	return n;
    }

    Nonterminal newNonterminal(Component c)
    {
	Nonterminal n = newNonterminal();
	c.add(n);
	return n;
    }

    /** Returns set of nonterminals in this grammar. */
    public Set getNonterminals()
    {
	return nonterminals;
    }

    /** Adds new unit production [<tt>a -> b</tt>]. */
    public void addUnitProduction(Nonterminal a, Nonterminal b)
    {
	if (a!=b)
	    a.productions.add(new UnitProduction(b));
    }

    /** Adds new pair production [<tt>a -> b c</tt>]. */
    public void addPairProduction(Nonterminal a, Nonterminal b, Nonterminal c)
    {
 	a.productions.add(new PairProduction(b, c));
    }

    /** Adds new automaton production [<tt>a -> reg</tt>]. */
    public void addAutomatonProduction(Nonterminal a, Automaton n)
    {
	if (!n.isEmpty())
	    a.productions.add(new AutomatonProduction(n));
    }

    /** Adds new epsilon production [<tt>a -> ""</tt>]. */
    public void addEpsilonProduction(Nonterminal a)
    {
 	a.productions.add(new EpsilonProduction());
    }

    /** Adds new unary operation production [<tt>a -> op1(b)</tt>]. */
    public void addUnaryProduction(Nonterminal a, UnaryOperation op, Nonterminal b)
    {
 	a.productions.add(new UnaryProduction(op, b));
    }

    /** Adds new binary operation production [<tt>a -> op2(b,c)</tt>]. */
    public void addBinaryProduction(Nonterminal a, BinaryOperation op, Nonterminal b, Nonterminal c)
    {
 	a.productions.add(new BinaryProduction(op, b, c));
    }

    /** 
     * Approximates operation cycles.
     * An operation cycle is an occurrence of a production [<tt>a -> op1(b)</tt>] or [<tt>a -> op2(b,c)</tt>]
     * where <tt>b</tt> or <tt>c</tt> recursively refers to <tt>a</tt>.
     */
    public void approximateOperationCycles()
    {
	findComponents();
	findCharsets();
	boolean done = false;
	while (!done) {
	    done = true;
	    Iterator i = components.iterator();
	    while (i.hasNext()) {
		Component c = (Component) i.next();
		int cycles = 0;
		Nonterminal maxn = null;
		Production maxp = null;
		// look for operation cycles in this component
		Iterator j = c.nonterminals.iterator();
		while (j.hasNext()) {
		    Nonterminal n = (Nonterminal) j.next();
		    Iterator k = n.productions.iterator();
		    while (k.hasNext()) {
			Production p = (Production) k.next();
			if (p.isOperationCycle(c)) {
			    Operation p_op = p.getOperation();
			    if (cycles==0 || p_op.getPriority()>maxp.getOperation().getPriority() ||
				(p_op.getPriority()==maxp.getOperation().getPriority() && 
				 p_op.getNumber()>maxp.getOperation().getNumber())) { // assume that the operations are different objects
				maxn = n;
				maxp = p;
			    }
			    cycles++;
			}
		    }
		}
		if (cycles>0) {
		    if (cycles>1)
			done = false;
		    // replace  A->op  with  A->automatonproduction(charset(op))
		    Automaton a = maxp.charsetTransfer().toAutomaton();
		    maxn.productions.remove(maxp);
		    maxn.productions.add(new AutomatonProduction(a));
		}
	    }
	    if (!done)
		findComponents();
	}
    }

    /** 
     * Returns number of operation cycles.
     */
    public int getNumberOfOperationCycles()
    {
	int cycles = 0;
	findComponents();
	Iterator i = components.iterator();
	while (i.hasNext()) {
	    Component c = (Component) i.next();
	    Iterator j = c.nonterminals.iterator();
	    while (j.hasNext()) {
		Nonterminal n = (Nonterminal) j.next();
		Iterator k = n.productions.iterator();
		while (k.hasNext()) {
		    Production p = (Production) k.next();
		    if (p.isOperationCycle(c))
			cycles++;
		}
	    }
	}
	return cycles;
    }

    /** 
     * Returns number of non-regular components.
     */
    public int getNumberOfNonRegularComponents()
    {
	int nonreg = 0;
	findComponents();
	Iterator i = components.iterator();
	while (i.hasNext()) {
	    Component c = (Component) i.next();
	    if (c.recursion==Component.BOTH)
		nonreg++;
	}
	return nonreg;
    }

    /** Finds charsets for all nonterminals, assumes that components have been found. */
    private void findCharsets()
    {
	Iterator i = components.iterator();
	while (i.hasNext())
	    findCharsets((Component) i.next());
    }

    /** Finds charsets for all nonterminals in the given component and in all reachable components. */
    private void findCharsets(Component c)
    {
	// reset charsets, find prevs, make sure reachable components are processed
	Iterator i = c.nonterminals.iterator();
	while (i.hasNext()) {
	    Nonterminal n = (Nonterminal) i.next();
	    n.charset = new CharSet();
	    n.findPrevs();
	    Iterator j = n.nexts.iterator();
	    while (j.hasNext()) {
		Nonterminal m = (Nonterminal) j.next();
		if (m.component!=c && m.charset==null)
		    findCharsets(m.component);
	    }
	}
	// fixpoint iteration
	TreeSet worklist = new TreeSet(c.nonterminals);
	while (!worklist.isEmpty()) {
	    Nonterminal n = (Nonterminal) worklist.first();
	    worklist.remove(n);
	    if (n.updateCharset())
		worklist.addAll(n.prevs);
	}
    }

    /** 
     * Returns string representation of character sets for all nonterminals. 
     */
    public String getCharsets()
    {
	findCharsets();
	StringBuffer b = new StringBuffer();
	Iterator i = nonterminals.iterator();
	while (i.hasNext()) {
	    Nonterminal n = (Nonterminal) i.next();
	    b.append(n).append(": ").append(n.charset).append("\n");
	}
	return b.toString();
    }

    /** 
     * Performs Mohri-Nederhof regular approximation on this grammar.
     * The grammar is assumed to have no operation cycles (see {@link #approximateOperationCycles()}).
     * @param hotspots nonterminals that correspond to expressions where automata 
     *                 are extracted later
     */
    public void approximateNonRegular(Set hotspots)
    {
	findComponents();
	// find nonterminals that need epsilons
	Iterator i = nonterminals.iterator();
	while (i.hasNext()) {
	    Nonterminal a = (Nonterminal) i.next();
	    if (hotspots.contains(a))
		a.need_epsilon = true;
	    Iterator j = a.nexts.iterator();
	    while (j.hasNext()) {
		Nonterminal b = (Nonterminal) j.next();
		if (a.component!=b.component)
		    b.need_epsilon = true;
	    }
	}
	// find components that have both right and left recursion
	i = components.iterator();
	while (i.hasNext()) {
	    Component c = (Component) i.next();
	    if (c.recursion==Component.BOTH) {
		Set nonterminals = (Set) c.nonterminals.clone();
		// make primed nonterminals and epsilon transitions
		Iterator j = nonterminals.iterator();
		while (j.hasNext()) {
		    Nonterminal a = (Nonterminal) j.next();
		    a.oldproductions = a.productions;
		    a.productions = new HashSet();
		    // A' -> ""  if hotspot or used by other component
		    a.primed = newNonterminal(c);
		    if (a.need_epsilon)
			addEpsilonProduction(a.primed);
		}
		// make new productions and extra nonterminals for each original nonterminal
		j = nonterminals.iterator();
		while (j.hasNext()) {
		    Nonterminal a = (Nonterminal) j.next();
		    Iterator k = a.oldproductions.iterator();
		    while (k.hasNext()) {
			Production p = (Production) k.next();
			if (p instanceof UnitProduction) {
			    UnitProduction pp = (UnitProduction) p;
			    if (c.contains(pp.b)) { 
				// A -> B  =>  A -> B, B' -> A'
				addUnitProduction(a, pp.b);
				addUnitProduction(pp.b.primed, a.primed);
			    } else {
				// A -> X  =>  A -> X A'
				addPairProduction(a, pp.b, a.primed);
			    }
			} else if (p instanceof PairProduction) {
			    PairProduction pp = (PairProduction) p;
			    if (c.contains(pp.b)) {
				if (c.contains(pp.c)) {
				    // A -> B C  =>  A -> B, B' -> C, C' -> A'
				    addUnitProduction(a, pp.b);
				    addUnitProduction(pp.b.primed, pp.c);
				    addUnitProduction(pp.c.primed, a.primed);
				} else {
				    // A -> B X  =>  A -> B, B' -> X A'
				    addUnitProduction(a, pp.b);
				    addPairProduction(pp.b.primed, pp.c, a.primed);
				}
			    } else {
				if (c.contains(pp.c)) {
				    // A -> X B  =>  A -> X B, B' -> A'
				    addPairProduction(a, pp.b, pp.c);
				    addUnitProduction(pp.c.primed, a.primed);
				} else {
				    // A -> X Y  =>  A -> R A', R -> X Y
				    Nonterminal r = newNonterminal(c);
				    addPairProduction(a, r, a.primed);
				    addPairProduction(r, pp.b, pp.c);
				}
			    }
			} else if (p instanceof AutomatonProduction) {
			    AutomatonProduction pp = (AutomatonProduction) p;
			    // A -> reg  =>  A -> R A', R -> reg
			    Nonterminal r = newNonterminal(c);
			    addPairProduction(a, r, a.primed);
			    addAutomatonProduction(r, pp.n);
			} else if (p instanceof UnaryProduction) {
			    UnaryProduction pp = (UnaryProduction) p;
			    // A -> op1(X)  =>  A -> R A', R -> op1(X)
			    Nonterminal r = newNonterminal(c);
			    addPairProduction(a, r, a.primed);
			    addUnaryProduction(r, pp.op, pp.b);
			} else if (p instanceof BinaryProduction) {
			    BinaryProduction pp = (BinaryProduction) p;
			    // A -> op2(X,Y)  =>  A -> R A', R -> op2(X,Y)
			    Nonterminal r = newNonterminal(c);
			    addPairProduction(a, r, a.primed);
			    addBinaryProduction(r, pp.op, pp.b, pp.c);
			} else if (p instanceof EpsilonProduction)
			    addEpsilonProduction(a);
		    }
		}
		// now this component only has right recursion
		c.recursion = Component.RIGHT;
	    }
	}
    }
    
    /** Finds strongly connected components using Aho-Hopcroft-Ullman. */
    void findComponents()
    {
	components = new ArrayList();
	Iterator i = nonterminals.iterator();
	while (i.hasNext()) {
	    Nonterminal v = (Nonterminal) i.next();
	    v.resetComponentInfo();
	}
	Stack stack = new Stack();
	i = nonterminals.iterator();
	while (i.hasNext()) {
	    Nonterminal v = (Nonterminal) i.next();
	    if (v.newmark)
		searchc(v, 0, components, stack);
	}
    }

    private int searchc(Nonterminal v, int count, List components, Stack stack)
    {
	v.newmark = false;
	v.lowlink = v.dfnumber = count++;
	stack.push(v);
	v.onstack = true;
	Iterator i = v.nexts.iterator();
	while (i.hasNext()) {
	    Nonterminal w = (Nonterminal) i.next();
	    if (w.newmark) {
		count = searchc(w, count, components, stack);
		if (w.lowlink<v.lowlink)
		    v.lowlink = w.lowlink;
	    } else if (w.dfnumber<v.dfnumber && w.onstack && w.dfnumber<v.lowlink)
		v.lowlink = w.dfnumber;
	}
	if (v.lowlink==v.dfnumber) {
	    Component c = new Component();
	    components.add(c);
	    Nonterminal x;
	    do {
		x = (Nonterminal) stack.pop();
		x.onstack = false;
		c.add(x);
	    } while (x!=v);
	    c.findRecursion();
	}
	return count;
    }

    /** Returns number of nonterminals in this grammar. */
    public int getNumberOfNonterminals()
    {
	return nonterminals.size();
    }

    /** Returns number of productions in this grammar. */
    public int getNumberOfProductions()
    {
	int x = 0;	
	Iterator i = nonterminals.iterator();
	while (i.hasNext())
	    x += ((Nonterminal) i.next()).productions.size();
	return x;
    }

    /** 
     * Returns number of components.
     */
    public int getNumberOfComponents()
    {
	findComponents();
	return components.size();
    }


    /** Returns string representation of this grammar. */
    public String toString()
    {
	StringBuffer s = new StringBuffer();
	Iterator i = nonterminals.iterator();
	while (i.hasNext()) {
	    Nonterminal v = (Nonterminal) i.next();
	    Iterator j = v.productions.iterator();
	    while (j.hasNext()) {
		Production p = (Production) j.next();
		s.append(v).append(" -> ").append(p).append("\n");
	    }
	}
	return s.toString();
    }

    /** 
     * Constructs {@link MLFA} from this grammar. 
     * The grammar is assumed to be approximated first (see {@link #approximateNonRegular(Set)}).
     * @see Nonterminal#getMLFAStatePair()
     */
    public MLFA toMLFA()
    {
	if (components==null)
	    throw new RuntimeException("grammar has not been approximated");
	MLFA mlfa = new MLFA();
	Iterator i = components.iterator();
	while (i.hasNext())
	    ((Component) i.next()).state = null;
	i = components.iterator();
	while (i.hasNext())
	    convert((Component) i.next(), mlfa);
	return mlfa;
    }

    void convert(Component c, MLFA mlfa)
    {
	if (c.state==null) {
	    // make states
	    c.state = mlfa.newState();
	    Iterator i = c.nonterminals.iterator();
	    while (i.hasNext()) {
		Nonterminal a = (Nonterminal) i.next();
		a.state = mlfa.newState();
	    }
	    // make transitions
	    i = c.nonterminals.iterator();
	    while (i.hasNext()) {
		Nonterminal a = (Nonterminal) i.next();
		Iterator j = a.productions.iterator();
		while (j.hasNext()) {
		    Production p = (Production) j.next();
		    if (c.recursion==Component.BOTH)
			throw new RuntimeException("grammar is not strongly regular");
		    else if (c.recursion==Component.RIGHT || c.recursion==Component.NONE) {
			// component is right or non recursive
			if (p instanceof UnitProduction) {
			    UnitProduction pp = (UnitProduction) p;
			    if (c.contains(pp.b)) {
				// A -> B  =>  (A)--e-->(B)
				mlfa.addEpsilonTransition(a.state, pp.b.state);
			    } else {
				// A -> X  =>  (A)--[X]-->(T)
				convert(pp.b.component, mlfa);
				mlfa.addIdentityTransition(a.state, c.state, pp.b.getMLFAStatePair());
			    }
			} else if (p instanceof PairProduction) {
			    PairProduction pp = (PairProduction) p;
			    if (c.contains(pp.c)) {
				// A -> X B  =>  (A)--[X]-->(B)
				convert(pp.b.component, mlfa);
				mlfa.addIdentityTransition(a.state, pp.c.state, pp.b.getMLFAStatePair());
			    } else {
				// A -> X Y  =>  (A)--[X]-->(R), (R)--[Y]-->(T)
				MLFAState r = mlfa.newState();
				convert(pp.b.component, mlfa);
				convert(pp.c.component, mlfa);
				mlfa.addIdentityTransition(a.state, r, pp.b.getMLFAStatePair());
				mlfa.addIdentityTransition(r, c.state, pp.c.getMLFAStatePair());
			    }
			} else if (p instanceof AutomatonProduction) {
			    AutomatonProduction pp = (AutomatonProduction) p;
			    // A -> reg  =>  (A)--[reg]-->(T)
			    mlfa.addAutomatonTransition(a.state, c.state, pp.n);
			} else if (p instanceof UnaryProduction) {
			    UnaryProduction pp = (UnaryProduction) p;
			    // A -> op1(X)  =>  (A)--[op1(X)]-->(T)
			    convert(pp.b.component, mlfa);
			    mlfa.addUnaryTransition(a.state, c.state, pp.op, pp.b.getMLFAStatePair());
			} else if (p instanceof BinaryProduction) {
			    BinaryProduction pp = (BinaryProduction) p;
			    // A -> op2(X,Y)  =>  (A)--[op2(X,Y)]-->(T)
			    convert(pp.b.component, mlfa);
			    convert(pp.c.component, mlfa);
			    mlfa.addBinaryTransition(a.state, c.state, pp.op,
						     pp.b.getMLFAStatePair(), pp.c.getMLFAStatePair());
			} else if (p instanceof EpsilonProduction) {
			    EpsilonProduction pp = (EpsilonProduction) p;
			    // A -> ""  =>  (A)--e-->(T)
			    mlfa.addEpsilonTransition(a.state, c.state);
			}
		    } else {
			// component is left recursive
			if (p instanceof UnitProduction) {
			    UnitProduction pp = (UnitProduction) p;
			    if (c.contains(pp.b)) {
				// A -> B  =>  (B)---e--->(A)
				mlfa.addEpsilonTransition(pp.b.state, a.state);
			    } else {
				// A -> X  =>  (S)--[X]-->(A)
				convert(pp.b.component, mlfa);
				mlfa.addIdentityTransition(c.state, a.state, pp.b.getMLFAStatePair());
			    }
			} else if (p instanceof PairProduction) {
			    PairProduction pp = (PairProduction) p;
			    if (c.contains(pp.b)) {
				// A -> B X  =>  (B)--[X]-->(A)
				convert(pp.c.component, mlfa);
				mlfa.addIdentityTransition(pp.b.state, a.state, pp.c.getMLFAStatePair());
			    } else {
				// A -> X Y  =>  (S)--[X]-->(R), (R)--[Y]-->(A)
				MLFAState r = mlfa.newState();
				convert(pp.b.component, mlfa);
				convert(pp.c.component, mlfa);
				mlfa.addIdentityTransition(c.state, r, pp.b.getMLFAStatePair());
				mlfa.addIdentityTransition(r, a.state, pp.c.getMLFAStatePair());
			    }
			} else if (p instanceof AutomatonProduction) {
			    AutomatonProduction pp = (AutomatonProduction) p;
			    // A -> reg  =>  (S)--[reg]-->(A)
			    mlfa.addAutomatonTransition(c.state, a.state, pp.n);
			} else if (p instanceof UnaryProduction) {
			    UnaryProduction pp = (UnaryProduction) p;
			    // A -> op1(X)  =>  (S)--[op1(X)]-->(A)
			    convert(pp.b.component, mlfa);
			    mlfa.addUnaryTransition(c.state, a.state, pp.op, pp.b.getMLFAStatePair());
			} else if (p instanceof BinaryProduction) {
			    BinaryProduction pp = (BinaryProduction) p;
			    // A -> op2(X,Y)  =>  (S)--[op2(X,Y)]-->(A)
			    convert(pp.b.component, mlfa);
			    convert(pp.c.component, mlfa);
			    mlfa.addBinaryTransition(c.state, a.state, pp.op,
						     pp.b.getMLFAStatePair(), pp.c.getMLFAStatePair());
			} else if (p instanceof EpsilonProduction) {
			    EpsilonProduction pp = (EpsilonProduction) p;
			    // A -> ""  =>  (S)---e--->(A)
			    mlfa.addEpsilonTransition(c.state, a.state);
			}
		    }
		}
	    }
	}
    }
}
