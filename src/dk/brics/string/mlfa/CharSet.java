package dk.brics.string.mlfa;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

/** Character set. */
public class CharSet
{
    HashSet intervals = new HashSet();

    /** Constructs new empty character set. */
    public CharSet() {}

    /** Returns new character set with all characters in strings in the given language. */
    public CharSet(Automaton a)
    {
	Automaton b = a.singleChars();
	Iterator i = b.getInitialState().getTransitions().iterator();
	while (i.hasNext()) {
	    Transition t = (Transition) i.next();
	    intervals.add(new Interval(t.getMin(), t.getMax()));
	}
    }

    /** Clones this character set. */
    public Object clone()
    {
	CharSet a = new CharSet();
	a.intervals = (HashSet) intervals.clone();
	return a;
    }

    /** Checks for equality with another character set object. */
    public boolean equals(Object obj)
    {
	if (obj instanceof CharSet) {
	    CharSet a = (CharSet) obj;
	    return intervals.equals(a.intervals);	    
	} else
	    return false;
    }

    /** Returns string representation of this character set. */
    public String toString()
    {
	StringBuffer b = new StringBuffer();
	Iterator i = intervals.iterator();
	while (i.hasNext()) {
	    Interval v = (Interval) i.next();
	    if (v.min==v.max)
		appendChar(b, v.min);
	    else {
		appendChar(b, v.min);
		b.append("-");
		appendChar(b, v.max);
	    }
	    if (i.hasNext()) 
		b.append(",");
	}
	return b.toString();
    }

    private void appendChar(StringBuffer b, char c)
    {
	b.append('\'');
	if (c>=0x21 && c<=0x7e && c!='-')
	    b.append(c);
	else {
	    b.append("\\u");
	    String t = Integer.toHexString(((int) c) & 0xffff);
	    for (int j=0; j+t.length()<4; j++)
		b.append('0');
	    b.append(t);
	}
	b.append('\'');
    }

    void reduce()
    {
	TreeSet s = new TreeSet(intervals);
	intervals.clear();
	int min = -1, max = -1;
	Iterator i = s.iterator();
	while (i.hasNext()) {
	    Interval v = (Interval) i.next();
	    if (v.min<=max+1) {
		if (v.max>max)
		    max = v.max;
	    } else {
		if (min>0)
		    intervals.add(new Interval((char) min, (char) max));
		min = v.min;
		max = v.max;
	    }
	}
	if (min>0)
	    intervals.add(new Interval((char) min, (char) max));
    }

    /** Returns new character set with every character. */
    public static CharSet makeAnychars()
    {
	CharSet a = new CharSet();
	a.intervals.add(new Interval(Character.MIN_VALUE, Character.MAX_VALUE));
	return a;
    }

    /** Constructs union of this character set and the given one. */
    public CharSet union(CharSet a)
    {
	CharSet b = (CharSet) clone();
	b.intervals.addAll((HashSet) a.intervals.clone());
	b.reduce();
	return b;
    }

    /** Constructs union of the given character sets. */
    public static CharSet union(List c)
    {
	CharSet a = new CharSet();
	Iterator i = c.iterator();
	while (i.hasNext()) {
	    CharSet b = (CharSet) i.next();
	    a.intervals.addAll((HashSet) b.intervals.clone());
	}
	a.reduce();
	return a;
    }

    /** Constructs character set as this one but removes the given character. */
    public CharSet remove(char c)
    {
	CharSet b = (CharSet) clone();
	Iterator i = intervals.iterator();
	while (i.hasNext()) {
	    Interval v = (Interval) i.next();
	    if (v.min<=c && c<=v.max) {
		b.intervals.remove(v);
		if (v.min<c)
		    b.intervals.add(new Interval(v.min, (char) (c-1)));
		if (c<v.max)
		    b.intervals.add(new Interval((char) (c+1), v.max));
	    }
	}
	return b;
    }

    /** Constructs character set as this one but adds the given character. */
    public CharSet add(char c)
    {
	CharSet b = (CharSet) clone();
	b.intervals.add(new Interval(c));
	b.reduce();
	return b;
    }

    boolean isTotal()
    {
	if (intervals.size()==1) {
	    Interval v = (Interval) intervals.iterator().next();
	    return v.min==Character.MIN_VALUE && v.max==Character.MAX_VALUE;
	} else 
	    return false;
    }

    /** Constructs character set as this one and performs uppercase conversion of all characters. */
    public CharSet toLowerCase()
    {
	if (isTotal())
	    return (CharSet) clone();
	CharSet b = new CharSet();
	Iterator i = intervals.iterator();
	while (i.hasNext()) {
	    Interval v = (Interval) i.next();
	    for (char c = v.min; c<=v.max; c++) 
		b.intervals.add(new Interval(Character.toLowerCase(c)));
	}
	b.reduce();
	return b;
    }

    /** Constructs character set as this one and performs lowercase conversion of all characters. */
    public CharSet toUpperCase()
    {
	if (isTotal())
	    return (CharSet) clone();
	CharSet b = new CharSet();
	Iterator i = intervals.iterator();
	while (i.hasNext()) {
	    Interval v = (Interval) i.next();
	    for (char c = v.min; c<=v.max; c++) 
		b.intervals.add(new Interval(Character.toUpperCase(c)));
	}
	b.reduce();
	return b;
    }

    /** Constructs automaton accepting strings with zero or more characters from this set. */
    public Automaton toAutomaton()
    {
	Automaton a = new Automaton();
	State s = a.getInitialState();
	s.setAccept(true);
	Iterator i = intervals.iterator();
	while (i.hasNext()) {
	    Interval v = (Interval) i.next();
	    s.addTransition(new Transition(v.min, v.max, s));
	}
	return a;
    }

    /** Checks whether a particular characters is in the set. */
    public boolean contains(char c)
    {
	Iterator i = intervals.iterator();
	while (i.hasNext()) {
	    Interval v = (Interval) i.next();
	    if (v.min<=c && c<=v.max)
		return true;
	}
	return false;
    }
}

class Interval implements Comparable
{
    char min, max;

    Interval(char c)
    {
	min = max = c;
    }

    Interval(char min, char max)
    {
	if (max<min) {
	    char t = max;
	    max = min;
	    min = t;
	}
	this.min = min;
	this.max = max;
    }

    public boolean equals(Object obj)
    {
	if (obj instanceof Interval) {
	    Interval t = (Interval) obj;
	    return t.min==min && t.max==max;
	} else
	    return false;
    }

    public int hashCode()
    {
	return min*2+max*3;
    }
    
    public Object clone()
    {
	return new Interval(min, max);
    }

    public int compareTo(Object o)
    {
	if (o instanceof Interval) {
	    Interval v = (Interval) o;
	    if (min<v.min || (min==v.min && max<v.max))
		return -1;
	    if (v.min<min || (v.min==min && v.max<max))
		return 1;
	    return 0;
	} else
	    throw new ClassCastException();
    }
}
