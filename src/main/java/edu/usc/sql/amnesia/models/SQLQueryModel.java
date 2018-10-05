package edu.usc.sql.amnesia.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SQLQueryModel {
	protected String name;
	protected Set states, transitions;
	protected Node initialState;
	protected boolean userDefined;
	
	protected List longestMatchQuery;
	protected List matchPathQuery;
	protected List longestMatchModel;
	protected List matchPathModel;
    protected Set visited;
	
	public SQLQueryModel(String iName, boolean iUserDefined, Set iStates, Set iTransitions, Node iState) {
		matchPathQuery = new ArrayList();
		longestMatchQuery = new ArrayList();
		matchPathModel = new ArrayList();
		longestMatchModel = new ArrayList();
		visited = new HashSet();
	}
	
	public boolean hasUserDefinedElements() {
		return userDefined;
	}
    
	public boolean accepts(Query query) {
		matchPathQuery.clear();
		matchPathModel.clear();
		visited.clear();
		longestMatchQuery.clear();
		longestMatchModel.clear();
		boolean accepts=explore(query.getInitialState(), initialState, longestMatchQuery, longestMatchModel);
		if (accepts) {
			longestMatchModel=matchPathModel;
			longestMatchQuery=matchPathQuery;
		} 
		return accepts;
	}
    
	private boolean explore(SQLToken queryToken, Node modelNode, List matchQuery, List matchModel) {
		if (matchQuery.size() > longestMatchQuery.size()) {longestMatchQuery=matchQuery;}
		if (matchModel.size() > longestMatchModel.size()) {longestMatchModel=matchModel;}
		
		if ((queryToken != null) && (modelNode != null)) {
			if ((queryToken.getNext()==null) && (modelNode.isAccept())) {
				return true;
			} else {		
				Set nextModelTransitionSet=modelNode.next(queryToken);
				Iterator it = nextModelTransitionSet.iterator();
				while (it.hasNext()) {
					Edge nextModelEdge = (Edge) it.next();
					visited.add(nextModelEdge);
					ArrayList tempQuery = new ArrayList(matchQuery);
					ArrayList tempModel = new ArrayList(matchModel);
					tempQuery.add(queryToken);
					tempModel.add(nextModelEdge);
					if (explore(queryToken.getNext(), (Node)nextModelEdge.getDest(), tempQuery, tempModel)) {
						matchPathQuery.add(0, queryToken);
						matchPathModel.add(0, nextModelEdge);
						return true;
					}
				} 
				return false;    
			}
		} else {
			return false;
		}
	}
}
