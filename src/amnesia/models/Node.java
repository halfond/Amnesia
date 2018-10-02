package amnesia.models;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import sql.models.TokenTransition;

import dk.brics.automaton.State;

public class Node {
	private static int globalID=0;
	
	private Set inEdges, outEdges;
	private boolean accept = false;
	private int id;
	private long numPath=0;
	
	public Node(State s) {
		id=globalID;
        globalID++;
		inEdges = new HashSet();
		outEdges = new HashSet();
	}
		
	public void addOutTransition(Edge t) {
		outEdges.add(t);
	}
	
	public void addInTransition(Edge t) {
		inEdges.add(t);
	}
	
	public Set next(SQLToken st) {
		Set nextSet= new HashSet();
        Iterator it = outEdges.iterator();
        while (it.hasNext()) {
            Edge outEdge = (Edge)it.next();
            if (outEdge.matches(st)) {
                nextSet.add(outEdge);
            }
        }
        return nextSet;
	}
	
	public String toString() {
		return null;
	}
	
	public String toDot() {
		return null;
	}
	
	public boolean isAccept() {
		return accept;
	}
	
	public long getNumPath() {
        return numPath;
    }

    public void setNumPath(long np) {
        numPath = np;
    }
    
    public int getID() {
    	return id;
    }
}
