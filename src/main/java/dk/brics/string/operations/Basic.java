package dk.brics.string.operations;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

/** Basic automata. */
public class Basic
{
    static Automaton emptyString, noString, anyString;
    static Automaton booleanString, characterString, doubleString, floatString, integerString, longString;

    static {
	emptyString = Automaton.makeEmptyString();
	emptyString.setInfo("\"\"");
	noString = Automaton.makeEmpty();
	noString.setInfo("<no string>");
	anyString = Automaton.makeAnyString();
	anyString.setInfo("<any string>");
	booleanString = Automaton.makeString("true").union(Automaton.makeString("false"));
	booleanString.setInfo("\"true\"|\"false\"");
	characterString = Automaton.makeAnyChar();
	characterString.setInfo("<char>");
	Automaton t0 = Automaton.makeCharRange('1', '9').concatenate(Automaton.makeCharRange('0', '9').repeat(0));
	Automaton t1 = Automaton.makeChar('0').union(Automaton.makeChar('-').optional().concatenate(t0));
	t1.minimize();
	t1.setInfo("<int>");
	integerString = t1;
	longString = t1;
	Automaton t2 = t0.concatenate(Automaton.makeChar('.')).concatenate(t0);
	Automaton t3 = Automaton.makeChar('E').concatenate(integerString).optional();
	Automaton t4 = t2.concatenate(t3).union(Automaton.makeString("Infinity"));
	Automaton t5 = Automaton.makeChar('-').optional().concatenate(t4);
	Automaton t6 = t5.union(Automaton.makeString("NaN"));
	t6.minimize();
	t6.setInfo("<float>");
	floatString = t6;
	doubleString = t6;
    }

    private Basic() {}

    /** Returns automaton for the empty string. */
    public static Automaton makeEmptyString()
    {
	return emptyString;
    }

    /** Returns automaton for any string.*/
    public static Automaton makeAnyString()
    {
	return anyString;
    }

    /** Returns automaton for no string.*/
    public static Automaton makeNoString()
    {
	return noString;
    }

    /** Returns automaton for string values of <tt>Boolean</tt>.*/
    public static Automaton makeBooleanString()
    {
	return booleanString;
    }

    /** Returns automaton for string values of <tt>Character</tt>.*/
    public static Automaton makeCharacterString()
    {
	return characterString;
    }

    /** Returns automaton for string values of <tt>Double</tt>.*/
    public static Automaton makeDoubleString()
    {
	return doubleString;
    }

    /** Returns automaton for string values of <tt>Float</tt>.*/
    public static Automaton makeFloatString()
    {
	return floatString;
    }

    /** Returns automaton for string values of <tt>Byte</tt>.*/
    public static Automaton makeByteString()
    {
	return integerString;
    }

    /** Returns automaton for string values of <tt>Short</tt>.*/
    public static Automaton makeShortString()
    {
	return integerString;
    }

    /** Returns automaton for string values of <tt>Integer</tt>.*/
    public static Automaton makeIntegerString()
    {
	return integerString;
    }

    /** Returns automaton for string values of <tt>Long</tt>.*/
    public static Automaton makeLongString()
    {
	return longString;
    }

    static void escapeChar(char c, StringBuffer b)
    {
	if (c>=0x20 && c<=0x7e)
	    b.append(c);
	else {
	    b.append("\\u");
	    String t = Integer.toHexString(((int) c) & 0xffff);
	    for (int j=0; j+t.length()<4; j++)
		b.append('0');
	    b.append(t);
	}
    }

    static String escapeChar(char c)
    {
	StringBuffer b = new StringBuffer();
	escapeChar(c, b);
	return b.toString();
    }

    static String escapeString(String s)
    {
	StringBuffer b = new StringBuffer();
	b.append('"');
	for (int i=0; i<s.length(); i++)
	    escapeChar(s.charAt(i), b);
	b.append('"');
	return b.toString();
    }

    /** Returns automaton for the given constant string. */
    public static Automaton makeConstString(String s)
    {
	Automaton a = Automaton.makeString(s);
	a.setInfo(escapeString(s));
	return a;
    }

    /** Constructs name for the given automaton. */
    public static String getName(Automaton a)
    {
	Object info = a.getInfo();
	if (info==null)
	    return "<???>";
	else
	    return (String) info;
    }

    static Set findReachableStates(State s)
    {
	Set reachable = new HashSet();
	TreeSet pending = new TreeSet();
	pending.add(s);
	while (!pending.isEmpty()) {
	    State p = (State) pending.first();
	    pending.remove(p);
	    reachable.add(p);
	    Iterator i = p.getTransitions().iterator();
	    while (i.hasNext()) {
		Transition t = (Transition) i.next();
		State q = t.getDest();
		if (!reachable.contains(q))
		    pending.add(q);
	    }
	}
	return reachable;
    }
}
