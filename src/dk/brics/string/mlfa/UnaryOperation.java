package dk.brics.string.mlfa;

import dk.brics.automaton.Automaton;

/** Abstract superclass for unary operations. */
abstract public class UnaryOperation extends Operation
{
    /** Constructor, should be invoked by subclasses. */
    protected UnaryOperation()
    {
	super();
    }

    /** 
     * Unary operation on automata. 
     * @param a input automaton, should not be modified
     * @return output automaton
     */
    abstract public Automaton op(Automaton a);

    /** Transfer function for character set analysis. */
    abstract public CharSet charsetTransfer(CharSet a);
}
