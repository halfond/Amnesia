package dk.brics.string.grammar;

import java.util.HashSet;
import java.util.Iterator;

import dk.brics.string.mlfa.MLFAState;

/** Strongly connected component. */
class Component
{
    static final int NONE = 0;  // 00
    static final int RIGHT = 1; // 01
    static final int LEFT = 2;  // 10
    static final int BOTH = 3;  // 11

    HashSet nonterminals = new HashSet();
    int recursion;

    MLFAState state;

    void findRecursion()
    {
	recursion = NONE;
	Iterator i = nonterminals.iterator();
	while (i.hasNext()) {
	    Nonterminal n = (Nonterminal) i.next();
	    Iterator j = n.productions.iterator();
	    while (j.hasNext()) {
		Production p = (Production) j.next();
		if (p instanceof PairProduction) {
		    PairProduction pp = (PairProduction) p;
		    if (nonterminals.contains(pp.b))
			recursion |= LEFT;
		    if (nonterminals.contains(pp.c))
			recursion |= RIGHT;
		}
	    }
	}
    }

    void add(Nonterminal x)
    {
	nonterminals.add(x);
	x.component = this;
    }

    boolean contains(Nonterminal x)
    {
	return nonterminals.contains(x);
    }
}
