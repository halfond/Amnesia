
package dk.brics.string.intermediate;

/** A variable or other intermediate value in the Java program.
 *  A variable has a type, which is either {@link java.lang.String},
 *  {@link java.lang.StringBuffer}, an array (of arbitrary dimension)
 *  of {@link java.lang.String}, or some irrelevant type.
 */
public class Variable {
    /** The variable has some irrelevant type */
    public static final int TYPE_NONE = 0;
    /** The variable has type {@link java.lang.String} */
    public static final int TYPE_STRING = 1;
    /** The variable has type {@link java.lang.StringBuffer} */
    public static final int TYPE_STRINGBUFFER = 2;
    /** The variable has type array (of arbitrary dimension)
     *  of {@link java.lang.String} */
    public static final int TYPE_ARRAY = 3;

    /** The type of the variable */
    public int type;

    private static int serial_counter = 0;
    private int serial;

    private synchronized int next_serial() {
	return ++serial_counter;
    }

    public Variable(int type) {
	this.type = type;
	serial = next_serial();
    }

    /** Returns a string representation of the variable.
     *  @return a unique identifier for the variable.
     */
    public String toString() {
	String n = "x";
	switch (type) {
	case TYPE_NONE:
	    n = "n";
	    break;
	case TYPE_STRING:
	    n = "s";
	    break;
	case TYPE_STRINGBUFFER:
	    n = "b";
	    break;
	case TYPE_ARRAY:
	    n = "a";
	    break;
	}
	return n+serial;
    }
}
