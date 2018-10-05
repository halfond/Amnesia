package dk.brics.string.mlfa;

/** Abstract superclass for operations on standard automata. */
abstract public class Operation
{
    static int count;
    int number;

    Operation()
    {
	number = count++;
    }

    /** Returns name of this operation. */
    abstract public String toString();

    /**
     * Returns priority of this operation. 
     * When approximating operation loops in grammars, operations with 
     * high priority are considered first.
     */
    abstract public int getPriority();

    /** Returns construction number of this operation. */
    public int getNumber()
    {
	return number;
    }
}
