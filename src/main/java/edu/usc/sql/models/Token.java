/*
 * Copyright 2005
 * Georgia Tech Research Corporation
 * Atlanta, GA  30332-0415
 * All Rights Reserved
 */

package edu.usc.sql.models;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import dk.brics.automaton.State;

public class Token implements Serializable{
    private static final long serialVersionUID = 1L;

    protected Set<TokenTransition> outTransitions, inTransitions;
    protected boolean acceptState=false;
    protected int id;
    protected boolean initialState=false;
    protected static int globalID=0;
    
    private long numPath=0;
    
    
    public Token() {
        id=globalID;
        globalID++;
        outTransitions = new HashSet<TokenTransition>();
        inTransitions = new HashSet<TokenTransition>();
    }
    
    public Token(State s) {
        acceptState=s.isAccept();
        outTransitions = new HashSet<TokenTransition>();
        inTransitions = new HashSet<TokenTransition>();
        id=s.getID();
        //TODO: check if ID is already taken
    }
    
    public boolean isAccept() {
        return acceptState;
    }
    
    public void setAccept(boolean a) {
        acceptState=a;
    }
    
    public int getID() {
        return id;
    }
    
    public Set getTransitions() {
        return outTransitions;
    }
    
    public void setInitial() {
        initialState=true;
    }
    
    public void addOutTransition(TokenTransition t) {
        if (outTransitions.add(t)) {
            t.setSource(this);
        }
    }
    
    public void addOutTransition(Set transitions) {
        Iterator it = transitions.iterator();
        while (it.hasNext()) {
            TokenTransition t = (TokenTransition)it.next();
            addOutTransition(t);
        }
    }
    
    public void setOutTransitions(Collection<TokenTransition> transitions) {
        outTransitions.clear();
        outTransitions.addAll(transitions);
    }
    
    public void addInTransition(TokenTransition t) {
        if (inTransitions.add(t)) {
           // t.setDest(this);
        }
    }
    
    public void addInTransition(Set transitions) {
        Iterator it = transitions.iterator();
        while (it.hasNext()) {
            TokenTransition t = (TokenTransition)it.next();
            addInTransition(t);
        }
    }
    
    public void setInTransitions(Collection<TokenTransition> transitions) {
        inTransitions.clear();
        inTransitions.addAll(transitions);
    }
    
    public Set getInTransitions() {
    	return inTransitions;
    }
    
    public Set getOutTransitions() {
        return outTransitions;
    }
    
    public void addTransition(TokenTransition tt) {
    	outTransitions.add(tt);
    	tt.getDest().inTransitions.add(tt);
    }
    
    public Map<String, Set<Token>> getOutMap() {
    	Map<String, Set<Token>> outMap = new HashMap<String, Set<Token>>();
    	
    	for (TokenTransition tt:outTransitions) {
    		Set<Token> tokens = outMap.get(tt.getLabel());
    		if (tokens == null) {
    			tokens = new HashSet<Token>();
    		}
    		tokens.add(tt.getDest());
    		outMap.put(tt.getLabel(), tokens);
    	}
    	return outMap;
    }
    
    public Set next(TokenTransition t) {
        Set<TokenTransition> nextSet= new HashSet<TokenTransition>();
        Iterator it = outTransitions.iterator();
        while (it.hasNext()) {
            TokenTransition outTrans = (TokenTransition)it.next();
            if (outTrans.matches(t)) {
                nextSet.add(outTrans);
            }
        }
        return nextSet;
    }
    
    public String toString() {
        String temp="";
        if (initialState) {
            temp+="Initial ";
        }
        temp+="Node: ";
        if (isAccept()) {
            temp+="((" + getID() + "))";
        } else {
            temp+="(" + getID() + ")";
        }
//        Iterator j = outTransitions.iterator();
//        while (j.hasNext()) {
//            TokenTransition t = (TokenTransition) j.next();
//            temp+=t;
//        }
        return temp;
    }
    
    public String toDot() {
        String temp="";
        temp+="  " + getID();
        if (isAccept()) {
            //temp+=" [shape=doublecircle,label=\"" + getID() + "\"];\n";
            temp+=" [shape=doublecircle,label=\"" + id + "\"];\n";
        } else {
            //temp+=" [shape=circle,label=\"" + getID() + "\"];\n";
            temp+=" [shape=circle,label=\"" + id + "\"];\n";
        }
        if (initialState) {
            temp+="  initial [shape=plaintext,label=\"\"];\n";
            temp+="  initial -> " + getID() + ";\n";
        }
        Iterator j = outTransitions.iterator();
        while (j.hasNext()) {
            TokenTransition t = (TokenTransition) j.next();
            temp+=t.toDot();
        }
        return temp;
    }

    public String toGraphML() {
    	StringBuffer edge = new StringBuffer();
    	if (!acceptState) {
	    	Iterator inTrans = inTransitions.iterator();
	    	while (inTrans.hasNext()) {
	    		TokenTransition inTran = (TokenTransition)inTrans.next();
	    		Iterator outTrans = outTransitions.iterator();
	    		while (outTrans.hasNext()) {
	    			TokenTransition outTran = (TokenTransition)outTrans.next();
	    			edge.append(outTran.toGraphML());
	    			edge.append(" <edge source=\""+inTran.getID() +"\" target=\""+outTran.getID() +"\"/>\n");
	    		}
	    	}
    	}
    	return edge.toString()+"\n";
    }
    
    public long getNumPath() {
        return numPath;
    }

    public void setNumPath(long numPath) {
        this.numPath = numPath;
    }
}