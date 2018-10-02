package sql.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QueryAnalysis {

	protected List<TokenTransition> longestMatchQuery;
	protected List<TokenTransition> longestMatchModel;
	protected Set<TokenTransition> visited;
	protected boolean accept;


	
	
	public QueryAnalysis() {
		longestMatchQuery = new ArrayList<TokenTransition>();
		longestMatchModel = new ArrayList<TokenTransition>();
		visited = new HashSet<TokenTransition>();
	}

	public List<TokenTransition> getLongestMatchModel() {
		return longestMatchModel;
	}
	public List<TokenTransition> getLongestMatchQuery() {
		return longestMatchQuery;
	}
	public Set<TokenTransition> getVisited() {
		return visited;
	}
	public boolean getAccept() {
		return accept;
	}

	protected void setAccept(boolean accept) {
		this.accept = accept;
	}

	protected void setLongestMatchModel(List<TokenTransition> longestMatchModel) {
		this.longestMatchModel = longestMatchModel;
	}

	protected void setLongestMatchQuery(List<TokenTransition> longestMatchQuery) {
		this.longestMatchQuery = longestMatchQuery;
	}

	protected void setVisited(Set<TokenTransition> visited) {
		this.visited = visited;
	}
	
	

}
