/*
 * Copyright 2004
 * Georgia Tech Research Corporation
 * Atlanta, GA  30332-0415
 * All Rights Reserved
 */

package sql.models;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import amnesia.lexer.Lexer;
import amnesia.lexer.LexicalToken;
import amnesia.lexer.SQLLexerException;

public class QueryModel {
    QueryToken initialState=null;
    private List<QueryToken> states = new ArrayList<QueryToken>();
    private List<QueryTokenTransition> transitions = new ArrayList<QueryTokenTransition>();
    
    public QueryModel(String queryString, Lexer l) throws SQLLexerException {
        List tokens = l.lexQuery(queryString);
        Iterator it = tokens.iterator();
        initialState = new QueryToken();
        states.add(initialState);
        initialState.setInitial();
        QueryToken prevState = initialState;
        while (it.hasNext()) {
            LexicalToken lt=(LexicalToken)it.next();
            QueryToken nextState = new QueryToken();
            states.add(nextState);
            QueryTokenTransition tt = new QueryTokenTransition(prevState, nextState, lt);
            transitions.add(tt);
            prevState.addOutTransition(tt);
            nextState.addInTransition(tt);
            prevState=nextState;
        }
        prevState.setAccept(true);  
    }
    
    public QueryToken getInitialState() {
        return initialState;
    }    
        
    public List<QueryTokenTransition> getTransitions() {
    	return transitions;
    }
    
    
//  This function copied from the JSA implementation
    public String toDot() {
        StringBuffer b = new StringBuffer("digraph Automaton {\n");
        b.append("  rankdir = LR;\n");
        Iterator i = transitions.iterator();
        while (i.hasNext()) {
            TokenTransition tt = (TokenTransition) i.next();
            b.append(tt.toDot());            
        }
        return b.append("}\n").toString();
    }
    
//  This function modified from the JSA implementation
    public String toString() {
        StringBuffer b = new StringBuffer("Query Model: \n");
        QueryToken qt = initialState;
        TokenTransition tt = qt.next();
        while (tt != null) {
            b.append(qt);
            qt=(QueryToken)tt.getDest();
            tt=qt.next();
        }
        b.append("\n");
        return b.toString();
    }
    
    public void toFile(String fileName) {
        try {
            FileWriter outputFile=new FileWriter(fileName);
            outputFile.write(toDot());
            outputFile.close();         
        } catch (IOException ioe) {
            System.err.println("Could not create output file: " + fileName);
            ioe.printStackTrace();
        }
        
    }
}
