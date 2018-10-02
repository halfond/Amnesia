package dk.brics.string.mlfa;

import dk.brics.automaton.Automaton;

/** Abstract superclass for binary operations. */
abstract public class BinaryOperation extends Operation
{
    /** Constructor, should be invoked by subclasses. */
    protected BinaryOperation()
    {
	super();
    }

    /**
     * Binary operation on automata. 
     * @param a1 first input automaton, should not be modified
     * @param a2 second input automaton, should not be modified
     * @return output automaton
     */
    abstract public Automaton op(Automaton a1, Automaton a2);

    /** Transfer function for character set analysis. */
    abstract public CharSet charsetTransfer(CharSet a1, CharSet a2);
}
