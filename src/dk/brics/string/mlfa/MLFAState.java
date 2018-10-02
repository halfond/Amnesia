package dk.brics.string.mlfa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** MLFA state. */
public class MLFAState
{
    List edges = new ArrayList();

    Component component;
    Collection reachable;

    int dfnumber, lowlink;
    boolean newmark, onstack;

    MLFAState() {}

    void addTransition(MLFATransition t, MLFAState dest)
    {
	edges.add(new Edge(t,dest));
    }

    List getEdges()
    {
	return edges;
    }
    /** Returns name of this state. */
    public String toString()
    {
	String s = super.toString();
	return "S"+s.substring(s.indexOf('@')+1);
    }
}

class Edge
{
    MLFATransition t;
    MLFAState dest;

    Edge(MLFATransition t, MLFAState dest)
    {
	this.t = t;
	this.dest = dest;
    }
}

class Component implements Comparable
{
    List states = new ArrayList();
    Set nexts = new HashSet();
    
    int number;
    static int next_number;

    Component()
    {
	number = next_number++;
    }

    public int compareTo(Object obj)
    {
	return ((Component) obj).number-number;
    }
}
