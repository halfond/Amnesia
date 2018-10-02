package amnesia.models;

import java.util.ArrayList;
import java.util.List;

public class Query {
	
	private List tokens;

	public Query (String queryString) {
		tokens = new ArrayList();
		//TODO: tokenize the query string
	}
	
	public void addToken(SQLToken st) {
		if (!tokens.isEmpty()) {
			SQLToken last = (SQLToken)tokens.get(tokens.size()-1);
			last.setNext(st);
		}
		tokens.add(st);
	}
	
	public List getTokens() {
		return tokens;
	}
	
	public SQLToken getInitialState() {
		return (SQLToken)tokens.get(0);
	}
}
