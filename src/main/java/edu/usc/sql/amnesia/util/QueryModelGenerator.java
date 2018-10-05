package edu.usc.sql.amnesia.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.usc.sql.amnesia.models.Edge;
import edu.usc.sql.amnesia.models.Node;
import edu.usc.sql.amnesia.models.SQLQueryModel;

import soot.ValueBox;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dk.brics.string.StringAnalysis;

public class QueryModelGenerator {
	private Set keywords = null;
	private Set ops =  null;
	
	private Set white, black, gray, states, transitions;
	private Node initialState;
	private Map processedStates;
	private boolean userDefinedFlag = false;
	
	public QueryModelGenerator() {
		keywords = AmnesiaConstants.DEFAULT_KEYWORDS;
		ops = AmnesiaConstants.DEFAULT_OPERATORS;
	}
	
	
	public SQLQueryModel convert (StringAnalysis sa, Automaton aut, ValueBox b) {
		String cn = sa.getClassName(b);
        int line = sa.getLineNumber(b);
        String name = cn + line;
        
        states = new HashSet();
		transitions = new HashSet();
		processedStates=new HashMap();
		white = new HashSet(aut.getStates());
		gray = new HashSet();
		black = new HashSet();
		
		initialState=new Node(aut.getInitialState());
		states.add(initialState);
        
		dfsVisit(initialState, aut.getInitialState(), "", Edge.NONE, false, false, new HashSet());
        
		return null;
	}
	
		
	
	private void dfsVisit(Node p, State s, String labelText, int currTokenType, boolean inQuote, boolean foundSlash, Set currentStates) {
		white.remove(s);
		gray.add(s);
		fixState(s);
		Set edges = s.getTransitions();
		Iterator it = edges.iterator();

		Node nextP = p;
		boolean nextFoundSlash=foundSlash;
		int nextCurrTokenType=currTokenType;
		String nextLabelText = "";
		Set nextCurrentStates = new HashSet();
		
		if (((currTokenType != Edge.TEXTFIELD) && (currTokenType != Edge.NONE)) || edges.size()==0) {
			nextP = addState(s);
			addTransition(p, nextP, labelText, currTokenType);
			processedStates.put(new Integer(s.getID()), nextP);
			updateProcessedStates(currentStates, p);			
		}
		
		boolean endTextField=false;
		while (it.hasNext()) {
			Transition currTransition = (Transition) it.next();
			State nextState = currTransition.getDest();
			boolean nextInQuote=inQuote;
			
			if (isRangedTransition(currTransition)) {
				nextCurrTokenType=Edge.VAR;
				userDefinedFlag=true;
				nextLabelText="VAR";
			} else {
				char c=getChar(currTransition);
				if (c=='\\' && inQuote) {
					nextFoundSlash=true;
				} else {
					nextFoundSlash=false;				
				}
				if ((c=='\'') && (!foundSlash)) {
					nextCurrTokenType=Edge.QUOTE;
					nextInQuote=!inQuote;
				} else  {
					if (inQuote && c !='%') {
                    //if (inQuote) {
						nextCurrTokenType=Edge.TEXTFIELD;
					} else if (ops.contains(String.valueOf(c))) {
						nextCurrTokenType=Edge.OP;
					} else {
						nextCurrTokenType=Edge.TEXTFIELD;
					}
				}
				nextLabelText = String.valueOf(c);
			}
			
			if ((currTokenType != nextCurrTokenType ) && (currTokenType == Edge.TEXTFIELD) && (!endTextField)) {
				nextP = addState(s);
				addTransition(p, nextP, labelText, currTokenType);
				updateProcessedStates(currentStates, p);
				endTextField=true;
			} 
			
			if ((currTokenType == nextCurrTokenType) && (currTokenType == Edge.TEXTFIELD)) {
				nextLabelText = labelText + nextLabelText;
				nextCurrentStates=currentStates;
			}
			
			nextCurrentStates.add(new Integer(s.getID()));
			
			if (white.contains(nextState)) {
				dfsVisit(nextP, nextState, nextLabelText, nextCurrTokenType, nextInQuote, nextFoundSlash, nextCurrentStates);
			} else {
				//next state is a black state
				if (nextCurrTokenType == Edge.TEXTFIELD)  {
					dfsVisit(nextP, nextState, nextLabelText, nextCurrTokenType, nextInQuote, nextFoundSlash, nextCurrentStates);
				} else {
					Node tempNextP = (Node)processedStates.get(new Integer(nextState.getID()));
					if (tempNextP == null) System.out.println("Could not locate: " + nextState.getID());
					addTransition(nextP, tempNextP, nextLabelText, nextCurrTokenType);
					updateProcessedStates(nextCurrentStates, nextP);
				}			
			}
		}
		gray.remove(s);
		black.add(s);
	}
	
	private boolean isRangedTransition(Transition t)	{
		if (t.getMin()!=t.getMax())	{
			return true;
		} else {
			return false;
		}
	}
	
	private char getChar(Transition t) {
		return t.getMin();	
	}
	
	private void fixState (State s) {
		//this is where we cut all  cycles
		Set edges=s.getTransitions();
		Set removeEdges=new HashSet();
		Iterator it=edges.iterator();
		while (it.hasNext()) {
			Transition t = (Transition)it.next();
			State d=t.getDest();
			if ((s==d) || (gray.contains(d))){
				removeEdges.add(t);
			}
		}
		edges.removeAll(removeEdges);
		s.resetTransitions(edges);		
	}
	
	private void addTransition(Node source, Node dest, String label, int type) {
		Edge transition = new Edge(source, dest, label, type);
		source.addOutTransition(transition);
		dest.addInTransition(transition);		
		transitions.add(transition);
	}
	
	private Node addState(State s) {
		Node t = new Node(s);
		states.add(t);
		return t;
	}
	
	private void updateProcessedStates(Set currentStates, Node t) {
		Iterator csit=currentStates.iterator();
		while (csit.hasNext()) {
			Integer stateID = (Integer)csit.next();	
			processedStates.put(stateID, t);
		}
	}
	
}
