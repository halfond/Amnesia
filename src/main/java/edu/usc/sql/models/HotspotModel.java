/*
 *Copyright 2005 
 *Georgia Tech Research Corporation
 *Atlanta, GA  30332-0415
 *All Rights Reserved
 */


package edu.usc.sql.models;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.usc.sql.amnesia.lexer.Lexer;
import edu.usc.sql.amnesia.lexer.SQLLexerException;
import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

public class HotspotModel implements Serializable {
	private static final long serialVersionUID = 1L;
	private static Set<String> ops, keywords;

	protected Set<HotspotToken> states;
	protected Set<TokenTransition> transitions;
	protected HotspotToken initialState;
	
	
	private Set<State> white, gray, black;
	private Map<Integer, HotspotToken> processedStates;
	private boolean userDefinedFlag=false;


	static {
		String text;
		ops = new HashSet<String>();
		keywords = new HashSet<String>();
		try {
			BufferedReader keywordsFile = new BufferedReader(new FileReader("/tmp/SQL.keywords"));
			BufferedReader opsFile = new BufferedReader(new FileReader("/tmp/SQL.ops"));

			while ((text = keywordsFile.readLine()) != null) {
				keywords.add(text);
			}
			while ((text = opsFile.readLine()) != null) {
				ops.add(text);
			}
			keywordsFile.close();
			opsFile.close();
		} catch (FileNotFoundException fnfe) {
			String[] k = {"SELECT", "FROM", "WHERE", "OR", "AND", "DROP", "UPDATE", "LIKE", "UNION", "INNER_JOIN"};
			keywords.addAll(Arrays.asList(k));
			String[] o = {"=", "<",	">", "<=", ">=", "!=", "!", ")", "(", ",", "\"", "*", "+", "-",	"%", "'", ";", ".", " "};
			ops.addAll(Arrays.asList(o));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public HotspotModel (Set<HotspotToken> initStates, HotspotToken initState) {
		states=initStates;
		initialState=initState;
	}

	public HotspotModel (Automaton a) {
		states = new HashSet<HotspotToken>();
		transitions = new HashSet<TokenTransition>();
		processedStates=new HashMap<Integer, HotspotToken>();

		initialState=new HotspotToken(a.getInitialState());
		initialState.setInitial();
		white = new HashSet<State>(a.getStates());
		gray = new HashSet<State>();
		black = new HashSet<State>();
		states.add(initialState);

		dfsVisit(initialState, a.getInitialState(), "", TokenTransition.NONE, false, false, new HashSet<Integer>());
	}


	private void dfsVisit(HotspotToken p, State s, String labelText, int currTokenType, boolean inQuote, boolean foundSlash, Set<Integer> currentStates) {
		white.remove(s);
		gray.add(s);
		fixState(s);
		Set edges = s.getTransitions();
		Iterator it = edges.iterator();

		HotspotToken nextP = p;
		boolean nextFoundSlash=foundSlash;
		int nextCurrTokenType=currTokenType;
		String nextLabelText = "";
		Set<Integer> nextCurrentStates = new HashSet<Integer>();

		if (((currTokenType != TokenTransition.TEXTFIELD) && (currTokenType != TokenTransition.NONE)) || edges.size()==0) {
			nextP = new HotspotToken(s);
			states.add(nextP);
			TokenTransition newTokenTransition= new TokenTransition(p, nextP, labelText, currTokenType);
			transitions.add(newTokenTransition);
			p.addOutTransition(newTokenTransition);
			nextP.addInTransition(newTokenTransition);
			processedStates.put(new Integer(s.getID()), nextP);
			Iterator csit=currentStates.iterator();
			while (csit.hasNext()) {
				Integer stateID = (Integer)csit.next();	
				processedStates.put(stateID, p);
			}	
		}

		boolean endTextField=false;
		while (it.hasNext()) {
			Transition currTransition = (Transition) it.next();
			State nextState = currTransition.getDest();
			boolean nextInQuote=inQuote;

			if (isRangedTransition(currTransition)) {
				nextCurrTokenType=TokenTransition.VAR;
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
					nextCurrTokenType=TokenTransition.QUOTE;
					nextInQuote=!inQuote;
				} else  {
					if (inQuote && c !='%') {
						//if (inQuote) {
						nextCurrTokenType=TokenTransition.TEXTFIELD;
					} else if (ops.contains(String.valueOf(c))) {
						nextCurrTokenType=TokenTransition.OP;
					} else {
						nextCurrTokenType=TokenTransition.TEXTFIELD;
					}
				}
				nextLabelText = String.valueOf(c);
			}

			if ((currTokenType != nextCurrTokenType ) && (currTokenType == TokenTransition.TEXTFIELD) && (!endTextField)) {
				nextP = new HotspotToken(s);
				states.add(nextP);
				TokenTransition newTokenTransition= new TokenTransition(p, nextP, labelText, currTokenType);
				transitions.add(newTokenTransition);
				p.addOutTransition(newTokenTransition);
				nextP.addInTransition(newTokenTransition);

				Iterator csit=currentStates.iterator();
				while (csit.hasNext()) {
					Integer stateID = (Integer)csit.next();	
					processedStates.put(stateID, p);
				}				
				endTextField=true;
			} 

			if ((currTokenType == nextCurrTokenType) && (currTokenType == TokenTransition.TEXTFIELD)) {
				nextLabelText = labelText + nextLabelText;
				nextCurrentStates=currentStates;
			}

			nextCurrentStates.add(new Integer(s.getID()));

			if (white.contains(nextState)) {
				dfsVisit(nextP, nextState, nextLabelText, nextCurrTokenType, nextInQuote, nextFoundSlash, nextCurrentStates);
			} else {
				//next state is a black state
				if (nextCurrTokenType == TokenTransition.TEXTFIELD)  {
					dfsVisit(nextP, nextState, nextLabelText, nextCurrTokenType, nextInQuote, nextFoundSlash, nextCurrentStates);
				} else {
					Token tempNextP = (Token)processedStates.get(new Integer(nextState.getID()));
					if (tempNextP == null) System.out.println("Could not locate: " + nextState.getID());
					TokenTransition newTokenTransition= new TokenTransition(nextP, tempNextP, nextLabelText, nextCurrTokenType);
					transitions.add(newTokenTransition);
					nextP.addOutTransition(newTokenTransition);
					tempNextP.addInTransition(newTokenTransition);
					Iterator csit=nextCurrentStates.iterator();
					while (csit.hasNext()) {
						Integer stateID = (Integer)csit.next();	
						processedStates.put(stateID, nextP);
					}
				}			
			}
		}
		gray.remove(s);
		black.add(s);
	}

	public boolean hasUserDefinedElements() {
		return userDefinedFlag;
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
		Set<Transition> edges=s.getTransitions();
		Set<Transition> removeEdges=new HashSet<Transition>();
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


	public boolean accepts(String queryString, Lexer dbLexer) throws SQLLexerException {
		return analyze(queryString, dbLexer).getAccept();
	}

	public QueryAnalysis analyze(String queryString, Lexer dbLexer) throws SQLLexerException {
		QueryModel qModel = new QueryModel(queryString, dbLexer);
		QueryAnalysis qa = new QueryAnalysis();
		boolean accepts=explore(qModel.getInitialState(), initialState, new ArrayList<TokenTransition>(), new ArrayList<TokenTransition>(), qa);
		qa.setAccept(accepts);
		return qa;
	}
	
	
	private boolean explore(QueryToken queryNode, HotspotToken modelNode, List<TokenTransition> matchQuery, List<TokenTransition> matchModel, QueryAnalysis qa) {
		if (matchQuery.size() > qa.getLongestMatchQuery().size()) {qa.setLongestMatchQuery(matchQuery);}
		if (matchModel.size() > qa.getLongestMatchModel().size()) {qa.setLongestMatchModel(matchModel);}

		if ((queryNode != null) && (modelNode != null)) {
			if ((queryNode.isAccept()) && (modelNode.isAccept())) {
				return true;
			} else {
				TokenTransition nextQueryTransition=queryNode.next();
				if (nextQueryTransition!=null) {
					Set nextModelTransitionSet=modelNode.next(nextQueryTransition);
					Iterator it = nextModelTransitionSet.iterator();
					while (it.hasNext()) {
						TokenTransition nextModelTransition = (TokenTransition) it.next();
						qa.getVisited().add(nextModelTransition);
						List<TokenTransition> tempQuery = new ArrayList<TokenTransition>(matchQuery);
						List<TokenTransition> tempModel = new ArrayList<TokenTransition>(matchModel);
						tempQuery.add(nextQueryTransition);
						tempModel.add(nextModelTransition);
						if (explore((QueryToken)nextQueryTransition.getDest(), (HotspotToken)nextModelTransition.getDest(), tempQuery, tempModel, qa)) {
							return true;
						}
					} 
					return false;    
				} else {
					return false;
				}
			}
		} else {
			return false;
		}
	}

	public int getNumberOfStates() {
		return states.size();
	}

	public Set<HotspotToken> getStates() {
		return states;
	}

	public Set<TokenTransition> getTransitions() {
		return transitions;
	}

	public HotspotToken getInitialState() {
		return initialState;
	}

	public String toGraphML() {
		StringBuffer graph = new StringBuffer();
		graph.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		graph.append("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\"> \n");
		graph.append("<graph edgedefault=\"undirected\">\n\n");
		graph.append(TokenTransition.toGraphMLInit()+"\n");

		Iterator i = states.iterator();
		while (i.hasNext()) {
			HotspotToken s = (HotspotToken) i.next();
			graph.append(s.toGraphML());
		}

		graph.append("\n</graph>\n");
		graph.append("</graphml>\n");
		return graph.toString();
	}

	//This function copied from the JSA implementation
	public String toDot() {
		StringBuffer b = new StringBuffer("digraph Automaton {\n");
		b.append("  rankdir = LR;\n");
		Iterator i = states.iterator();
		while (i.hasNext()) {
			HotspotToken s = (HotspotToken) i.next();
			b.append(s.toDot());
		}
		return b.append("}\n").toString();
	}

//	This function modified from the JSA implementation
	public String toString() {
		StringBuffer b = new StringBuffer("Hotspot Model: \n");
		Iterator i = states.iterator();
		while (i.hasNext()) {
			HotspotToken s = (HotspotToken) i.next();
			b.append(s);
		}
		b.append("\n");
		return b.toString();
	}

}


