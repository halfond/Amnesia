package dk.brics.string.operations;

import dk.brics.automaton.Automaton;
import dk.brics.string.mlfa.BinaryOperation;
import dk.brics.string.mlfa.CharSet;

/** Automaton operation for {@link StringBuffer#replace(int,int,String)}. */
public class Replace5 extends BinaryOperation
{
    /** Constructs new operation object. */
    public Replace5() {}

    /** 
     * Automaton operation. 
     * See {@link Insert#op(Automaton,Automaton) Insert.op(Automaton,Automaton)}.
     * @param a first input automaton
     * @param b second input automaton
     * @return resulting automaton
     */
    public Automaton op(Automaton a, Automaton b) 
    {
	return Insert.pop(a, b);
    }

    public String toString()
    {
	return "replace5";
    }

    public int getPriority()
    {
	return 6;
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
	return obj instanceof Replace5;
    }
}
